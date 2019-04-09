package be.vub.soft.socrates.analysis.scalal

import java.io.File
import java.nio.file.Paths

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.{DirectoryFileFilter, FalseFileFilter}
import be.vub.soft.socrates.analysis.{EmptyScalaProjectResult, Socrates, ScalaProjectResult}
import be.vub.soft.socrates.semantic.ProjectDatabase
import be.vub.soft.socrates.util.{Debuggable, Logger, SBTPlugin}
import com.intellij.openapi.project.Project

case class ScalaRepository(project: Project, log: File,
                           sbtHome: String, sbtOptions: String,
                           update: Int => Unit, updateString: String => Unit,
                           rtPath: String,
                           paths: List[String]) extends Debuggable {

    private val full_name = project.getName
    private val logger = Logger(full_name)
    private val local = new File(project.getBasePath)

    def timeoutProcess(cmd: String, pwd: File, timeout: Int): Int = {
        import scala.concurrent._
        import ExecutionContext.Implicits.global
        import scala.sys.process._

        val processLogger = new FileProcessLogger(log)
        val sbtProcess = Process(cmd, pwd).run(processLogger) // start asynchronously
        val f = Future(blocking(sbtProcess.exitValue())) // wrap in Future

        try {
            val code = Await.result[Int](f, duration.Duration(timeout, "sec"))
            processLogger.close()
            code
        } catch {
            case _: TimeoutException => {
                logger.info(s"process timeout: $cmd")
                sbtProcess.destroy()
                Socrates.PROCESS_TIMEOUT
            }
            case _: Throwable => {
                sbtProcess.destroy()
                Socrates.PROCESS_EXCEPTION
            }
        } finally {
            processLogger.close()
        }
    }

    def compile(): ScalaProjectResult = {
        val buildSbt = FileUtils
            .listFiles(local, Array("sbt", "scala", "properties"), true)
            .toArray(new Array[File](0)).toList.find(f => {
                val name = f.getName.toLowerCase

                name.equals("build.sbt") || name.equals("build.scala") || name.equals("build.properties")
            })

        if(buildSbt.isEmpty)
        {
            logger.info("Found no build.sbt")

            EmptyScalaProjectResult("NO_SBT_PROJECT")
        }
        else
        {
            logger.info("Found build.sbt")

            val sbtCmd = s"$sbtHome $sbtOptions semanticdb"

            try {
                logger.info(s"Compiling...")
                updateString("Compiling...")

                val exitCode2 = timeoutProcess(sbtCmd, local, Socrates.COMPILE_TIMEOUT)

                if(exitCode2 == 0) {
                    val sdb = FileUtils.listFiles(local, Array("semanticdb"), true).toArray(new Array[File](0)).nonEmpty

                    if(sdb) {
                        explore()
                    } else {
                        EmptyScalaProjectResult("NO_SDB_GENERATED")
                    }

                } else {
                    EmptyScalaProjectResult(s"SBT_FAILED_$exitCode2")
                }
            } catch {
                case e: Throwable =>
                    logger.info(e.getMessage)
                    EmptyScalaProjectResult("Failed compilation.")
            }
        }
    }


    def explore(): ScalaProjectResult = {
        logger.info("Exploring...")
        updateString("Exploring...")

        var sourceDirs: List[File] = FileUtils.listFilesAndDirs(local, FalseFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY).toArray(new Array[File](0)).toList.filter({
            case src => src.getName.endsWith("src") && !src.getAbsolutePath.contains("/.sbt") && !src.getAbsolutePath.contains("META-INF")
        })

        // No src folders found: we assume the root folder is the src folder
        if (sourceDirs.isEmpty) {
            logger.info(s"Did not find src directory for repository $full_name")
            sourceDirs = List(local)
        }

        try
        {
            val mainsAndTests = sourceDirs.map(src => {
                // Main folders
                val ms = FileUtils.listFilesAndDirs(src, FalseFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY).toArray(new Array[File](0)).toList.filter({
                    case src => src.getName.toLowerCase().equals("main")
                })

                // All folders except test as a fallback
                val mss = if (ms.nonEmpty) ms else FileUtils.listFilesAndDirs(src, FalseFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY).toArray(new Array[File](0)).toList.filter({
                    case src => !src.getName.toLowerCase().equals("test")
                })

                // Test folders
                val ts = FileUtils.listFilesAndDirs(src, FalseFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY).toArray(new Array[File](0)).toList.filter({
                    case src => src.getName.toLowerCase().equals("test")
                })

                (mss, ts)
            })

            val mainDirs: List[File] = mainsAndTests.flatMap(_._1)
            val testDirs: List[File] = mainsAndTests.flatMap(_._2)

            val productionClasses: List[File] = mainDirs.flatMap(m => FileUtils.listFiles(m, Array("scala"), true).toArray(new Array[File](0)).toList)
            val testClasses: List[File] = testDirs.flatMap(t => FileUtils.listFiles(t, Array("scala"), true).toArray(new Array[File](0)).toList)

            //TODO: use implicits
            val debugger = new Debuggable {
                override val file: Option[File] = Some(log)
            }

            debugger.debug("Production Classes:")
            productionClasses.foreach(x => debugger.debug(x.getAbsolutePath))
            debugger.debug("Test Classes:")
            testClasses.foreach(x => debugger.debug(x.getAbsolutePath))

            val path = project.getBasePath
            val pdb = ProjectDatabase.compute(path, rtPath, paths: _*)
            ScalaProject(path, productionClasses, testClasses, pdb, update, updateString).analyze(debugger)
        }
        catch
        {
            case e: Exception => {
                e.printStackTrace()
                EmptyScalaProjectResult(e.getMessage)
            }
        }
    }

    def analyze(): ScalaProjectResult = {
        logger.info("Analyzing...")

        try {
            // Install plugin
            SBTPlugin.install(project)

            // Compile project
            compile()

        } catch {
            case e: Throwable => EmptyScalaProjectResult(s"COMPILATION_FAILED: ${e.getMessage}")
        }
    }


}
