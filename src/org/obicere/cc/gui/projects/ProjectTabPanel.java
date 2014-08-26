package org.obicere.cc.gui.projects;

import org.obicere.cc.gui.MainTabPanel;

import javax.swing.*;
import java.awt.*;

@MainTabPanel(name = "Projects", index = 0)
public class ProjectTabPanel extends JPanel {

    private static ProjectTabPanel instance;

    private final ProjectSelector projectSelector;

    public ProjectTabPanel() {
        super(new BorderLayout());
        final SearchPanel search = new SearchPanel();
        projectSelector = new ProjectSelector();
        add(search, BorderLayout.NORTH);
        add(projectSelector, BorderLayout.CENTER);
    }

    public static ProjectTabPanel getInstance() {
        if (instance == null) {
            instance = new ProjectTabPanel();
        }
        return instance;
    }

    public ProjectSelector getProjectSelector() {
        return projectSelector;
    }
}
