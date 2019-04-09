package be.vub.soft.socrates.analysis.scalal.explorable

import be.vub.soft.socrates.analysis.smells.TestSmells
import be.vub.soft.socrates.analysis.smells.impl.{GeneralFixture, LazyTest}
import be.vub.soft.socrates.analysis.{NonEmptyScalaTestSuiteResult, ScalaAnalyzable, ScalaTestSuiteResult}
import be.vub.soft.socrates.semantic.ProjectDatabase
import be.vub.soft.socrates.util.Debuggable

import scala.meta.Tree
import scala.meta.internal.semanticdb.TextDocument

case class ScalaTestClass(ast: Tree,
                          document: TextDocument,
                          production: Option[(Tree, TextDocument)],
                          tests: List[ScalaTestCase] = List.empty[ScalaTestCase],
                          style: String = "Unknown",
                          implementation: String = "Unknown",
                          hasGeneralFixture: Boolean = false) extends ScalaAnalyzable {

    override def analyze(debugger: Debuggable, pdb: ProjectDatabase): ScalaTestSuiteResult = {
        val loc = ast.toString().lines.size
        val productionUri = if(production.nonEmpty) production.get._2.uri else "Unknown"

        val smells          = TestSmells.verifyTestClass(this, pdb) // TODO
        val lazyTest        = smells.getOrElse(LazyTest.description, false)
        val generalFixture  = smells.getOrElse(GeneralFixture.description, false)

        val testCases       = tests.map(_.analyze(debugger, this, pdb))

        debugger.debug("Test suite smells:")
        debugger.debug(ast.toString())
        debugger.debug(smells.toString())

        NonEmptyScalaTestSuiteResult(ast.toString(), loc, document.uri, productionUri, testCases, style, implementation, hasGeneralFixture, generalFixture, lazyTest)
    }

}
