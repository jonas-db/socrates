package be.vub.soft.socrates.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ResultsView {
    private JTabbedPane tabbedPane1;
    public JPanel mainPanel;
    public JTable testCasesTable;
    public JTable testClassesTable;
    public JButton goBack;
    private JTextField textField1;
    private JTextField textField2;

    public ResultsView() {
        textField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(((DefaultTableModel) testClassesTable.getModel()));
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + textField1.getText()));

                testClassesTable.setRowSorter(sorter);
            }
        });
        textField2.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(((DefaultTableModel) testCasesTable.getModel()));
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + textField2.getText()));

                testCasesTable.setRowSorter(sorter);
            }
        });
    }
}
