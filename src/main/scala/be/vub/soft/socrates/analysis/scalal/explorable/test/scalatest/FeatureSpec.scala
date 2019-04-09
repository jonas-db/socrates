package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.internal.semanticdb.TextDocument
import scala.meta.{Lit, Term, Tree}

/*
    http://doc.scalatest.org/3.0.1/#org.scalatest.FeatureSpec
    Recommended Usage: Class FeatureSpec is primarily intended for acceptance testing,
    including facilitating the process of programmers working alongside non-programmers to define the acceptance requirements.
*/
object FeatureSpec extends Explorable {

    override def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Term.Apply(Term.Apply(Term.Name("scenario"), Lit.String(_) :: _), params) =>
            ScalaTestCase(test, document, collectParamFixture(params), collectLoanFixtures(test), collectFixtureContexts(params))
    })
}
