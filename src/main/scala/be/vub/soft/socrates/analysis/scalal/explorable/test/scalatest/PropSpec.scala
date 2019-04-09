package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.internal.semanticdb.TextDocument
import scala.meta.{Lit, Term, Tree}

/*
    http://doc.scalatest.org/3.0.1/#org.scalatest.PropSpec
    Recommended Usage: Class PropSpec is a good fit for teams that want to write tests exclusively in terms of property checks,
    and is also a good choice for writing the occasional test matrix when a different style trait is chosen as the main unit testing style.
*/
object PropSpec extends Explorable {

    override def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Term.Apply(Term.Apply(Term.Name("property"), Lit.String(_) :: _), params) =>
            ScalaTestCase(test, document, collectParamFixture(params), collectLoanFixtures(test), collectFixtureContexts(params))
    })
}
