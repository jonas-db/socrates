package be.vub.soft.socrates.analysis.smells.impl

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.analysis.smells.TestCaseTestSmell
import be.vub.soft.socrates.util.Debuggable

import scala.meta.Term
import scala.meta.internal.semanticdb.Scala._
import scala.meta.internal.semanticdb._
import scala.meta.internal.semanticdb.{ClassSignature, Scope, SymbolInformation, ThisType, TypeRef, TypeSignature, ValueSignature}

object LoanTestArgFixture extends TestCaseTestSmell with Debuggable {

    override def verify(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean = {

        if(testCase.hasLoanTestArgFixture) {
            val loanFixtures = testCase.loanFixtureMethods

            debug("loanFixtures="+loanFixtures)

            // Get for the symbol for every parameter (should all be local)
            val loanFixtureSymbols: List[SymbolInformation] = loanFixtures
                .flatMap(t => testCase.document.occurrences.find(p => SemanticDB.position(p.range.get, t.pos)))
                .flatMap({case s: SymbolOccurrence => if(s.symbol.isLocal) testCase.document.symbols.find(p => p.symbol.equals(s.symbol)) else pdb.lookupGlobally(s.symbol) })

            // Get all symbols that occur in the testcase
            val used = testCase.ast
                .collect({ case t: Term.Name if !loanFixtures.contains(t) => t })
                .flatMap(t => testCase.document.occurrences.find(p => SemanticDB.position(p.range.get, t.pos)))
                .flatMap({case s: SymbolOccurrence => if(s.symbol.isLocal) testCase.document.symbols.find(p => p.symbol.equals(s.symbol)) else pdb.lookupGlobally(s.symbol) })
                .filter(symbol => loanFixtureSymbols.contains(symbol))
                .toSet

            debug("used="+used)
            debug(loanFixtureSymbols.mkString(","))

            // We have a local general fixture when we use less variables than fixture params
            val result = used.size < loanFixtureSymbols.size

            debug("loanFixture RESULT="+result)

            result
        } else {
            false // No loan fixtures.
        }

    }

    override val description: String = "LoanTestArgFixture"
}
