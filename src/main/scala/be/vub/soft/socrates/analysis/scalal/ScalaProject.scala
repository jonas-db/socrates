package be.vub.soft.socrates.analysis.scalal

import java.io.File

import be.vub.soft.socrates.analysis.{ScalaTestSuiteResult, _}
import be.vub.soft.socrates.analysis.scalal.explorable.test.Spec
import be.vub.soft.socrates.analysis.scalal.explorable.{Explorable, ScalaTestCase, ScalaTestClass}
import be.vub.soft.socrates.analysis.smells.impl.GeneralFixture
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.util.{Debuggable, Utilities}

import scala.meta.{Defn, Pkg, Source, Stat, Term, Tree, Type}
import scala.meta.internal.semanticdb.TextDocument

case class ScalaProject(root: String,
                        productionClasses: List[File], testClasses: List[File],
                        pdb: ProjectDatabase, update: Int => Unit, updateString: String => Unit) extends ScalaAnalyzable {

    override def analyze(debugger: Debuggable): ScalaProjectResult = {

        val ProjectDatabase(trees, documents, _, _, _) = pdb
        val accompanying: Map[File, File] = Utilities.findAccompanying(productionClasses, testClasses)

        debugger.debug(s"Analyzing project: tests=${testClasses.size}, production=${productionClasses.size}")

        val productionComponents = productionClasses
            .map(file => file.getAbsolutePath.substring(root.length + 1))
            .filter(uri => trees.get(uri).nonEmpty && documents.get(uri).nonEmpty)
            .flatMap(uri => {
                val Source(stats) = trees(uri)
                val realStats: List[Stat] = stats match {
                    case List(Pkg(_, s)) => s
                    case _ => stats
                }

                realStats
                    .filter({
                        case _: Defn.Object | _: Defn.Class | _: Defn.Trait => true
                        case _ => false // Imports
                    })
                    .map(ast => {
                        val sast = ast.toString()
                        NonEmptyScalaProductionResult(sast, sast.lines.size)
                    })
            })

        var done: Double = 0
        val testComponents = testClasses
            .map(file => file.getAbsolutePath.substring(root.length + 1))
            .filter(uri => trees.get(uri).nonEmpty && documents.get(uri).nonEmpty)
            .flatMap(uri => {
                val Source(stats) = trees(uri)
                val realStats: List[Stat] = stats match {
                    case List(Pkg(_, s)) => s
                    case _ => stats
                }
                val document = documents(uri)

                val res = realStats
                    .filter({
                        case _: Defn.Object | _: Defn.Class | _: Defn.Trait => true
                        case _ => false // Imports
                    })
                    .map(ast => {
                        //val document = database.get(file.getAbsolutePath.substring(root.length) + 1)
                        val definition: Tree = ast
                        val defName = ast match {
                            case Defn.Class(_, Type.Name(name), _, _, _) => name
                            case Defn.Object(_, Term.Name(name), _) => name
                            case Defn.Trait(_, Type.Name(name), _, _, _) => name
                        }
                        val spec = Spec.findTestStyle(definition, document, pdb)
                        val implementation = spec._1
                        val style = spec._2

                        updateString(s"Analyzing ${document.uri}")

                        debugger.debug(s"${document.uri}: $defName")
                        debugger.debug("implementation=" + implementation + ",style=" + style)

                        val productionClass: Option[(Tree, TextDocument)] = accompanying
                            .find({ case (k, _) => k.getAbsolutePath.endsWith(uri) })
                            .flatMap({ case (_, p) => {
                                val puri = p.getAbsolutePath.substring(root.length + 1)
                                if (trees.get(puri).nonEmpty && documents.get(puri).nonEmpty) Some((trees(puri), documents(puri))) else None
                            }
                            })

                        debugger.debug(s"productionClass: ${productionClass.getOrElse((null, TextDocument()))._2.uri}")

                        val tests = Explorable.get(style).map(explorable => explorable.explore(definition, document)).getOrElse(List.empty[ScalaTestCase])
                        val hasGeneralFixture = GeneralFixture.collectBefore(ast).isDefined || GeneralFixture.collectBeforeAll(ast).isDefined || GeneralFixture.collectBeforeEach(ast).isDefined

                        ScalaTestClass(definition, document, productionClass, tests, style, implementation, hasGeneralFixture).analyze(debugger, pdb)
                    })

                    done = done + 1

                    val status: Double = (done / testClasses.size) * 100.0
                    update(status.toInt)

                    res
            }).filter({
                case r: NonEmptyScalaTestSuiteResult => !r.style.equals("Unknown")
                case _ => false
            })

        val productionLOCs = productionClasses.map(x => scala.io.Source.fromFile(x).getLines.size).sum
        val testLOCs = testClasses.map(x => scala.io.Source.fromFile(x).getLines.size).sum
        val testComponentsLOCs = testComponents.map({
            case r: NonEmptyScalaTestSuiteResult => r.loc
            case _ => 0
        }).sum
        val productionComponentsLOCs = productionComponents.map({
            case r: NonEmptyScalaProductionResult => r.loc
            case _ => 0
        }).sum

        NonEmptyScalaProjectResult(
            productionClasses.size, productionComponents.size, productionLOCs, productionComponentsLOCs, productionComponents,
            testClasses.size, testComponents.size, testLOCs, testComponentsLOCs, testComponents)
    }

}