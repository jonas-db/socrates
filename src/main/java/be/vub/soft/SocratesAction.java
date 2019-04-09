package be.vub.soft;

import be.vub.soft.socrates.gui.GUI;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SocratesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        java.awt.EventQueue.invokeLater(() -> new GUI(project));
    }
}
