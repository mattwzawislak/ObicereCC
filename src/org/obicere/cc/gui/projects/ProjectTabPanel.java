/*
This file is part of ObicereCC.

ObicereCC is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ObicereCC is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ObicereCC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.obicere.cc.gui.projects;

import javax.swing.*;
import java.awt.*;

/**
 * One of the three main tabs, the <tt>ProjectTabPanel</tt> class is
 * used to display the available projects the user can run.
 *
 * @author Obicere
 * @since 1.0
 */

public class ProjectTabPanel extends JPanel {

    private static ProjectTabPanel instance;

    private final ProjectSelector projectSelector;

    /**
     * Used to implement a singleton definition.
     * If no instance has been created yet, one will be created and returned.
     *
     * @return the instance of the <tt>ProjectTabPanel</tt>
     */

    public static ProjectTabPanel getInstance(){
        if(instance == null){
            instance = new ProjectTabPanel();
        }
        return instance;
    }

    /**
     * Constructs a new <tt>ProjectTabPanel</tt>. This is private
     * to enforce a singleton definition.
     */

    private ProjectTabPanel(){
        super(new BorderLayout());
        final SearchPanel search = new SearchPanel();
        projectSelector = new ProjectSelector();
        add(search, BorderLayout.NORTH);
        add(projectSelector, BorderLayout.CENTER);
    }

    /**
     * This is used to get the <tt>ProjectSelector</tt> instance.
     * The project selector panel is where all projects will be displayed.
     *
     * @return The instance of the instanced project selector.
     */

    public ProjectSelector getProjectSelector(){
        return projectSelector;
    }

    @Override
    public String getName(){
        return "Projects";
    }
}
