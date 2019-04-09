package be.vub.soft.socrates.util

import java.io.{File, FileWriter}

import be.vub.soft.socrates.analysis.Socrates

trait Debuggable {

    val file: Option[File] = None

    def debug(m: String) = if(Socrates.DEBUG) {
        val str = s"$m"

        if(file.nonEmpty) {
            val fw = new FileWriter(file.get, true)

            try fw.write(str)
            catch {
                case _: Throwable => ()
            }
            finally fw.close()
        }

        println(str)
    }

}
