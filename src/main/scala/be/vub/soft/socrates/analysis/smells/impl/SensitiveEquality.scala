package be.vub.soft.socrates.analysis.smells.impl

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.analysis.smells.TestCaseTestSmell
import be.vub.soft.socrates.semantic.ProjectDatabase
import be.vub.soft.socrates.util.Debuggable

import scala.meta.{Term, Tree}

object SensitiveEquality extends TestCaseTestSmell with Debuggable {

    val assertions: PartialFunction[Tree, Tree] = {
        case a@Term.Apply(Term.Name(t), _) if t.equals("assert")        => a
        case a@Term.Apply(Term.Name(t), _) if t.equals("assertResult")  => a
        case a@Term.Apply(Term.Name(t), _) if t.equals("assume")        => a
        case a@Term.Apply(Term.Name(t), _) if t.equals("cancel")        => a
        case a@Term.Apply(Term.Name(t), _) if t.equals("fail")          => a
    }

    def hasToString(tree: Tree): Boolean = {
        tree.collect({
            case t@Term.Select(_, Term.Name("toString")) => t
        }).nonEmpty
    }

    override def verify(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb:ProjectDatabase): Boolean = {
        testCase.ast.collect(assertions).exists(a => hasToString(a))
    }

    override val description: String = "SensitiveEquality"
}
