package be.vub.soft.socrates.analysis.scalal.explorable

import scala.meta.Tree

abstract class Check
case class FunctionCheck(spec: String, matcher: Tree => Boolean) extends Check
case class InheritanceCheck(spec: String) extends Check
