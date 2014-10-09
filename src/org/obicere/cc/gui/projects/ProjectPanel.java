package org.obicere.cc.gui.projects;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.gui.FrameManager;
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

public class ProjectPanel extends JPanel implements Comparable<ProjectPanel> {

    private static final Dimension PROJECT_PANEL_MANIFEST_SIZE = new Dimension(200, 40);

    private final Project project;
    private final JLabel  complete;

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

        open.addActionListener(e -> FrameManager.openProject(project, ProjectSelectorControls.getControls().getSelectedLanguage()));

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

    public void setComplete(boolean isComplete) {
        complete.setIcon(isComplete ? new ImageIcon(Global.COMPLETE_IMAGE) : null);
    }

    public Project getProject() {
        return project;
    }

    @Override
    public int compareTo(final ProjectPanel o) {
        return getProject().getSortName().compareTo(o.getProject().getSortName());
    }

    private String toHTML(final String description) {
        return "<html>".concat(description.replace("\n", "<br>")).concat("</html>");
    }

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
