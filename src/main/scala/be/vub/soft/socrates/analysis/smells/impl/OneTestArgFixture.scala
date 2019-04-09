package be.vub.soft.socrates.analysis.smells.impl

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.analysis.smells.TestCaseTestSmell
import be.vub.soft.socrates.util.Debuggable

import scala.meta.Term
import scala.meta.internal.semanticdb.Scala._
import scala.meta.internal.semanticdb._
import scala.meta.internal.semanticdb.{ClassSignature, Scope, SymbolInformation, ThisType, TypeRef, TypeSignature, ValueSignature}

object OneTestArgFixture extends TestCaseTestSmell with Debuggable {

    override def verify(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean = {

        if(testCase.hasOneTestArgFixture) {
            val fixture = testCase.oneTestArgFixture.head // only one

            // Get all symbols representing the fixture (or in case of a case class, the symbols of the fields)
            val fixtureSymbols:List[SymbolInformation] = SemanticDB.symbolInformationGlobal(fixture, testCase.document, pdb.symTab).map({

                case fs@SymbolInformation(_,_,_,_,_,ValueSignature(TypeRef(_, symbolFixture,_)),_,_) => {

                    //TODO: avoid configmap
                    val symbols: List[SymbolInformation] = pdb.lookupGlobally(symbolFixture).map({
                        case SymbolInformation(_ ,_ ,_ ,_ ,_ , TypeSignature(_,a:TypeRef,b: TypeRef),List(),_) if a.equals(b) => a.symbol // type FixtureParam = someType
                        case SymbolInformation(symbol,_,_,_,_, ClassSignature(_,_, _,_),_,_) => symbol // FixtureParam redefined
                    }).flatMap({
                        symbol: String => pdb.lookupGlobally(symbol) //Option[SymbolInformation] // Lookup fixtureparam again, or someType
                    }).map({
                        // Case class: check all fields are referenced
                        case si@SymbolInformation(_,_,_,_,_, ClassSignature(_, _, _, Some(Scope(decls, _))),_,_) if si.isCase => {
                            val fields: List[SymbolInformation] = decls.toList.flatMap(d => {
                                pdb.lookupGlobally(d)
                            }).filter(declSymbol => declSymbol.isVal && declSymbol.isMethod)

                            fields
                        }
                        // Class, tuples, ...: just check instance is used
                        // just get local symbol of param, not of reference typed!
                        case _: SymbolInformation => List(fs) // !si.isCase or class
                    }).getOrElse(List.empty)

                    symbols
                }
                case fs => assert(false, s"Unknown fixture symbol: $fs"); List()
            }).getOrElse(List.empty)

            // Get all symbols that occur in the testcase
            val used = testCase.ast
                .collect({ case t: Term.Name if !testCase.oneTestArgFixture.head.equals(t) => t })
                .flatMap(t => testCase.document.occurrences.find(p => SemanticDB.position(p.range.get, t.pos)))
                .flatMap({case s: SymbolOccurrence => if(s.symbol.isLocal) testCase.document.symbols.find(p => p.symbol.equals(s.symbol)) else pdb.lookupGlobally(s.symbol) })
                // filter those that refer to the fixture (either the fields of the case class, or simply the parameter)
                .filter(symbol => fixtureSymbols.contains(symbol))
                .toSet

            // We have a local general fixture in case there the size of `used` is smaller than `fixtureSymbols`,
            // because that means one or more parameters was not used
            val result = used.size < fixtureSymbols.size

            debug("onetestarg fixture RESULT="+result)

            result
        } else {
            false // No local fixture parameter.
        }

    }

    override val description: String = "OneTestArgFixture"
}
