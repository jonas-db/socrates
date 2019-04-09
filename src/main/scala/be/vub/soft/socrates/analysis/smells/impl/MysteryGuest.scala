package be.vub.soft.socrates.analysis.smells.impl

import java.io.File
import java.nio.file.Paths

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.analysis.smells.TestCaseTestSmell
import be.vub.soft.socrates.util.Debuggable

import scala.meta.{Init, Lit, Term, Tree, Type}

/*
    External resource used in a test case.
 */
object MysteryGuest extends TestCaseTestSmell with Debuggable {

    def existsNonLiteral(args: List[Tree]) = args.exists({
        case _: Lit.String => false
        case _ => true
    })

    def collect(ast: Tree, lookup: (Tree, String) => Boolean): List[String] = ast.collect({
        case Term.Apply(Term.Select(_, m@Term.Name(_)), _) if lookup(m, "java/sql/DriverManager#getConnection().") => {
            "DriverManager#getConnection" //TODO: fix hack
        }
        case Term.Apply(Term.Select(_, m@Term.Name(_)), List(arg)) if lookup(m, "scala/io/Source.fromFile().") => arg match {
            case Lit.String(s) => s
            case _ => arg.toString()
        }
        case Term.Apply(Term.Select(_, m@Term.Name("getResourceAsStream")), List(arg)) if lookup(m, "java/lang/Class#getResourceAsStream().") => arg match {
            case Lit.String(s) => s
            case _ => arg.toString()
        }
        case Term.Apply(Term.Select(_, m@Term.Name("getResource")), List(arg)) if lookup(m, "java/lang/Class#getResource().") => arg match {
            case Lit.String(s) => s
            case _ => arg.toString()
        }
        case Term.Apply(Term.Select(_, m@Term.Name("getResourceAsStream")), List(arg)) if lookup(m, "java/lang/ClassLoader#getResourceAsStream().") => arg match {
            case Lit.String(s) => s
            case _ => arg.toString()
        }
        case Term.Apply(Term.Select(_, m@Term.Name("getResource")), List(arg)) if lookup(m, "java/lang/ClassLoader#getResource().") => arg match {
            case Lit.String(s) => s
            case _ => arg.toString()
        }
        case Term.Apply(Term.Select(_, m@Term.Name(_)), args: List[Term]) if lookup(m, "java/nio/file/Paths.get().") && !existsNonLiteral(args) => {
            args.map(x => x.toString()).mkString(File.separator)
        }
        case Term.New(Init(t: Type.Name, _, List(List(Lit.String(path))))) if lookup(t, "java/io/FileInputStream#") => path
        case Term.New(Init(t: Type.Name, _, List(List(Lit.String(path))))) if lookup(t, "java/io/File#") => path
        case Term.New(Init(t: Type.Name, _, List(List(Lit.String(parent), Lit.String(child))))) if lookup(t, "java/io/File#") => Paths.get(parent, child).toAbsolutePath.toString
    })

    override def verify(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean = {
        val ast = testCase.ast

        def lookup(t: Tree, ss: String) = SemanticDB
            .symbolInformationGlobal(t, testCase.document, pdb.symTab)
            .exists(s => s.symbol.equals(ss))

        val result = collect(ast, lookup).nonEmpty

        result
    }

    override val description: String = "MysteryGuest"
}
