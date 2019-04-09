package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.internal.semanticdb.TextDocument
import scala.meta.{Lit, Term, Tree}

/*
    http://doc.scalatest.org/3.0.1/index.html#org.scalatest.FunSuite
    Recommended Usage: For teams coming from xUnit, FunSuite feels comfortable and familiar while still giving some benefits of BDD:
    FunSuite makes it easy to write descriptive test names, natural to write focused tests,
    and generates specification-like output that can facilitate communication among stakeholders.
*/
object FunSuite extends Explorable {

    override def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Term.Apply(Term.Apply(Term.Name("test"), Lit.String(_) :: _), params) =>
            ScalaTestCase(test, document, collectParamFixture(params), collectLoanFixtures(test), collectFixtureContexts(params))
    })
}
