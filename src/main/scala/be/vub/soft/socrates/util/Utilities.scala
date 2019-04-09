package be.vub.soft.socrates.util

import java.io.File

object Utilities {

    // Variations of naming conventions, "" means the class name might be the same as the one of the production class
    val additions = List("Test", "Tests", "TC", "TestCase", "Spec", "Specification", "Suite", "Prop", "").map(_.toLowerCase)

    def findAccompanying(productionClasses: List[File], tests: List[File]): Map[File, File] = {

        var mainMapping = Map.empty[String, List[File]]

        productionClasses.foreach(f => {
            val path = f.getAbsolutePath
            val relativePath: String = path.substring(path.indexOf("src/main") + 8, path.length - f.getName.size)

            mainMapping.get(relativePath) match {
                case Some(l) => mainMapping = mainMapping + (relativePath -> (f :: l))
                case None => mainMapping = mainMapping + (relativePath -> List(f))
            }
        })

        tests.map(test => {
            // Get name of file (e.g. BasketTestCase)
            val name = test.getName.replace(".scala", "").toLowerCase

            // Check if te file ends with a variation (e.g. TestCase)
            val add = additions.filter(add => name.endsWith(add)).headOption

            add match {
                // We got a match (e.g. TestCase)
                case Some(d) => {
                    // Strip of variation suffix (e.g. BasketTestCase => Basket)
                    val productionClass = name.substring(0, name.length - d.length)

                    // "/Users/X/project/src/test/scala/my/package/BasketTestCase.class
                    val path = test.getAbsolutePath

                    // ... => scala/my/package/
                    val relativePath: String = path.substring(path.indexOf("src/test") + 8, path.length - name.length - ".scala".length)

                    // Check if we have a production class for this package
                    val matchingProductionClass = mainMapping.get(relativePath) match {
                        case Some(l) => {
                            // Go through all files and check the name to match productionClass (e.g. Basket)
                            val result = l.filter(m => m.getName.replace(".scala", "").toLowerCase.equals(productionClass))

                            // Due to multi-repository projects, we opt to be safe and return None if we have multiple production classes
                            if (result.size > 1) {
                                //println(s"Multiple classes with same name: ${result}: ${test.getAbsolutePath}")
                                None
                                //assert(result.size < 2, s"Multiple classes with same name: ${result}: ${test.getAbsolutePath}")
                            } else {
                                result.headOption
                            }
                        }
                        case None => None
                    }

                    test -> matchingProductionClass
                }
                case None => test -> None
            }
        }).toMap.filter({case (k,v) => v.nonEmpty}).map({case (k,v) => k -> v.get})
    }

}
