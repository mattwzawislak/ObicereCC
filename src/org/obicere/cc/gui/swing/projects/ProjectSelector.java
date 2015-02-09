package org.obicere.cc.gui.swing.projects;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.gui.swing.layout.VerticalFlowLayout;
import org.obicere.cc.gui.swing.layout.WrapLayout;
import org.obicere.cc.projects.Project;
import org.obicere.cc.projects.ProjectComparator;
import org.obicere.cc.projects.ProjectLoader;
import org.obicere.cc.shutdown.SaveProgressHook;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectSelector extends JPanel {

    private final ArrayList<ProjectPanel> projects = new ArrayList<>();
    private final SaveProgressHook        hook     = Domain.getGlobalDomain().getHookManager().hookByClass(SaveProgressHook.class);

    private final JPanel[] difficultyPanels = new JPanel[5];

    public ProjectSelector() {
        super(new BorderLayout());
        final JPanel selector = new JPanel();
        final VerticalFlowLayout layout = new VerticalFlowLayout(VerticalFlowLayout.TOP);
        final Border border = BorderFactory.createRaisedSoftBevelBorder();
        final List<Project> projectList = ProjectLoader.getData();

        layout.setMaximizeOtherDimension(true);
        selector.setLayout(layout);
        Collections.sort(projectList, new ProjectComparator());

        for (final Project project : projectList) {
            final ProjectPanel panel = new ProjectPanel(project);
            projects.add(panel);
            panel.setBorder(border);
            panel.setComplete(hook.isComplete(project.getName()));
        }
        for (int i = 0; i < difficultyPanels.length; i++) {
            final JPanel panel = new JPanel(new WrapLayout(WrapLayout.LEFT));
            panel.setBorder(new TitledBorder(Project.getDifficultyString(i + 1))); // Difficulty strings are 1-based
            selector.add(panel);

            difficultyPanels[i] = panel;
        }

        projects.forEach(e -> {
            final Project project = e.getProject();
            difficultyPanels[project.getDifficulty() - 1].add(e); // Difficulty strings are 1-based
        });

        final JScrollPane scrollPane = new JScrollPane(selector);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);
    }

    public static void setComplete(final String name, final boolean complete) {
        Domain.getGlobalDomain().getFrameManager().getTab(ProjectTabPanel.class).getProjectSelector().setProjectComplete(name, complete);
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
        synchronized (getTreeLock()) {
            for (final JPanel panel : difficultyPanels) {
                panel.removeAll();
            }
            for (final ProjectPanel p : projects) {
                final Project project = p.getProject();
                final String pName = project.getName();
                final boolean pComplete = hook.isComplete(pName);
                if (name && pName.toLowerCase().contains(key.toLowerCase()) || key.isEmpty()) {
                    if (complete && pComplete || incomplete && !pComplete) {
                        difficultyPanels[project.getDifficulty() - 1].add(p);
                    }
                }
            }
            revalidate();
            updateUI();
        }
    }
}
