package be.vub.soft.socrates.analysis.scalal.explorable.test

import be.vub.soft.socrates.analysis.scalal.explorable.{Check, FunctionCheck, InheritanceCheck}
import be.vub.soft.socrates.semantic.{ProjectDatabase, SemanticDB}
import be.vub.soft.socrates.util.Logger

import scala.meta.internal.semanticdb.{SymbolOccurrence, TextDocument}
import scala.meta.{Defn, Init, Template, Tree, Type}

object Spec {

    val logger = Logger("Spec")

    /*
        ScalaTest
    */

    private val scalaTestStyles = Map(
        "FunSpec" -> "org/scalatest",
        "FunSuite" -> "org/scalatest",
        "FlatSpec" -> "org/scalatest",
        "WordSpec" -> "org/scalatest",
        "FreeSpec" -> "org/scalatest",
        "PropSpec" -> "org/scalatest",
        "FeatureSpec" -> "org/scalatest",
        "RefSpec" -> "org/scalatest/refspec",
    ).flatMap({
        case (k, v) =>
            val specVariations = List(k, s"${k}Like", s"Async$k", s"Async${k}Like")
            val semanticSpec = specVariations.map(x => s"$v/$x#" -> InheritanceCheck(k))
            val semanticSpecFixtures = specVariations.map(x => s"$v/fixture/$x#" -> InheritanceCheck(k))

            semanticSpec.toMap ++ semanticSpecFixtures.toMap
    })

    val specs: Map[String, Check] = scalaTestStyles

    def findTestStyle(ast: Tree, doc: TextDocument, pdb: ProjectDatabase): (String, String) = specs.collectFirst({
        case (k, InheritanceCheck(spec)) if checkForSpec(k, ast, doc, pdb) => (k, spec)
        case (k, FunctionCheck(spec, fn)) if checkForSpecWithFunction(ast, fn) => (k, spec)
    }).getOrElse(("Unknown", "Unknown"))

    def checkForSpec(semantic: String, ast: Tree, doc: TextDocument, pdb: ProjectDatabase): Boolean = {

        val inits = (ast match {
            case Defn.Class(_, _, _, _, Template(_, i, _, _)) => i
            case Defn.Object(_, _, Template(_, i, _, _)) => i
            case Defn.Trait(_, _, _, _, Template(_, i, _, _)) => i
        }).map({
            case Init(Type.Select(_, name: Type.Name), _, _) => name
            case Init(Type.Apply(name: Type.Name, _), _, _) => name
            case Init(name: Type.Name, _, _) => name
        })

        // find all symboloccurences for the given inheritance type asts
        val parentTypesTrees: List[SymbolOccurrence] = inits.flatMap(x => doc.occurrences.find({
            case SymbolOccurrence(Some(range), _, _) => SemanticDB.position(range, x.pos)
            case _ => false
        }))

        val semanticSymbol = pdb.lookupGlobally(semantic)

        if(semanticSymbol.isEmpty) {
            // No semantic information about this symbol, so no .jar/classes, so not used in the project
            false
        } else {
            // check whether there exists a superclass that is a subclass of `semantic`
            parentTypesTrees.exists(soc => {
                val rs = pdb.lookupGlobally(soc.symbol)

                if (rs.nonEmpty) {
                    pdb.ch.isSubClass(rs.get, semanticSymbol.get)
                } else {
                    false
                }
            })
        }
    }

    def checkForSpecWithFunction(ast: Tree, fn: Tree => Boolean): Boolean = fn(ast)
}
