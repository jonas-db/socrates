package be.vub.soft.socrates.analysis.scalal.explorable

import be.vub.soft.socrates.analysis.smells.TestSmells
import be.vub.soft.socrates.analysis.smells.impl._
import be.vub.soft.socrates.analysis.{NonEmptyScalaTestCaseResult, ScalaAnalyzable, ScalaTestCaseResult}
import be.vub.soft.socrates.semantic.ProjectDatabase
import be.vub.soft.socrates.util.Debuggable

import scala.meta.Tree
import scala.meta.internal.semanticdb.TextDocument

case class ScalaTestCase(ast: Tree,
                         document: TextDocument,
                         oneTestArgFixture: List[Tree] = List.empty,
                         loanFixtureMethods: List[Tree] = List.empty,
                         contextFixtures: List[Tree] = List.empty)
    extends ScalaAnalyzable {

    override def analyze(debugger: Debuggable, scalaTestSuite: ScalaTestClass, pdb: ProjectDatabase): ScalaTestCaseResult = {
        val loc = ast.toString().lines.size

        val hasOneTestArgFixture    = oneTestArgFixture.nonEmpty
        val hasLoanFixtureMethods   = loanFixtureMethods.nonEmpty
        val hasContextFixtures      = contextFixtures.nonEmpty

        val smells              = TestSmells.verifyTestCase(this, scalaTestSuite, pdb)
        val assertionRoulette   = smells.getOrElse(AssertionRoulette.description, false)
        val sensitiveEquality   = smells.getOrElse(SensitiveEquality.description, false)
        val eagerTest           = smells.getOrElse(EagerTest.description, false)
        val loanTestArgFixture  = smells.getOrElse(LoanTestArgFixture.description, false)
        val oneTestArgFixture2  = smells.getOrElse(OneTestArgFixture.description, false)
        val contextFixture      = smells.getOrElse(ContextFixture.description, false)
        val resourceOptimism    = smells.getOrElse(ResourceOptimism.description, false)
        val mysteryGuest        = smells.getOrElse(MysteryGuest.description, false)

        debugger.debug("Test smells:")
        debugger.debug(ast.toString())
        debugger.debug(smells.toString())

        NonEmptyScalaTestCaseResult(ast.toString(), loc, hasOneTestArgFixture, hasLoanFixtureMethods, hasContextFixtures,
            assertionRoulette, sensitiveEquality, eagerTest, loanTestArgFixture,
            oneTestArgFixture2, contextFixture, resourceOptimism, mysteryGuest)
    }

    def hasOneTestArgFixture: Boolean = oneTestArgFixture.nonEmpty
    def hasLoanTestArgFixture: Boolean = loanFixtureMethods.nonEmpty
    def hasContextFixture: Boolean = contextFixtures.nonEmpty
}
