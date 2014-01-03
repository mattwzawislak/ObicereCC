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

import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * The selector is used to display the list of available projects and allow the
 * user to open them. This will display all correctly published Runners.
 *
 * @author Obicere
 * @since 1.0
 */

public class ProjectSelector extends JPanel {

    private static final long serialVersionUID = 4869219241938861949L;
    private static final ArrayList<ProjectPanel> PROJECTS = new ArrayList<>();
    private final JPanel selector;

    /**
     * Constructs a new project selector. Only to be used inside of the
     * {@link org.obicere.cc.gui.GUI#openProject(Project, Language)}.
     *
     * @since 1.0
     */

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

    /**
     * Filters out <tt>ProjectPanels</tt> by a search key or based
     * conditions. This method clears the panel, then re-adds components
     * that meet the required conditions if any.
     *
     * @param key        The key to search for in name
     * @param complete   Whether or not to include complete projects
     * @param name       Whether or not to search by name. This uses
     *                   the <tt>key</tt> for reference
     * @param incomplete Whether or not to include incomplete projects
     */

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

    /**
     * Gets the list of all added projects to the selector pane.
     *
     * @return The list of available projects.
     * @since 1.0
     */

    public static ArrayList<ProjectPanel> getProjectList() {
        return PROJECTS;
    }
}
