package org.obicere.cc.gui.settings;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.gui.MainTabPanel;
import org.obicere.cc.gui.layout.VerticalFlowLayout;
import org.obicere.cc.gui.layout.VerticalWrapLayout;
import org.obicere.cc.gui.layout.WrapLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;
import org.obicere.cc.shutdown.ShutDownHook;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Map;

/**
 * @author Obicere
 */
@MainTabPanel(name = "Settings", index = 1)
public class MainSettingsPanel extends JPanel {

    public MainSettingsPanel() {
        super(new BorderLayout());

        final VerticalFlowLayout layout = new VerticalFlowLayout(VerticalFlowLayout.LEADING);
        layout.setMaximizeOtherDimension(true);
        final JPanel panel = new JPanel(layout);
        final JScrollPane scroll = new JScrollPane(panel);

        final ShutDownHook[] hooks = Domain.getGlobalDomain().getHookManager().getShutDownHooks();
        for (final ShutDownHook hook : hooks) {
            if (hook instanceof SettingsShutDownHook) {

                final SettingsShutDownHook setting = (SettingsShutDownHook) hook;

                panel.add(new JSeparator());
                panel.add(new JLabel(setting.getGroupName(), JLabel.CENTER));

                final Map<String, SettingPanel> options = setting.getSettingPanels();
                options.forEach(
                        (key, option) -> {
                            option.buildPanel();
                            panel.add(option);
                        });
            }
        }
        add(scroll, BorderLayout.CENTER);

    }
}
