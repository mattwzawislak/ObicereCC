package org.obicere.cc.gui.settings;

import org.obicere.cc.gui.MainTabPanel;
import org.obicere.cc.gui.VerticalFlowLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;
import org.obicere.cc.shutdown.ShutDownHook;
import org.obicere.cc.shutdown.ShutDownHookManager;

import javax.swing.*;
import java.awt.*;

/**
 * @author Obicere
 */
@MainTabPanel(name = "Settings", index = 1)
public class MainSettingsPanel extends JPanel {

    public MainSettingsPanel() {
        super(new FlowLayout(FlowLayout.LEFT));

        final VerticalFlowLayout layout = new VerticalFlowLayout();
        layout.setMaximizeOtherDimension(true);
        final JPanel panel = new JPanel(layout);
        final JScrollPane scroll = new JScrollPane(panel);

        final ShutDownHook[] hooks = ShutDownHookManager.getShutDownHooks();
        for (final ShutDownHook hook : hooks) {
            if (hook instanceof SettingsShutDownHook) {
                final ShutDownHookPanel content = new ShutDownHookPanel((SettingsShutDownHook) hook);
                panel.add(content);
            }
        }
        add(scroll);

    }
}
