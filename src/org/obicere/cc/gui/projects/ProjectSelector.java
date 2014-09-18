package org.obicere.cc.gui.projects;

import org.obicere.cc.shutdown.SaveProgressHook;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.projects.Project;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class ProjectSelector extends JPanel {

    private static final long                    serialVersionUID = 4869219241938861949L;
    private static final ArrayList<ProjectPanel> PROJECTS         = new ArrayList<>();
    private final JPanel selector;
    private final SaveProgressHook hook = ShutDownHookManager.hookByClass(SaveProgressHook.class);

    public ProjectSelector() {
        super(new BorderLayout());
        selector = new JPanel(new WrapLayout(WrapLayout.LEFT));
        for (final Project project : Project.DATA) {
            final ProjectPanel temp = new ProjectPanel(project, hook.isComplete(project.getName()));
            PROJECTS.add(temp);
        }
        Collections.sort(PROJECTS, (o1, o2) -> o1.compareTo(o2));
        PROJECTS.forEach(selector::add);
        final JScrollPane scrollPane = new JScrollPane(selector);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);
    }

    public static ArrayList<ProjectPanel> getProjectList() {
        return PROJECTS;
    }

    public void refine(final String key, final boolean complete, final boolean name, final boolean incomplete) {
        selector.removeAll();
        for (final ProjectPanel p : PROJECTS) {
            final String pName = p.getProject().getName();
            final boolean pComplete = hook.isComplete(pName);
            if (name && pName.toLowerCase().contains(key.toLowerCase()) || key.isEmpty()) {
                if (complete && pComplete || incomplete && !pComplete) {
                    selector.add(p);
                }
            }
        }
        revalidate();
        updateUI();
    }
}
