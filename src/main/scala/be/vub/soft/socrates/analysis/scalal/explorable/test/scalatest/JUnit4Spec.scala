package be.vub.soft.socrates.analysis.scalal.explorable.test.scalatest

import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase}

import scala.meta.internal.semanticdb.TextDocument
import scala.meta.{Defn, Init, Mod, Term, Tree, Type}

/*
    http://doc.scalatest.org/3.0.1/#org.scalatest.junit.JUnitSuite

    eg:
    https://github.com/anascotti/awesome-spring-scala/blob/master/src/test/scala/com/awesome/springScala/AwesomeTest.scala
    https://github.com/anascotti/awesome-spring-scala/blob/master/src/test/scala/com/awesome/springScala/AwesomeTest.scala
 */
object JUnit4Spec extends Explorable {

    override def explore(ast: Tree, document: TextDocument): List[ScalaTestCase] = ast.collect({
        case test@Defn.Def(List(Mod.Annot(Init(Type.Name(name), _, _))), Term.Name(_), _, _, _, _) if name.equals("Test") => ScalaTestCase(test, document)
    })
}
