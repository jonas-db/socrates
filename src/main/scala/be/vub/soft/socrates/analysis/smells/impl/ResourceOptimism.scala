package be.vub.soft.socrates.analysis.smells.impl

import java.io.File

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.analysis.smells.TestCaseTestSmell

import scala.meta.Tree

/*
    Resource optimism requires checking the path if it exists or not.
    It is a subset of MysteryGuest

 */
object ResourceOptimism extends TestCaseTestSmell {

    override def verify(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean = {
        val ast = testCase.ast

        def lookup(t: Tree, ss: String) = SemanticDB
            .symbolInformationGlobal(t, testCase.document, pdb.symTab)
            .exists(s => s.symbol.equals(ss))

        MysteryGuest
            .collect(ast, lookup)
            .exists(p => !new File(p).exists())
    }

    override val description: String = "ResourceOptimism"
}
