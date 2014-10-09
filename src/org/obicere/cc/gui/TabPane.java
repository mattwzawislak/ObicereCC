package org.obicere.cc.gui;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.projects.Project;

import javax.swing.*;
import java.awt.*;

public class TabPane extends JPanel {

    private static final Dimension TAB_SIZE = new Dimension(170, 30);

    public TabPane(final Project project, final Language language) {
        final JLabel label = new JLabel(project.getName());
        final JButton close = new JButton(new ImageIcon(Global.CLOSE_IMAGE));

        close.setPreferredSize(new Dimension(24, 24));
        close.setContentAreaFilled(false);
        close.setOpaque(false);
        close.addActionListener(e -> FrameManager.removeTab(project.getName(), language));

        add(label);
        add(Box.createHorizontalGlue());
        add(close);
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    }

    @Override
    public Dimension getPreferredSize() {
        return TAB_SIZE;
    }

}
