package org.obicere.cc.gui.projects;

import org.obicere.cc.gui.MainTabPanel;

import javax.swing.*;
import java.awt.*;

@MainTabPanel(name = "Projects", index = 0)
public class ProjectTabPanel extends JPanel {

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
