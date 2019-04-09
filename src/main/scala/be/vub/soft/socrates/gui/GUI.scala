package be.vub.soft.socrates.gui

import java.awt.Dimension
import java.awt.event.{ActionEvent, ActionListener}
import java.nio.file.Paths
import java.util.concurrent.FutureTask

import be.vub.soft.socrates.analysis.Socrates
import be.vub.soft.socrates.analysis.Socrates.SocratesResult
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.progress.{ProgressIndicator, ProgressManager, Task}
import com.intellij.openapi.project.Project
import javax.swing._
import javax.swing.table.DefaultTableModel

class GUI(project: Project) extends JFrame with ActionListener {

    private val columnsTestClasses = Array[AnyRef]("URI", "Global Fixture", "Lazy Test")
    private val columnsTestCases = Array[AnyRef]("Test", "Assertion Roulette", "Sensitive Equality", "Eager Test", "Loan Fixture", "With Fixture", "Fixture Context", "Mystery Guest")
    private val modelTestClasses = new DefaultTableModel(columnsTestClasses, 0) {
        override def isCellEditable(row: Int, column: Int): Boolean = false
    }
    private val modelTestCases = new DefaultTableModel(columnsTestCases, 0) {
        override def isCellEditable(row: Int, column: Int): Boolean = false
    }

    private val optionsPanel = new OptionsPanel()
    private val resultsView = new ResultsView()

    private val home = System.getProperty("user.home")
    optionsPanel.ivyTextField.setText(Paths.get(home, ".ivy2").toString)
    optionsPanel.sbtOptionsTextField.setText("-mem 4096")
    optionsPanel.sbtHomeTextfield.setText("/usr/local/bin/sbt")
    optionsPanel.rtTextField.setText("/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/jre/lib/rt.jar")

    val tool = "SoCRATES"

    this.setTitle(tool)
    this.add(optionsPanel.mainPanel)
    this.setAlwaysOnTop(true)
    this.toFront()

    this.pack()
    this.setVisible(true)

    val renderer = new CustomCellRenderer()

    resultsView.testClassesTable.setAutoCreateRowSorter(true)
    resultsView.testCasesTable.setAutoCreateRowSorter(true)

    resultsView.testClassesTable.setModel(modelTestClasses)
    resultsView.testCasesTable.setModel(modelTestCases)

    columnsTestClasses.drop(1).foreach(i => resultsView.testClassesTable.getColumn(i).setCellRenderer(renderer))
    columnsTestCases.drop(1).foreach(i => resultsView.testCasesTable.getColumn(i).setCellRenderer(renderer))

    resultsView.mainPanel.setPreferredSize(new Dimension(1000, 500))

    optionsPanel.analyzeButton.addActionListener(this)
    resultsView.goBack.addActionListener(this)

//    private def enableAll(status: Boolean) = {
//        optionsPanel.analyzeButton.setEnabled(status)
//        optionsPanel.ivyTextField.setEnabled(status)
//        optionsPanel.selectButton.setEnabled(status)
//        optionsPanel.sbtOptionsTextField.setEnabled(status)
//        optionsPanel.sbtHomeButton.setEnabled(status)
//        optionsPanel.sbtHomeTextfield.setEnabled(status)
//
//        this.setVisible(status)
//    }

    override def actionPerformed(e: ActionEvent): Unit = e match {
        case _ if e.getSource.equals(optionsPanel.analyzeButton) =>
            this.setVisible(false)

            try {
                val paths = optionsPanel.ivyTextField.getText.split(":").toList
                val sbtOptions = optionsPanel.sbtOptionsTextField.getText
                val sbtHome = optionsPanel.sbtHomeTextfield.getText
                val rtPath = optionsPanel.rtTextField.getText

                val self = this

                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Starting Socrates...") {
                    def run(progressIndicator: ProgressIndicator)
                    {
                        java.awt.EventQueue.invokeAndWait(() => {
                            progressIndicator.setFraction(0.0)
                        })

                        def update(n: Int): Unit = java.awt.EventQueue.invokeLater(() => {
                            progressIndicator.setFraction(n / 100.0)
                        })

                        def updateString(n: String): Unit = progressIndicator.setText(n)

                        val future: FutureTask[SocratesResult] = Socrates.analyze(project, sbtHome, sbtOptions, update, updateString, rtPath, paths: _*)

                        future.run()
                        val (testClasses, testCases) = future.get()

                        java.awt.EventQueue.invokeLater(() => {

                            modelTestClasses.setRowCount(0)
                            modelTestCases.setRowCount(0)

                            testClasses.foreach(tc => modelTestClasses.addRow(Array[AnyRef](tc.name, Boolean.box(tc.gf), Boolean.box(tc.lt))))
                            testCases.foreach(tc => modelTestCases.addRow(Array[AnyRef](tc.name,
                                Boolean.box(tc.ar), Boolean.box(tc.se), Boolean.box(tc.et),
                                Boolean.box(tc.lt), Boolean.box(tc.ot), Boolean.box(tc.cf),
                                Boolean.box(tc.mg))))

                            self.remove(optionsPanel.mainPanel)
                            self.add(resultsView.mainPanel)

                            self.setVisible(true)

                            Notifications.Bus.notify(new Notification(tool, tool, "Analysis finished.", NotificationType.INFORMATION))

                            self.pack()
                            self.repaint()
                            self.revalidate()
                        })
                    }
                })
            } catch {
                case e: Throwable =>
                    e.printStackTrace()
                    Notifications.Bus.notify(new Notification(tool, tool, "Something went wrong:\n"+e.getMessage, NotificationType.INFORMATION))
                    this.setVisible(true)
            }
        case _ if e.getSource.equals(resultsView.goBack) =>
            // Swap views
            this.add(optionsPanel.mainPanel)
            this.remove(resultsView.mainPanel)

            this.setVisible(true)

            this.pack()
            this.repaint()
            this.revalidate()
    }
}
