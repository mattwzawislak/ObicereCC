package org.obicere.cc.gui.settings;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;

/**
 * @author Obicere
 */
public class SettingsPanel extends JPanel {

    private static final Border GROUP_BORDER = new BevelBorder(BevelBorder.RAISED);

    private static SettingsPanel instance;

    public SettingsPanel() {
        super(new FlowLayout(FlowLayout.CENTER, 25, 25));

        setName("Settings");
        final ShutDownHookPanelGroup shutDownGroup = new ShutDownHookPanelGroup();
        shutDownGroup.setBorder(GROUP_BORDER);

        add(shutDownGroup);

    }

    public static SettingsPanel getInstance() {
        if (instance == null) {
            instance = new SettingsPanel();
        }
        return instance;
    }

}
