package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.{Defn, Tree}
import scala.meta.internal.semanticdb.TextDocument

object RefSpec extends Explorable {

    def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Defn.Def(_, name, _, _,_, body) if name.pos.text.contains("`") =>
            ScalaTestCase(test, document, collectParamFixture(List(body)), collectLoanFixtures(body), collectFixtureContexts(List(body)))
    })

}
