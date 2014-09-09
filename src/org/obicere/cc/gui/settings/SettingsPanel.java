package org.obicere.cc.gui.settings;

import org.obicere.cc.gui.MainTabPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Obicere
 */
@MainTabPanel(name = "Settings", index = 1)
public class SettingsPanel extends JPanel {

    public SettingsPanel() {
        super(new BorderLayout(25, 25));

        final ShutDownHookPanelGroup shutDownGroup = new ShutDownHookPanelGroup();
        add(shutDownGroup);

    }
}
