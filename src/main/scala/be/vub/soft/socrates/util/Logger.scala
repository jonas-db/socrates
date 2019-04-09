package be.vub.soft.socrates.util

class Logger(n: String) {
    def info(s: String): Unit = {
        println(s"$n: $s")
    }
}
object Logger {
    def apply(n: String) = new Logger(n)
}
