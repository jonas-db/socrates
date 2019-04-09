package be.vub.soft.socrates.analysis.smells

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.analysis.smells.impl.{ContextFixture, _}
import be.vub.soft.socrates.semantic.ProjectDatabase

object TestSmells {

    val testClassSmells: List[TestClassTestSmell] = List(
        GeneralFixture,
        LazyTest,
    )

    val testCaseSmells: List[TestCaseTestSmell] = List(
        AssertionRoulette,
        SensitiveEquality,
        LoanTestArgFixture,
        OneTestArgFixture,
        EagerTest,
        MysteryGuest,
        ContextFixture,
    )

    def verifyTestCase(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb: ProjectDatabase): Map[String, Boolean] = {
        testCaseSmells.map(x => (x.description, x)).map({ case (desc, x) => (desc, x.verify(testCase, testSuite, pdb)) }).toMap
    }

    def verifyTestClass(testSuite: ScalaTestClass, pdb: ProjectDatabase): Map[String, Boolean] = {
        testClassSmells.map(x => (x.description, x)).map({ case (desc, x) => (desc, x.verify(testSuite, pdb)) }).toMap
    }

}

abstract class TestSmell {
    val description: String
}

abstract class TestCaseTestSmell extends TestSmell {
    def verify(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean
}

abstract class TestClassTestSmell extends TestSmell {
    def verify(testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean
}


