package org.obicere.cc.gui.projects;

import org.obicere.cc.gui.MainTabPanel;

import javax.swing.JPanel;
import java.awt.BorderLayout;

@MainTabPanel(name = "Projects", index = 0)
public class ProjectTabPanel extends JPanel {

    public static final String NAME = "Projects";

    private final ProjectSelector projectSelector;

    public ProjectTabPanel() {
        super(new BorderLayout());
        final ProjectSelectorControls search = ProjectSelectorControls.getControls();
        projectSelector = new ProjectSelector();
        add(search, BorderLayout.NORTH);
        add(projectSelector, BorderLayout.CENTER);
    }

    public ProjectSelector getProjectSelector() {
        return projectSelector;
    }
}
