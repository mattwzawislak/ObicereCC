package org.obicere.cc.gui.projects;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.language.LanguageHandler;
import org.obicere.cc.gui.FrameManager;
import org.obicere.cc.projects.Project;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectPanel extends JPanel implements Comparable<ProjectPanel> {

    private static final long serialVersionUID = -3692838815172773196L;
    private static final Color TEXT_COLOR = new Color(0x0F0F0F);
    private final Project project;
    private final JLabel complete;

    public ProjectPanel(final Project project, final boolean isComplete) {
        super(new BorderLayout());

        final JPanel rightPane = new JPanel(new BorderLayout());
        final JPanel leftPane = new JPanel();
        final JPanel centerPane = new JPanel(new GridLayout(3, 1));
        final JPanel options = new JPanel(new BorderLayout(5, 5));

        final JLabel difficulty = new JLabel(Project.DIFFICULTY[project.getDifficulty() - 1]);
        final JLabel name = new JLabel();
        final JLabel author = new JLabel(project.getAuthor());

        final JButton open = new JButton("Open");

        final JComboBox<String> languageChoice = new JComboBox<>(new Vector<>(LanguageHandler.getSupportedLanguages()));

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

        String description = project.getDescription();
        description = description.replace("\n", "<br>");
        description = "<html>".concat(description).concat("</html>");

        complete = new JLabel(null, null, SwingConstants.CENTER);
        setComplete(isComplete);

        this.project = project;

        leftPane.setPreferredSize(new Dimension(25, 150));

        options.add(languageChoice, BorderLayout.NORTH);
        options.add(open, BorderLayout.SOUTH);

        rightPane.add(options, BorderLayout.SOUTH);
        rightPane.add(complete, BorderLayout.CENTER);
        rightPane.setPreferredSize(new Dimension(100, 150));

        setPreferredSize(new Dimension(320, 150));
        setBorder(new BevelBorder(BevelBorder.RAISED));

        open.addActionListener(e -> FrameManager.openProject(project, LanguageHandler.getLanguage((String) languageChoice.getSelectedItem())));

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

}
