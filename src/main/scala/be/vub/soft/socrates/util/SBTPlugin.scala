package be.vub.soft.socrates.util

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import com.intellij.openapi.project.Project

object SBTPlugin {

    def install(project: Project) = {
        val path = Paths.get(project.getBasePath, "project", "SemanticdbCommandPlugin.scala")
        Files.write(path, src.getBytes(StandardCharsets.UTF_8))
    }

    private val src =
        """
          |// To install as a global sbt plugin, place this file in:
          |//   ~/.sbt/1.0/plugins/Semanticdb.scalac
          |//   ~/.sbt/0.13/plugins/Semanticdb.scala
          |// To use it, run `sbt semanticdb`. This will first enable the semanticdb
          |// compiler plugin for modules that have a compatible scala version and then
          |// trigger compilation for those projects.
          |import sbt._
          |import sbt.Keys._
          |import sbt.plugins.JvmPlugin
          |
          |object SemanticdbCommandPlugin extends AutoPlugin {
          |    val ScalametaVersion = "4.1.0"
          |    override def requires: Plugins = JvmPlugin
          |    override def trigger: PluginTrigger = allRequirements
          |    val V: Map[(Long, Long), String] = Map(
          |        (2L -> 10L) -> "2.11.12",
          |        (2L -> 11L) -> "2.11.12",
          |        (2L -> 12L) -> "2.12.8"
          |    )
          |    def relevantProjects(state: State): Seq[(ProjectRef, String)] = {
          |        val extracted = Project.extract(state)
          |
          |        for {
          |            p <- extracted.structure.allProjectRefs
          |            version <- scalaVersion.in(p).get(extracted.structure.data).toList
          |            (major, minor) <- CrossVersion.partialVersion(version).toList
          |            fullVersion <- V.get(major.toLong -> minor.toLong).toList
          |        } yield p -> fullVersion
          |    }
          |
          |    val compileAll = taskKey[Unit]("compile all projects in test+compile configs")
          |    override def globalSettings = List(
          |        aggregate.in(compileAll) := false,
          |        compileAll := Def.taskDyn {
          |            val refs = relevantProjects(state.value).map(_._1)
          |            println(refs.toList)
          |            val filter = ScopeFilter(
          |                projects = inProjects(refs: _*),
          |                configurations = inConfigurations(Compile, Test))
          |            compile.all(filter)
          |        }.value,
          |        commands += Command.command("semanticdb") { s =>
          |            val extracted = Project.extract(s)
          |            val refs = List.newBuilder[ProjectRef]
          |            val settings: Seq[Setting[_]] = for {
          |                (p, fullVersion) <- relevantProjects(s)
          |                setting <- List(
          |                    scalaVersion.in(p) := fullVersion,
          |                    scalacOptions.in(p) += "-Yrangepos",
          |                    scalacOptions.in(p) += "-P:semanticdb:synthetics:on",
          |                    libraryDependencies.in(p) += compilerPlugin(
          |                        "org.scalameta" % "semanticdb-scalac" % ScalametaVersion cross CrossVersion.full)
          |                )
          |            } yield setting
          |            val installed = extracted.append(settings, s)
          |            "compileAll" ::
          |                installed
          |        }
          |    )
          |}
        """.stripMargin

}
