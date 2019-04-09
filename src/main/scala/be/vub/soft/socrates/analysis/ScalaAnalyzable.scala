package be.vub.soft.socrates.analysis

import be.vub.soft.socrates.analysis.scalal.explorable.ScalaTestClass
import be.vub.soft.socrates.semantic.ProjectDatabase
import be.vub.soft.socrates.util.Debuggable

abstract class ScalaResult
case object EmptyScalaResult extends ScalaResult

abstract class ScalaAnalyzable {
    def analyze(debugger: Debuggable, sts: ScalaTestClass, pdb: ProjectDatabase): ScalaTestCaseResult = EmptyScalaTestCaseResult()
    def analyze(debugger: Debuggable, pdb: ProjectDatabase): ScalaTestSuiteResult = EmptyScalaTestSuiteResult()
    def analyze(debugger: Debuggable): ScalaProjectResult = EmptyScalaProjectResult()
}

abstract class ScalaTestCaseResult extends ScalaResult
case class EmptyScalaTestCaseResult() extends ScalaTestCaseResult
case class NonEmptyScalaTestCaseResult(ast: String,
                                       loc: Int,
                                       hasOneTestArgFixture: Boolean,
                                       hasLoanFixtureMethods: Boolean,
                                       hasContextFixtures: Boolean,
                                       assertionRoulette: Boolean,
                                       sensitiveEquality: Boolean,
                                       eagerTest: Boolean,
                                       loanTestArgFixture: Boolean,
                                       oneTestArgFixture: Boolean,
                                       contextFixture: Boolean,
                                       resourceOptimism: Boolean,
                                       mysteryGuest: Boolean) extends ScalaTestCaseResult

abstract class ScalaProductionResult extends ScalaResult
case class NonEmptyScalaProductionResult(ast: String,
                                        loc: Int) extends ScalaProductionResult

abstract class ScalaTestSuiteResult extends ScalaResult
case class EmptyScalaTestSuiteResult() extends ScalaTestSuiteResult
case class NonEmptyScalaTestSuiteResult(ast: String,
                                        loc: Int,
                                        uri: String,
                                        productionUri: String,
                                        tests: List[ScalaTestCaseResult],
                                        style: String,
                                        implementation: String,
                                        hasGeneralFixture: Boolean,
                                        generalFixture: Boolean,
                                        lazyTest: Boolean) extends ScalaTestSuiteResult


abstract class ScalaProjectResult extends ScalaResult
case class EmptyScalaProjectResult(reason: String = "Unknown") extends ScalaProjectResult
case class NonEmptyScalaProjectResult(productionFiles: Int,
                                      productionComponents: Int,
                                      productionLOCs: Int,
                                      productionComponentsLOCs: Int,
                                      productions: List[ScalaProductionResult],
                                      testFiles: Int,
                                      testComponents: Int,
                                      testLOCs: Int,
                                      testComponentsLOCs: Int,
                                      tests: List[ScalaTestSuiteResult]) extends ScalaProjectResult

object ScalaProjectResult {
    def empty = NonEmptyScalaProjectResult(0, 0, 0, 0, List.empty, 0, 0, 0, 0, List.empty)
}