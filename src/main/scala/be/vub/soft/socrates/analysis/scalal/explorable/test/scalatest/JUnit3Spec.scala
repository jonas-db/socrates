package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.internal.semanticdb.TextDocument
import scala.meta.{Defn, Term, Tree}

object JUnit3Spec extends Explorable {

    override def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Defn.Def(_, Term.Name(fn), _, _, _, _) if fn.startsWith("test") => ScalaTestCase(test, document)
    })
}
