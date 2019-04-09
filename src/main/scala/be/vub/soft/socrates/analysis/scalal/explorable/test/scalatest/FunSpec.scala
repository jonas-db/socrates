package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.internal.semanticdb.TextDocument
import scala.meta.{Lit, Template, Term, Tree}

/*
    http://doc.scalatest.org/3.0.1/index.html#org.scalatest.FunSpec
    Recommended Usage: For teams coming from Ruby's RSpec tool, FunSpec will feel familiar and comfortable;
    More generally, for any team that prefers BDD, FunSpec's nesting and gentle guide to structuring text (with describe and it)
    provide an excellent general-purpose choice for writing specification-style tests.
 */
object FunSpec extends Explorable {

    def check(s: String) = s.contains("it") || s.contains("they")

    override def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Term.Apply(Term.Apply(Term.Name(it), List(Lit.String(_))), params) if check(it) =>
            ScalaTestCase(test, document, collectParamFixture(params), collectLoanFixtures(test), collectFixtureContexts(params))
    })


}
