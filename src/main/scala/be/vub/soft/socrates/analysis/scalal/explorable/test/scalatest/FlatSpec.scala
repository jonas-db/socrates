package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.internal.semanticdb.TextDocument
import scala.meta.{Lit, Template, Term, Tree}

/*
    http://doc.scalatest.org/3.0.1/index.html#org.scalatest.FlatSpec
    Recommended Usage: Class FlatSpec is a good first step for teams wishing to move from xUnit to BDD,
    because its structure is flat like xUnit, so simple and familiar, but the test names must be written in a specification style: “X should Y,” “A must B,” etc.
    FlatSpec's no-nesting approach contrasts with traits Spec and WordSpec, which use nesting to reduce duplication of specification text.
*/
object FlatSpec extends Explorable {

    def check(s: String) = s.contains("should") || s.contains("can") || s.contains("must")

    override def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Term.ApplyInfix(Term.ApplyInfix(_, Term.Name(smc),_, List(Lit.String(_))), Term.Name("in"), _,  params) if check(smc) =>
                ScalaTestCase(test, document, collectParamFixture(params), collectLoanFixtures(test), collectFixtureContexts(params))
    })


}
