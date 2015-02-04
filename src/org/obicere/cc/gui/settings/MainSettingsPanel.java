package org.obicere.cc.gui.settings;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.gui.MainTabPanel;
import org.obicere.cc.gui.layout.VerticalFlowLayout;
import org.obicere.cc.gui.layout.VerticalWrapLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;
import org.obicere.cc.shutdown.ShutDownHook;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;

/**
 * @author Obicere
 */
@MainTabPanel(name = "Settings", index = 1)
public class MainSettingsPanel extends JPanel {

    public MainSettingsPanel() {
        super(new BorderLayout());

        final VerticalFlowLayout layout = new VerticalWrapLayout(VerticalFlowLayout.LEADING);
        layout.setSameWidth(true);
        layout.setMaximizeOtherDimension(false);
        final JPanel panel = new JPanel(layout);
        final JScrollPane scroll = new JScrollPane(panel);

        final ShutDownHook[] hooks = Domain.getGlobalDomain().getHookManager().getShutDownHooks();
        for (final ShutDownHook hook : hooks) {
            if (hook instanceof SettingsShutDownHook) {

                final SettingsShutDownHook setting = (SettingsShutDownHook) hook;

                //panel.add(new JSeparator());
                //panel.add(new JLabel(setting.getGroupName(), JLabel.CENTER));

                final VerticalFlowLayout innerLayout = new VerticalFlowLayout(VerticalFlowLayout.LEADING);
                innerLayout.setMaximizeOtherDimension(true);
                final JPanel inner = new JPanel(innerLayout);

                inner.setBorder(BorderFactory.createTitledBorder(setting.getGroupName()));

                final Map<String, SettingPanel> options = setting.getSettingPanels();
                options.forEach(
                        (key, option) -> {
                            option.buildPanel();
                            inner.add(option);
                        });
                panel.add(inner);
            }
        }
        add(scroll, BorderLayout.CENTER);

    }
}
