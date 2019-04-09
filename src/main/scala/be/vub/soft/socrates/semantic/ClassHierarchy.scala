package be.vub.soft.socrates.semantic

import scala.meta.internal.semanticdb.SymbolInformation

case class ClassHierarchy(hierarchy: Map[SymbolInformation, Set[SymbolInformation]] = Map.empty) {

    /**
      * Checks if a child class is a subclass of a parent class.
      * Traverse down from parent to find the child. The next parent is always one of its children
      * @param parent
      * @param child
      * @return
      */
    def isSubClass(child: SymbolInformation, parent: SymbolInformation): Boolean = parent.equals(child) match {
        case false =>
            val result: Set[SymbolInformation] = hierarchy.getOrElse(parent, Set())

            result match {
                case children if children.isEmpty => false
                case children => children.exists(c => isSubClass(child, c))
            }
        case _ => true
    }

}
