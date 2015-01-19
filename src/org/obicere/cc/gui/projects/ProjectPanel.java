package org.obicere.cc.gui.projects;

import org.obicere.cc.configuration.Configuration;
import org.obicere.cc.configuration.Domain;
import org.obicere.cc.projects.Project;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The small widget used to launch a specific project. Provides a small description when hovered,
 * the author of the project, the name - of course, and whether or not this project has been
 * completed by the user. A small check-mark will be displayed to signify the completion of a
 * project.
 * <p>
 * All of these panels should be the same size, independent of the actual content of the panel. This
 * is just a symmetric policy, to help improve the look of one of the most-used view points.
 *
 * @author Obicere
 * @version 1.0
 */

public class ProjectPanel extends JPanel {

    private static final Dimension PROJECT_PANEL_MANIFEST_SIZE = new Dimension(200, 40);

    private final Project project;
    private final JLabel  complete;

    /**
     * Constructs a new panel for displaying the project. This will also notify the domain to open
     * up a new project upon the <code>open</code> button being clicked.
     *
     * @param project The project to base the panel off of.
     */

    public ProjectPanel(final Project project) {
        super(new FlowLayout(FlowLayout.LEFT, 10, 5));

        this.project = project;

        final JPanel manifestPane = new JPanel();
        final JPanel options = new JPanel(new BorderLayout(5, 5));

        final JLabel name = new JLabel(separateWords(project.getName()));
        final JLabel author = new JLabel(project.getAuthor());
        this.complete = new JLabel(null, null, SwingConstants.CENTER);

        final JButton open = new JButton("Open");

        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));

        complete.setAlignmentX(Component.CENTER_ALIGNMENT);
        open.setAlignmentX(Component.CENTER_ALIGNMENT);

        options.add(Box.createVerticalGlue());
        options.add(complete);
        options.add(Box.createVerticalGlue());
        options.add(open);
        options.setPreferredSize(new Dimension(60, 100));

        open.addActionListener(e -> Domain.getGlobalDomain().getFrameManager().openProject(project, ProjectSelectorControls.getControls().getSelectedLanguage()));

        name.setPreferredSize(PROJECT_PANEL_MANIFEST_SIZE);
        name.setFont(name.getFont().deriveFont(14f));
        name.setToolTipText(toHTML(project.getDescription()));

        manifestPane.setLayout(new BoxLayout(manifestPane, BoxLayout.Y_AXIS));
        manifestPane.add(name);
        manifestPane.add(Box.createVerticalGlue());
        manifestPane.add(author);
        manifestPane.setPreferredSize(new Dimension(200, 100));

        add(manifestPane);
        add(options);
    }

    /**
     * Sets the icon in the frame to the complete check-mark if and only if the
     * <code>isComplete</code> flag it set to <code>true</code>.
     *
     * @param isComplete Whether or not the project is now complete.
     */

    public void setComplete(final boolean isComplete) {
        complete.setIcon(isComplete ? new ImageIcon(Configuration.COMPLETE_IMAGE) : null);
    }

    /**
     * Retrieves the project used to base this panel. This is used primarily for arranging the
     * panels by name and also when searching.
     *
     * @return The basis project.
     */

    public Project getProject() {
        return project;
    }

    /**
     * Attempts to convert a simple string to HTML. This is just meant for tooltip texts, where the
     * newline feeds are not considered, but the <code>&lt;br&gt;</code> tags are. HTML and Java
     * strings do not mix, so sorry if this blows up in a burning ball of flame.
     *
     * @param description The description to <i>attempt</i> to create the tag for.
     *
     * @return The - hopefully correct - HTML format for the tooltip.
     */

    private String toHTML(final String description) {
        return "<html>".concat(description.replace("\n", "<br>")).concat("</html>");
    }

    /**
     * Attempts to separate the words of the runner name into individual parts. This works the same
     * as the {@link org.obicere.cc.executor.language.Casing} system, but the default casing types
     * support code-oriented notation. This method will inherit whitespace to style the string
     * correctly. Note that this is just an approximation and there may be issues with parsing the
     * correct form of the runner's name.
     * <p>
     * For example, given the input string <code>"TestABC"</code>, this would split by casing and
     * produce:
     * <p>
     * <code>["Test", "A", "B", "C"]</code>
     * <p>
     * From there, the strings will be appended by whitespace to produce:
     * <p>
     * <code>"Test A B C"</code>
     *
     * @param name The name to attempt to split into individual words.
     *
     * @return The parsed string hopefully in the correct format.
     */

    private String separateWords(final String name) {
        final StringBuilder builder = new StringBuilder();
        final Pattern pattern = Pattern.compile("[A-Z]?[a-z]+|[0-9]+");
        final Matcher matcher = pattern.matcher(name);
        while (matcher.find()) {
            builder.append(matcher.group());
            builder.append(' ');
        }
        builder.trimToSize();
        return builder.toString();
    }

}
