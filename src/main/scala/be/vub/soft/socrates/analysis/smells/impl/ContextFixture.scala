package be.vub.soft.socrates.analysis.smells.impl

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.analysis.smells.TestCaseTestSmell
import be.vub.soft.socrates.util.Debuggable

import scala.meta.{Defn, Term, Type}
import scala.meta.internal.semanticdb.Scala._
import scala.meta.internal.semanticdb._
import scala.meta.internal.semanticdb.{ClassSignature, Scope, SymbolInformation, ThisType, TypeRef, TypeSignature, ValueSignature}
import scala.meta.contrib._
import scala.meta.internal.semanticdb._

object ContextFixture extends TestCaseTestSmell with Debuggable {

    override def verify(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean = {

        if(testCase.hasContextFixture) {
            debug(testSuite.document.uri +" has contextfixture")
            val contextFixtures = testCase.contextFixtures
            val names = contextFixtures.map({
                case Type.Name(n) => n
                case Term.Name(n) => n
            })

            // Get for the symbol for every type
            val contextFixturesSymbols: List[SymbolInformation] = contextFixtures
                .flatMap(t => testCase.document.occurrences.find(p => SemanticDB.position(p.range.get, t.pos)))
                .flatMap({case s: SymbolOccurrence => if(s.symbol.isLocal) testCase.document.symbols.find(p => p.symbol.equals(s.symbol)) else pdb.lookupGlobally(s.symbol) })

            // Find all definitions, get all ast/docs for these definitions
            val infos = contextFixturesSymbols.map(s => (s, pdb.definitions.get(s.symbol))).collect({
                case (s, Some(d)) if pdb.trees.isDefinedAt(d) && pdb.documents.isDefinedAt(d) => (s, pdb.trees(d), pdb.documents(d))
            })

            val allSymbols: Set[SymbolInformation] = infos.flatMap({
                case (_, ast, d) => ast.collect({
                    case Defn.Class(_, t@Type.Name(_), _, _, _) => SemanticDB.symbolInformationGlobal(t, d, pdb.symTab)
                    case Defn.Object(_, t@Term.Name(_), _) => SemanticDB.symbolInformationGlobal(t, d, pdb.symTab)
                    case Defn.Trait(_, t@Type.Name(_), _, _, _) => SemanticDB.symbolInformationGlobal(t, d, pdb.symTab)
                }).flatten
            }).collect({
                case SymbolInformation(_,_,_,_,dpn,ClassSignature(_, _, _, decls),_,_) if names.contains(dpn) => {
                    decls.map(scope => scope.symlinks.flatMap(s => pdb.lookupGlobally(s)).collect({ case s if s.isVal ||s.isVar => s }).toList).getOrElse(List.empty)
                }
            }).flatten.toSet

            // Get all symbols that occur in the testcase
            val used = testCase.ast
                .collect({ case t: Term.Name if !contextFixtures.contains(t) => t })
                .flatMap(t => testCase.document.occurrences.find(p => SemanticDB.position(p.range.get, t.pos)))
                .flatMap({case s: SymbolOccurrence => if(s.symbol.isLocal) testCase.document.symbols.find(p => p.symbol.equals(s.symbol)) else pdb.lookupGlobally(s.symbol) })
                .filter(symbol => allSymbols.contains(symbol))
                .toSet

            debug("used="+used.map(_.symbol))
            debug("all="+allSymbols.map(_.symbol).mkString(","))

            // We have a local general fixture when we use less variables than the variables from the context
            val result = used.size < allSymbols.size

            //println("usedpre="+used)
            debug("ContextFixture RESULT="+result)

            result
        } else {
            false // No context fixtures.
        }

    }

    override val description: String = "ContextFixture"
}
