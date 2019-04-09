package be.vub.soft.socrates.analysis.smells.impl

import be.vub.soft.socrates.analysis.scalal.explorable.ScalaTestClass
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.analysis.smells.TestClassTestSmell
import be.vub.soft.socrates.util.{Debuggable, Logger}

import scala.meta.internal.semanticdb.SymbolInformation
import scala.meta.{Defn, Term, Tree}

object LazyTest extends TestClassTestSmell with Debuggable {

    val logger = Logger("LazyTest")

    def verify(testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean = testSuite.production match {
        case None => false
        case Some((prodAst, prodDoc)) => {
            val testCases = testSuite.tests
            val testDoc = testSuite.document

            // Get all methods defined in the production class
            // TODO: method calls in superclasses of the production class
            val methodsInProductionClass: List[SymbolInformation] = prodAst.collect({
                case Defn.Def(_, fn: Term.Name, _, _, _ ,_) => SemanticDB.symbolInformationGlobal(fn, prodDoc, pdb.symTab)
            }).flatten

            // Print
            debug("Production methods: "+methodsInProductionClass.map(_.symbol).mkString(","))

            // Get for each test the method calls
            val methodCalls: List[Set[SymbolInformation]] = testCases
                .map(x => x.ast)
                .map(ast => ast.collect({ case t: Term.Name => t }))
                .map(tt => tt.flatMap(t => SemanticDB.symbolInformationGlobal(t, testDoc, pdb.symTab)))
                .map(l => l.filter(m => methodsInProductionClass.contains(m)).toSet)

            // Print
            debug("Test methods: "+methodCalls.map(_.map(_.symbol)).mkString(","))

            var called = Set.empty[SymbolInformation]

            // Iterate through each test and see if one or more methods are already called in another test
            // (ie intersection is non empty), once a method is already in the set we have a lazy test.
            val found = methodCalls.exists(set => {
                val methodAlreadyCalled = called.intersect(set).nonEmpty
                called = called ++ set

                methodAlreadyCalled
            })

            debug(s"Lazy Test= $found")

            found
        }
    }

    override val description: String = "LazyTest"
}

