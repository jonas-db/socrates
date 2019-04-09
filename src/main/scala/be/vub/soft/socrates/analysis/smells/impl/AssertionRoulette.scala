package be.vub.soft.socrates.analysis.smells.impl

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.analysis.smells.TestCaseTestSmell
import be.vub.soft.socrates.semantic.ProjectDatabase
import be.vub.soft.socrates.util.Debuggable

import scala.meta.{Lit, Term, Tree}

/*
    One or more assertions without an explanation or clue.
    http://www.scalatest.org/user_guide/using_assertions
 */
object AssertionRoulette extends TestCaseTestSmell with Debuggable {

    val assertions: PartialFunction[Tree, Tree] = {
        case a@Term.Apply(Term.Name(t), args) if t.equals("assert") && args.size == 1 => a
        case a@Term.Apply(Term.Name(t), args) if t.equals("assertResult") && args.size == 1 => a
        case a@Term.Apply(Term.ApplyType(Term.Name(t), _), _) if t.equals("assertThrows") => a

        case a@Term.Apply(Term.Name(t), args) if t.equals("cancel") && args.size == 0 => a
        case a@Term.Apply(Term.Name(t), args) if t.equals("assume") && args.size == 1 => a
        case a@Term.Apply(Term.Name(t), args) if t.equals("fail") && args.size == 0 => a
    }

    def hasWithClue(tree: Tree): Boolean = tree.parent match {
        case Some(Term.Apply(Term.Apply(Term.Name("withClue"), List(Lit.String(_))), List(Term.Block(_)))) => true
        case Some(parent) => hasWithClue(parent)
        case None => false
    }

    override def verify(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb:ProjectDatabase): Boolean = {
        testCase.ast.collect(assertions).filterNot(a => hasWithClue(a)).size > 1
    }

    override val description: String = "AssertionRoulette"
}
