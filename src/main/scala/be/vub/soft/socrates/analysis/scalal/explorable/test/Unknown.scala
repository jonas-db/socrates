package be.vub.soft.socrates.analysis.scalal.explorable.test

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.Tree
import scala.meta.internal.semanticdb.TextDocument

object Unknown extends Explorable {

    def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = List.empty

}
