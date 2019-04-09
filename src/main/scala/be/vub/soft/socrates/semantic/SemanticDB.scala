package be.vub.soft.socrates.semantic

import scala.meta.Tree
import scala.meta.inputs.Position
import scala.meta.internal.semanticdb.{Range, SymbolInformation, SymbolOccurrence, TextDocument}
import scala.meta.internal.symtab.GlobalSymbolTable
import scala.meta.internal.semanticdb.Scala._

object SemanticDB {

    def position(range: Range, pos: Position): Boolean =
        range.startLine == pos.startLine &&
            range.startCharacter == pos.startColumn &&
            range.endLine == pos.endLine &&
            range.endCharacter == pos.endColumn

    def symbolInformationGlobal2(tree: Tree, document: TextDocument, symtab: GlobalSymbolTable): Option[SymbolInformation] =
        document.occurrences
            .find({
                case SymbolOccurrence(Some(range), _, _) => SemanticDB.position(range, tree.pos)
                case _ => false
            })
            .flatMap({
                case SymbolOccurrence(_, symbol, _) => try {
                    symtab.info(symbol)
                } catch {
                    case _:Exception => None
                }
            })

    def symbolInformationGlobal(tree: Tree, document: TextDocument, symtab: GlobalSymbolTable): Option[SymbolInformation] =
        document.occurrences
            .find({
                case SymbolOccurrence(Some(range), _, _) => SemanticDB.position(range, tree.pos)
                case _ => false
            })
            .flatMap({
                case SymbolOccurrence(_, symbol, _) => try {
                    if(symbol.isLocal) document.symbols.find(l => l.symbol.equals(symbol)) else symtab.info(symbol)
                } catch {
                    case _:Exception => None
                }
            })

}
