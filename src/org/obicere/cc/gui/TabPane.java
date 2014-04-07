package org.obicere.cc.gui;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.awt.*;

public class TabPane extends JPanel {

    private static final Dimension TAB_SIZE = new Dimension(170, 30);

    public TabPane(final Project project, final Language language) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 2));
        final JLabel label = new JLabel(project.getName());
        final JPanel buffer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JButton close = new JButton(new ImageIcon(Global.CLOSE_IMAGE));

        buffer.setOpaque(false);
        buffer.setPreferredSize(new Dimension(146, 24));
        buffer.add(label);

        close.setPreferredSize(new Dimension(24, 24));
        close.setContentAreaFilled(false);
        close.setOpaque(false);
        close.addActionListener(e -> GUI.removeTab(project.getName(), language));

        add(buffer);
        add(close);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize(){
        return TAB_SIZE;
    }

}
