package be.vub.soft.socrates.gui;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class OptionsPanel {
    public JTextField ivyTextField;
    public JPanel mainPanel;
    public JButton analyzeButton;
    public JButton selectButton;
    public JTextField sbtOptionsTextField;
    public JTextField sbtHomeTextfield;
    public JButton sbtHomeButton;
    public JTextField rtTextField;
    public JButton rtButton;
    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    public OptionsPanel() {

        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int res = chooser.showOpenDialog(mainPanel);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    String filename = f.getAbsolutePath();
                    ivyTextField.setText(filename);

                }
            }
        });
        sbtHomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int res = chooser.showOpenDialog(mainPanel);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    String filename = f.getAbsolutePath();
                    sbtHomeTextfield.setText(filename);

                }
            }
        });

        rtButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                int res = chooser.showOpenDialog(mainPanel);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    String filename = f.getAbsolutePath();
                    rtTextField.setText(filename);

                }
            }
        });
    }

    private void createUIComponents() {

        // TODO: place custom component creation code here
    }
}
