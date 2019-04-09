package be.vub.soft.socrates.analysis

import java.nio.file.Paths
import java.util.concurrent.FutureTask

import be.vub.soft.socrates.analysis.scalal.ScalaRepository
import be.vub.soft.socrates.util.Logger
import com.intellij.openapi.project.Project

/*
    Entry point to analyze a given project.

    @author Jonas De Bleser
    @email jonas.de.bleser@vub.be
 */

object Socrates {

    val PROCESS_EXCEPTION: Int      = 7331
    val PROCESS_TIMEOUT: Int        = 1337
    val SKIP_COMPILE                = 1337123
    val CLONE_TIMEOUT: Int          = 15 * 60 // 15min
    val COMPILE_TIMEOUT: Int        = 30 * 60 // 30min
    var DEBUG: Boolean              = false
    val logger                      = Logger("Main")

    type SocratesResult = (List[TestClassProjection], List[TestCaseProjection])

    def analyze(project: Project, sbtHome: String, sbtOptions: String, update: Int => Unit, updateString: String => Unit, rtPath: String, paths: String*): FutureTask[SocratesResult] = {
        logger.info("Start analysis.")

        val logs = Paths.get(project.getBasePath, "logs")

        if(!logs.toFile.exists()) logs.toFile.mkdirs()

        val future = new FutureTask[SocratesResult](() => {
            val fileName = project.getName.replaceAll("/", "-").replaceAll(" ", "")
            val log = Paths.get(logs.toString, fileName + "-log.txt").toFile
            val result = ScalaRepository(project, log, sbtHome, sbtOptions, update, updateString, rtPath, paths.toList).analyze()

            logger.info("Finished analysis.")

            (getScalaTestClasses(result), getScalaTestCases(result))
        })

        future
    }

    private def getScalaTestClasses(result: ScalaProjectResult): List[TestClassProjection] = result match {
        case r: NonEmptyScalaProjectResult => r.tests.map({
            case testClass: NonEmptyScalaTestSuiteResult => TestClassProjection(testClass.uri, testClass.generalFixture, testClass.lazyTest)
        })
        case _ => List()
    }

    private def getScalaTestCases(result: ScalaProjectResult): List[TestCaseProjection] = result match {
        case r: NonEmptyScalaProjectResult => r.tests.flatMap({
            case testClass: NonEmptyScalaTestSuiteResult => testClass.tests.map({
                case t: NonEmptyScalaTestCaseResult =>
                    val idx = t.ast.indexOf("{")
                    TestCaseProjection(t.ast.substring(0, if(idx == -1) t.ast.length else idx) + "...",t.assertionRoulette, t.sensitiveEquality, t.eagerTest, t.loanTestArgFixture, t.oneTestArgFixture, t.contextFixture, t.resourceOptimism, t.mysteryGuest)
            })
            case _ => List.empty
        })
        case _ => List()
    }

}
