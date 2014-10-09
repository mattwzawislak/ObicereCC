package org.obicere.cc.gui.projects;

import org.obicere.cc.gui.FrameManager;
import org.obicere.cc.gui.layout.VerticalFlowLayout;
import org.obicere.cc.gui.layout.WrapLayout;
import org.obicere.cc.projects.Project;
import org.obicere.cc.shutdown.SaveProgressHook;
import org.obicere.cc.shutdown.ShutDownHookManager;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;

public class ProjectSelector extends JPanel {

    private final ArrayList<ProjectPanel> projects = new ArrayList<>();
    private final SaveProgressHook        hook     = ShutDownHookManager.hookByClass(SaveProgressHook.class);

    private final JPanel[] difficultyPanels = new JPanel[5];

    public ProjectSelector() {
        super(new BorderLayout());
        final JPanel selector = new JPanel();
        final VerticalFlowLayout layout = new VerticalFlowLayout(VerticalFlowLayout.TOP);
        layout.setMaximizeOtherDimension(true);
        selector.setLayout(layout);
        for (final Project project : Project.getData()) {
            final ProjectPanel temp = new ProjectPanel(project);
            projects.add(temp);
            temp.setBorder(new BevelBorder(BevelBorder.RAISED));
            temp.setComplete(hook.isComplete(project.getName()));
        }
        Collections.sort(projects);
        for (int i = 0; i < difficultyPanels.length; i++) {
            final JPanel panel = new JPanel(new WrapLayout(WrapLayout.LEFT));
            panel.setBorder(new TitledBorder(Project.getDifficultyString(i + 1)));
            selector.add(panel);

            difficultyPanels[i] = panel;
        }

        projects.forEach(e -> {
            final Project project = e.getProject();
            difficultyPanels[project.getDifficulty() - 1].add(e);
        });

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
