package be.vub.soft.socrates.analysis.smells.impl

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.analysis.smells.TestCaseTestSmell
import be.vub.soft.socrates.util.Debuggable

import scala.meta.internal.semanticdb.SymbolInformation
import scala.meta.{Defn, Term}

/*
    At least one method that uses more than one method of the tested class
 */
object EagerTest extends TestCaseTestSmell with Debuggable {

    def verify(testCase: ScalaTestCase, testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean = testSuite.production match {
        case None => false
        case Some((prodAst, prodDoc)) => {
            val testDoc = testCase.document

            debug(s"Checking test class ${testSuite.document.uri} with production class ${prodDoc.uri}")

            // Get all methods defined in the production class
            // TODO: method calls in superclasses of the production class
            val methodsInProductionClass: List[SymbolInformation] = prodAst.collect({
                case Defn.Def(_, fn: Term.Name, _, _, _ ,_) => SemanticDB.symbolInformationGlobal(fn, prodDoc, pdb.symTab)
            }).flatten

            // Print
            debug("Production methods: "+methodsInProductionClass.map(_.symbol).mkString(","))

            // Get for each test the method calls
            val methodCalls: Set[SymbolInformation] = testCase.ast
                .collect({ case t: Term.Name => t })
                .flatMap(t => SemanticDB.symbolInformationGlobal(t, testDoc, pdb.symTab))
                .filter(m => methodsInProductionClass.contains(m))
                .toSet

            // Print
            debug("Test methods: "+methodCalls.map(_.symbol).mkString(","))

            // Iterate through each test and see if one or more methods are called
            val found = methodCalls.size > 1

            debug(s"Eager test= $found")

            found
        }

    }

    override val description: String = "EagerTest"
}
