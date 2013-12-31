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

import org.obicere.cc.configuration.Global;
import org.obicere.cc.gui.GUI;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used for the construction of the
 * {@link ProjectSelector}. In regards to other usage, there is none.
 * This will get information from project and use that to render an appropriate
 * UI.
 *
 * @author Obicere
 * @see ProjectSelector
 * @since 1.0
 */

public class ProjectPanel extends JPanel implements Comparable<ProjectPanel> {

    private static final long serialVersionUID = -3692838815172773196L;

    private final Project project;

    private static final Color TEXT_COLOR = new Color(0x0F0F0F);
    private final JLabel complete;

    /**
     * Constructs a new panel dependent on the <tt>project</tt>.
     *
     * @param project The project to get required information such as name and
     *                difficulty.
     * @since 1.0
     */

    public ProjectPanel(final Project project) {
        super(new BorderLayout());

        final JPanel rightPane = new JPanel(new BorderLayout());
        final JPanel leftPane = new JPanel();
        final JPanel centerPane = new JPanel(new GridLayout(3, 1));
        final JPanel options = new JPanel(new BorderLayout(5, 5));

        final JLabel difficulty = new JLabel(Project.DIFFICULTY[project.getProperties().getCategory() - 1]);
        final JLabel name = new JLabel();
        final JLabel author = new JLabel(project.getProperties().getAuthor());

        final JButton open = new JButton("Open");

        //final JComboBox<Language> languageChoice = new JComboBox<>(Language.values());

        final StringBuilder builder = new StringBuilder();
        final Pattern pattern = Pattern.compile("[A-Z]?[a-z]+|[0-9]+");
        final Matcher matcher = pattern.matcher(project.getName());
        while (matcher.find()) {
            builder.append(matcher.group());
            builder.append(' ');
        }
        builder.trimToSize();
        name.setText(builder.toString());
        if (name.getText().length() == 0) {
            name.setText(project.getName());
        }

        String description = project.getProperties().getDescription();
        description = description.replace("\n", "<br>");
        description = "<html>".concat(description).concat("</html>");

        complete = new JLabel(null, null, SwingConstants.CENTER);
        setComplete(project.isComplete());

        this.project = project;

        leftPane.setPreferredSize(new Dimension(25, 150));

        //options.add(languageChoice, BorderLayout.NORTH);
        options.add(open, BorderLayout.SOUTH);

        rightPane.add(options, BorderLayout.SOUTH);
        rightPane.add(complete, BorderLayout.CENTER);
        rightPane.setPreferredSize(new Dimension(100, 150));

        setPreferredSize(new Dimension(320, 150));
        setBorder(new BevelBorder(BevelBorder.RAISED));

        open.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GUI.openProject(project);
            }

        });

        complete.setPreferredSize(new Dimension(75, 30));

        difficulty.setPreferredSize(new Dimension(200, 30));
        difficulty.setFont(difficulty.getFont().deriveFont(15f));
        difficulty.setForeground(TEXT_COLOR);

        name.setPreferredSize(new Dimension(200, 50));
        name.setFont(name.getFont().deriveFont(18f));
        name.setForeground(TEXT_COLOR);

        author.setPreferredSize(new Dimension(200, 30));
        author.setFont(author.getFont().deriveFont(15f));
        author.setForeground(TEXT_COLOR);

        centerPane.add(name);
        centerPane.add(author);
        centerPane.add(difficulty);

        add(leftPane, BorderLayout.WEST);
        add(centerPane, BorderLayout.CENTER);
        add(rightPane, BorderLayout.EAST);
        setToolTipText(description);
    }

    /**
     * Used to change the Icon for the completion marker. Only true usage is in
     * the instance construction and during execution.
     *
     * @param isComplete sets the project icon complete
     * @since 1.0
     */

    public void setComplete(boolean isComplete) {
        complete.setIcon(isComplete ? new ImageIcon(Global.COMPLETE_IMAGE) : null);
    }

    /**
     * This will return the project this panel is based off of.
     *
     * @return Project instance used to make this panel.
     * @since 1.0
     */

    public Project getProject() {
        return project;
    }

    @Override
    public int compareTo(ProjectPanel o) {
        return getProject().getSortName().compareTo(o.getProject().getSortName());
    }

}
