package org.obicere.cc.gui.projects;

import org.obicere.cc.gui.FrameManager;
import org.obicere.cc.gui.layout.WrapLayout;
import org.obicere.cc.projects.Project;
import org.obicere.cc.shutdown.SaveProgressHook;
import org.obicere.cc.shutdown.ShutDownHookManager;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;

public class ProjectSelector extends JPanel {

    private final ArrayList<ProjectPanel> projects = new ArrayList<>();
    private final JPanel selector;
    private final SaveProgressHook hook = ShutDownHookManager.hookByClass(SaveProgressHook.class);

    public ProjectSelector() {
        super(new BorderLayout());
        selector = new JPanel(new WrapLayout(WrapLayout.LEFT));
        for (final Project project : Project.getData()) {
            final ProjectPanel temp = new ProjectPanel(project);
            projects.add(temp);
            temp.setBorder(new BevelBorder(BevelBorder.RAISED));
            temp.setComplete(hook.isComplete(project.getName()));
        }
        Collections.sort(projects);
        projects.forEach(selector::add);
        final JScrollPane scrollPane = new JScrollPane(selector);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);
    }

    public static void setComplete(final String name, final boolean complete) {
        FrameManager.getTab(ProjectTabPanel.class).getProjectSelector().setProjectComplete(name, complete);
    }

    public void setProjectComplete(final String name, final boolean complete) {
        for (final ProjectPanel panel : projects) {
            if (panel.getProject().getName().equals(name)) {
                panel.setComplete(complete);
                return;
            }
        }
    }

    public void refine(final String key, final boolean complete, final boolean name, final boolean incomplete) {
        selector.removeAll();
        for (final ProjectPanel p : projects) {
            final String pName = p.getProject().getName();
            final boolean pComplete = hook.isComplete(pName);
            if (name && pName.toLowerCase().contains(key.toLowerCase()) || key.isEmpty()) {
                if (complete && pComplete || incomplete && !pComplete) {
                    selector.add(p);
                }
            }
        }
        validate();
        revalidate();
        updateUI();
    }
}
