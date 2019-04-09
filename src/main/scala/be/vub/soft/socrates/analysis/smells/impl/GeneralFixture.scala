package be.vub.soft.socrates.analysis.smells.impl

import be.vub.soft.socrates.analysis.scalal.explorable.{ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.analysis.smells.TestClassTestSmell
import be.vub.soft.socrates.util.Debuggable

import scala.meta.internal.semanticdb.SymbolInformation
import scala.meta.{Defn, Term, Tree}

object GeneralFixture extends TestClassTestSmell with Debuggable {

    override def verify(testSuite: ScalaTestClass, pdb: ProjectDatabase): Boolean = {
        val isObject = (t: Tree) => SemanticDB.symbolInformationGlobal(t, testSuite.document, pdb.symTab).exists(s => s.isObject)


        val beforeEach = verify(testSuite, ast => collectBeforeEach(ast), isObject, pdb)
        val before = verify(testSuite, ast => collectBefore(ast), isObject, pdb)
        val beforeAll = verify(testSuite, ast => collectBeforeAll(ast), isObject, pdb)

        debug("beforeEach="+beforeEach)
        debug("before="+before)
        debug("beforeAll="+beforeAll)

        val result = beforeEach || before || beforeAll

        debug("result="+result+" for "+testSuite.document.uri)

        result
    }

    /***
      * Trait BeforeAndAfterEach, BeforeAndAfterAll, BeforeAndAfter
      */

    def collectBeforeEach(ast: Tree) = {
        ast.collect({case t@Defn.Def(_, Term.Name("beforeEach"), _, _, _, body) => body}).headOption
    }

    def collectBeforeAll(ast: Tree) = {
        ast.collect({case t@Defn.Def(_, Term.Name("beforeAll"), _, _, _, body) => body}).headOption
    }

    def collectBefore(ast: Tree) = {
        ast.collect({case t@Term.Apply(Term.Name("before"), _) => t}).headOption
    }

    def collectInstanceTerms(body: Tree, isObject: Tree => Boolean) = {
        body.collect({
            case Term.Assign(t@Term.Name(_), _) => t
            case Term.Apply(Term.Select(t@Term.Name(n), _), _) if !n.equals("super") && !isObject(t) => t
            case Term.Apply(Term.Select(Term.Select(t@Term.Name(n), _), _), _) if !n.equals("super") && !isObject(t)  => t
            case Term.ApplyInfix(t@Term.Name(n), _, _, _) if !n.equals("super") && !isObject(t) => t
        }).toSet
    }

    def verify(testSuite: ScalaTestClass, test: Tree => Option[Tree], isObject: Tree => Boolean, pdb: ProjectDatabase) = test(testSuite.ast) match {
        case Some(dfn) => {
            // variables from before/beforeAll/beforeEach
            val fixtureVariables = collectInstanceTerms(dfn, isObject)

            debug("fixtureVariables="+fixtureVariables.size)
            debug(fixtureVariables.mkString("\n"))

            val vars: Set[SymbolInformation] = fixtureVariables.flatMap(t => SemanticDB.symbolInformationGlobal(t, testSuite.document, pdb.symTab))

            debug("vars="+vars.size)
            debug(vars.mkString("\n"))

            debug("about test case=")
            val result = testSuite.tests.exists({
                case tc: ScalaTestCase => {
                    val testAST = tc.ast
                    val localVars: Set[SymbolInformation] = testAST
                        .collect({ case t: Term.Name => t })
                        .flatMap(t => SemanticDB.symbolInformationGlobal(t, tc.document, pdb.symTab))
                        .toSet

                    debug("localVars="+localVars.size)
                    debug(localVars.mkString("\n"))

                    import scala.meta.internal.semanticdb.Scala._

                    val filtered: Set[SymbolInformation] = localVars.filter(l => vars.exists(ll => {
                        ll.equals(l) || (ll.symbol.contains("_=") && ll.displayName.substring(0, ll.displayName.length - 2).equals(l.displayName) && ll.symbol.owner.equals(l.symbol.owner))
                    }))

                    debug("filtered="+filtered.size)
                    debug(filtered.mkString("\n"))
                    debug("filtered="+vars.size)

                    filtered.size < vars.size // if the intersection is smaller than the vars in before, then we didn't use a variable
                }
            })

            result
        }
        case None => false
    }

    override val description: String = "GeneralFixture"
}
