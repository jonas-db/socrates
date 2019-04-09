package be.vub.soft.socrates.gui

import java.awt.{Color, Component}

import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

class CustomCellRenderer extends DefaultTableCellRenderer {

    override def getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component = {
        val cellComponent = super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column)

        if (table.getValueAt(row, column).toString == "true") cellComponent.setBackground(Color.decode("#F57670"))
        else if (table.getValueAt(row, column).toString == "false") cellComponent.setBackground(Color.decode("#1FBA47"))

        cellComponent
    }
}