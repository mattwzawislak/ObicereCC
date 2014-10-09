package org.obicere.cc.gui.settings;

import org.obicere.cc.gui.MainTabPanel;
import org.obicere.cc.gui.layout.VerticalWrapLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;
import org.obicere.cc.shutdown.ShutDownHook;
import org.obicere.cc.shutdown.ShutDownHookManager;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * @author Obicere
 */
@MainTabPanel(name = "Settings", index = 1)
public class MainSettingsPanel extends JPanel {

    public MainSettingsPanel() {
        super(new BorderLayout());

        final VerticalWrapLayout layout = new VerticalWrapLayout();
        final JPanel panel = new JPanel(layout);
        final JScrollPane scroll = new JScrollPane(panel);

        final ShutDownHook[] hooks = ShutDownHookManager.getShutDownHooks();
        for (final ShutDownHook hook : hooks) {
            if (hook instanceof SettingsShutDownHook) {
                final ShutDownHookPanel content = new ShutDownHookPanel((SettingsShutDownHook) hook);
                panel.add(content);
            }
        }
        add(scroll, BorderLayout.CENTER);

    }
}
