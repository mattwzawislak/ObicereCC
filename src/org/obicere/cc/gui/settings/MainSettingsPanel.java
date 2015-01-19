package org.obicere.cc.gui.settings;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.gui.MainTabPanel;
import org.obicere.cc.gui.layout.VerticalFlowLayout;
import org.obicere.cc.gui.layout.VerticalWrapLayout;
import org.obicere.cc.gui.layout.WrapLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;
import org.obicere.cc.shutdown.ShutDownHook;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * @author Obicere
 */
@MainTabPanel(name = "Settings", index = 1)
public class MainSettingsPanel extends JPanel {

    public MainSettingsPanel() {
        super(new BorderLayout());

        final LayoutManager layout = new VerticalWrapLayout(VerticalWrapLayout.LEADING);
        final JPanel panel = new JPanel(layout);
        final JScrollPane scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        final ShutDownHook[] hooks = Domain.getGlobalDomain().getHookManager().getShutDownHooks();
        for (final ShutDownHook hook : hooks) {
            if (hook instanceof SettingsShutDownHook) {
                final ShutDownHookPanel content = new ShutDownHookPanel((SettingsShutDownHook) hook);
                panel.add(content);
            }
        }
        add(scroll, BorderLayout.CENTER);

    }
}
