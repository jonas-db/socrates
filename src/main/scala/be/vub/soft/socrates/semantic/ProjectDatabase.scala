package be.vub.soft.socrates.semantic

import java.io.{File, PrintWriter}
import java.net.URLClassLoader
import java.nio.file.Paths

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{DirectoryFileFilter, TrueFileFilter}
import be.vub.soft.socrates.semantic

import scala.meta.{Tree, _}
import scala.meta.internal.semanticdb.{ClassSignature, Locator, SymbolInformation, TextDocument, TypeRef}
import scala.meta.internal.symtab.GlobalSymbolTable
import scala.meta.io.Classpath

object ProjectDatabase {

    def compute(root: String, rtPath:String, paths: String*): ProjectDatabase = {
        var trees: Map[String, Tree] = Map.empty
        var documents: Map[String, TextDocument] = Map.empty
        var definitions: Map[String, String] = Map.empty
        var classHierarchy: Map[SymbolInformation, Set[SymbolInformation]] = Map.empty

        val local = new File(root)

        //TODO: https://stackoverflow.com/questions/12985814/how-to-reliably-locate-javas-rt-jar-or-equivalent

        val externalJars = if(paths.isEmpty) List() else paths.flatMap(p => FileUtils.listFiles(new File(p), Array("jar"), true).toArray(new Array[File](0))
            .toList
            .map(f => Classpath(f.getAbsolutePath))
            .reduceOption(_ ++ _))

        val jars = FileUtils.listFiles(local, Array("jar"), true).toArray(new Array[File](0))
            .toList
            .map(f => Classpath(f.getAbsolutePath))
            .reduceOption(_ ++ _)

        val classes = FileUtils.listFilesAndDirs(local, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY).toArray(new Array[File](0))
            .toList
            .filter(p => p.isDirectory && !p.getAbsolutePath.contains(".sbt") && p.getAbsolutePath.contains("target") && p.getAbsolutePath.contains("classes"))
            .map(f => Classpath(f.getAbsolutePath))
            .reduceOption(_ ++ _)

        val all = (externalJars ++ jars ++ classes ++ List(Classpath(rtPath))).reduceOption(_ ++ _).getOrElse(Classpath(""))
        val symtab = GlobalSymbolTable(all)

        Locator.apply(Paths.get(root))((path, db) => db.documents.foreach({
            case TextDocument(_, uri, "", "", _, _, _, _, _) => () // skip diagnostics files
            case document@TextDocument(_, uri, _, _, _, _, _, _, _) => {
                val file = new File(Paths.get(root, uri).toUri)

                val ast = if(file.exists()) file.parse[Source].getOrElse(Source(List())) else Source(List())

                val methodDefinitions: List[Defn.Def] = ast.collect({
                    case d: Defn.Def => d
                })
                // Only class level methods
                val classLevelMethods = methodDefinitions.filter(m => {
                    val isLocal = SemanticDB.symbolInformationGlobal(m.name, document, symtab).map(si => si.isLocal)

                    isLocal.nonEmpty && !isLocal.get
                }).toSet

                //TODO: not every symbol has a corresponding AST (eg case class fields become methods)

                /*
                 * Class Hierarchy
                 */

                // Types
                val types = document.symbols.filter({ case s: SymbolInformation => s.isType })
                types.foreach(si => {
                    val uid = si.symbol

                    definitions = definitions + (uid -> uri)
                })

                // Classes, Object, Trait
                val dfs = document.symbols.filter({ case s: SymbolInformation => s.isClass || s.isObject || s.isTrait })
                dfs.foreach(si => {
                    val uid = si.symbol

                    definitions = definitions + (uid -> uri)

                    if(!classHierarchy.contains(si)) {
                        classHierarchy = classHierarchy + (si -> Set())
                    }

                    val ClassSignature(_, parents, _, _) = si.signature

                    val p = if(si.signature.isEmpty)
                        Seq.empty
                    else
                        parents
                            .flatMap({
                                case TypeRef(_, symb, _) => {
                                    definitions = definitions + (symb -> uri)
                                    //println(symb)

                                    val local = document.symbols.find(x => x.symbol.equals(symb))
                                    //if(local.isEmpty) println(s"nonlocal: $symb, globaldef=${symtab.info(symb).nonEmpty}")
                                    if(local.isEmpty) (try { symtab.info(symb) } catch { case _: Throwable => None }) else local
                                }
                                case _ => None
                            })
                            .map(symbol => classHierarchy.get(symbol) match {
                                // We've already seen this class (probably multiple classes inheriting from this class)
                                case Some(children) => {
                                    // We should never encounter this class definition more than once, so we can always add it directly
                                    //assert(children.find({ case ClassTree(name, _) => name.equals(x.symbol) }).nonEmpty, s"This class: ${x.symbol} was already added the children: $children")
                                    (symbol -> (children + si))
                                }
                                // First time we encounter this class, so add it and change the children to include this class definition
                                case None => (symbol -> Set(si))
                            })

                    classHierarchy = classHierarchy ++ p.toMap
                })

                trees = trees + (uri -> ast)
                documents = documents + (uri -> document)
            }
        }))

        ProjectDatabase(trees, documents, definitions, semantic.ClassHierarchy(classHierarchy), symtab)
    }

}

case class ProjectDatabase(trees: Map[String, Tree] = Map.empty,
                           documents: Map[String, TextDocument] = Map.empty,
                           definitions: Map[String, String] = Map.empty,
                           ch: ClassHierarchy = ClassHierarchy(),
                           symTab: GlobalSymbolTable) {

    def lookupGlobally(symbol: String) = try {
        symTab.info(symbol)
    } catch {
        case _: Throwable => None
    }

    def toFile() = {

        val ClassHierarchy(classHierarchy) = ch

        val information =
            s"""
            Trees: ${trees.size}\n
            Documents: ${documents.size}\n
            Definitions: ${definitions.size}\n
            ClassHierarchy: ${classHierarchy.size}\n
            """
        println(information)

        var defsPerFile: Map[String, Set[String]] = Map.empty

        definitions.foreach({
            case (k, v) =>
                val fileExists = defsPerFile.isDefinedAt(v)

                if(!fileExists)
                    defsPerFile = defsPerFile + (v -> Set(k))
                else
                    defsPerFile = defsPerFile + (v -> (defsPerFile(v) + k))
        })

        val prettyDefinitions = defsPerFile.map({
            case (k, v) => s"$k:\n${v.mkString("\n")}"
        }).mkString("\n\n")

        val definitionsWriter = new PrintWriter(new File("./definitions.txt"))
        definitionsWriter.write(prettyDefinitions)
        definitionsWriter.close()

        def strip(name: String) = name.substring(name.indexOf("@") + 1)

        def filter(fn: SymbolInformation => Boolean) = classHierarchy.keys.toList.filter(d => {
            //val s = documents.getOrElse(definitions.getOrElse(d, ""), TextDocument()).symbols.find(s => s.symbol.equals(strip(d))).get
            fn(d)
        }).map(x => "\"" + x.symbol + "\"" + "\n").mkString(" ")

        val classes = filter(s => s.isClass && !s.isAbstract && !s.isCase)
        val abstractClasses = filter(s => s.isClass && s.isAbstract && !s.isCase)
        val caseClasses = filter(s => s.isClass && !s.isAbstract && s.isCase)
        val objects = filter(s => s.isObject)
        val traits = filter(s => s.isTrait)

        val nodes = classHierarchy.flatMap({
            case (k, parents) => parents.map(parent => "\"" + parent.symbol + "\"" + " -> " + "\"" + k.symbol + "\"" + "\n")
        }).toList.mkString(" ")

        val classesOutput = if(classes.nonEmpty) s"node [shape = circle, style=filled, color=blue]; $classes;" else ""
        val abstractClassesOutput = if(abstractClasses.nonEmpty) s"node [shape = circle, style=filled, color=lightblue]; $abstractClasses;" else ""
        val caseClassesOutput = if(caseClasses.nonEmpty) s"node [shape = circle, style=filled, color=cyan]; $caseClasses;" else ""
        val objectsOutput = if(objects.nonEmpty) s"node [shape = circle, style=filled, color=gray]; $objects;" else ""
        val traitsOutput = if(traits.nonEmpty) s"node [shape = circle, style=filled, color=green]; $traits;" else ""

        val classHierarchyDOT = s"""digraph G {
                                   | size=50
                                   | $classesOutput
                                   | $abstractClassesOutput
                                   | $caseClassesOutput
                                   | $objectsOutput
                                   | $traitsOutput
                                   | $nodes
                                   |}""".stripMargin

        val classHierarchyWriter = new PrintWriter(new File("./classHierarchy.dot"))
        classHierarchyWriter.write(classHierarchyDOT)
        classHierarchyWriter.close()
    }

}


