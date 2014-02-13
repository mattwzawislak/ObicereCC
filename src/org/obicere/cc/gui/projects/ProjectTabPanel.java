package org.obicere.cc.gui.projects;

import javax.swing.*;
import java.awt.*;

public class ProjectTabPanel extends JPanel {

    private static ProjectTabPanel instance;

    private final ProjectSelector projectSelector;

    public static ProjectTabPanel getInstance(){
        if(instance == null){
            instance = new ProjectTabPanel();
        }
        return instance;
    }

    private ProjectTabPanel(){
        super(new BorderLayout());
        final SearchPanel search = new SearchPanel();
        projectSelector = new ProjectSelector();
        add(search, BorderLayout.NORTH);
        add(projectSelector, BorderLayout.CENTER);
    }

    public ProjectSelector getProjectSelector(){
        return projectSelector;
    }

    @Override
    public String getName(){
        return "Projects";
    }
}
