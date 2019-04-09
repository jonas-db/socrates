package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.internal.semanticdb.TextDocument
import scala.meta.{Lit, Term, Tree}

/*
    http://doc.scalatest.org/3.0.1/index.html#org.scalatest.WordSpec
    Recommended Usage: For teams coming from specs or specs2, WordSpec will feel familiar, and is often the most natural way to port specsN tests to ScalaTest.
    WordSpec is very prescriptive in how text must be written, so a good fit for teams who want a high degree of discipline enforced upon their specification text.
*/
object WordSpec extends Explorable {

    val words = List("when", "in", "which", "should", "must", "can", "will", "that")
    def check(s: String) = words.contains(s)

    override def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Term.ApplyInfix(_: Lit.String | _: Term.Interpolate, Term.Name("in"), _, params) =>
            ScalaTestCase(test, document, collectParamFixture(params), collectLoanFixtures(test), collectFixtureContexts(params))
    })

}
