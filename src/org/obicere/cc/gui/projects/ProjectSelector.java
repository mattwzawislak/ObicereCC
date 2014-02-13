package org.obicere.cc.gui.projects;

import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ProjectSelector extends JPanel {

    private static final long serialVersionUID = 4869219241938861949L;
    private static final ArrayList<ProjectPanel> PROJECTS = new ArrayList<>();
    private final JPanel selector;

    public ProjectSelector() {
        super(new BorderLayout());
        selector = new JPanel(new WrapLayout(WrapLayout.LEFT));
        for (final Project project : Project.DATA) {
            final ProjectPanel temp = new ProjectPanel(project);
            PROJECTS.add(temp);
        }
        Collections.sort(PROJECTS, new Comparator<ProjectPanel>() {

            @Override
            public int compare(ProjectPanel o1, ProjectPanel o2) {
                return o1.compareTo(o2);
            }

        });
        for (final ProjectPanel panel : PROJECTS) {
            selector.add(panel);
        }
        final JScrollPane scrollPane = new JScrollPane(selector);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refine(final String key, final boolean complete, final boolean name, final boolean incomplete) {
        selector.removeAll();
        for (final ProjectPanel p : PROJECTS) {
            if (name && p.getProject().getName().toLowerCase().contains(key.toLowerCase()) || key.isEmpty()) {
                if (complete && p.getProject().isComplete() || incomplete && !p.getProject().isComplete()) {
                    selector.add(p);
                }
            }
        }
        revalidate();
        updateUI();
    }

    public static ArrayList<ProjectPanel> getProjectList() {
        return PROJECTS;
    }
}
