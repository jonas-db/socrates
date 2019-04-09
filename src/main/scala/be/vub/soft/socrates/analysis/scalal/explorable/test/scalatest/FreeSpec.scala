package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.internal.semanticdb.TextDocument
import scala.meta.{Lit, Term, Tree}

/*
    http://doc.scalatest.org/3.0.1/index.html#org.scalatest.FreeSpec
    Recommended Usage: Because it gives absolute freedom (and no guidance) on how specification text should be written,
    FreeSpec is a good choice for teams experienced with BDD and able to agree on how to structure the specification text.
*/
object FreeSpec extends Explorable {

    override def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Term.ApplyInfix(Lit.String(_), Term.Name("in"), _, params) =>
            ScalaTestCase(test, document, collectParamFixture(params), collectLoanFixtures(test), collectFixtureContexts(params))
    })
}
