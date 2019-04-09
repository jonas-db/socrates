package be.vub.soft.socrates.analysis.scalal.explorable

import scala.meta.{Defn, Init, Source, Template, Term, Tree, Type, _}
import be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest._
import be.vub.soft.socrates.analysis.scalal.explorable.test.scalacheck._
import be.vub.soft.socrates.analysis.scalal.explorable.test.specs2._
import be.vub.soft.socrates.analysis.scalal.explorable.test.Unknown

import scala.meta.Term.NewAnonymous
import scala.meta.internal.semanticdb.TextDocument

abstract class Explorable {

    def collectLoanFixtures(ast: Tree) = {
        val fixtures = ast.collect {
            case Term.Apply(f@Term.Name(n), List(Term.Block(List(Term.Function(params, _))))) if /*n.toLowerCase.startsWith("with") &&*/ params.nonEmpty => (f, params.collect({
                case Term.Param(_, name, _, _) => name
            }))
        }

        fixtures.flatMap({ case(_, params) => params })
    }

    def collectParamFixture(params: List[Tree]) = params match {
       case List(Term.Block(List(Term.Function(List(Term.Param(_, t: Term.Name, _, _)), _)))) => List(t)
       case _ => List.empty
    }

    def collectFixtureContexts(ast: List[Tree]): List[Tree] = ast match {
        case List(NewAnonymous(Template(_, inits, _, _))) => inits.map(_.tpe) // FlatSpec
        case List(Term.Block(List(NewAnonymous(Template(_, inits, _, _))))) => inits.map(_.tpe) // FunSpec
        case _ => List.empty
    }

    def explore(ast: Tree, document: TextDocument): List[ScalaTestCase]

}

object Explorable {

    val explorers: Map[String, Explorable] = Map(
        "FunSpec" -> FunSpec,
        "FunSuite" -> FunSuite,
        "FlatSpec" -> FlatSpec,
        "WordSpec" -> WordSpec,
        "FreeSpec" -> FreeSpec,
        "PropSpec" -> PropSpec,
        "FeatureSpec" -> FeatureSpec,
        //"JUnitSpec" -> JUnit4Spec,
        "RefSpec" -> RefSpec,

        //"JUnit3" -> JUnit3Spec,
        //"JUnit4" -> JUnit4Spec,
        //"JUnitSuite" -> JUnit4Spec,
        //"JUnit3Suite" -> JUnit3Spec,

        //"ScalaCheck" -> ScalaCheckSpec,

        //"Specs2" -> Specs2Spec,

        "Unknown" -> Unknown
    )

    def get(spec: String)= explorers.get(spec)
}