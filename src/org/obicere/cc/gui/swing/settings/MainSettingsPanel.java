package org.obicere.cc.gui.swing.settings;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.gui.swing.MainTabPanel;
import org.obicere.cc.gui.swing.layout.VerticalFlowLayout;
import org.obicere.cc.gui.swing.layout.VerticalWrapLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;
import org.obicere.cc.shutdown.ShutDownHook;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Font;
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
        final JPanel panel = new JPanel(layout);
        final JScrollPane scroll = new JScrollPane(panel);

        final ShutDownHook[] hooks = Domain.getGlobalDomain().getHookManager().getShutDownHooks();

        final Font font = getFont().deriveFont(12f);
        for (final ShutDownHook hook : hooks) {
            if (hook instanceof SettingsShutDownHook) {

                final SettingsShutDownHook setting = (SettingsShutDownHook) hook;

                final VerticalFlowLayout innerLayout = new VerticalFlowLayout(VerticalFlowLayout.LEADING);
                innerLayout.setMaximizeOtherDimension(true);
                final JPanel inner = new JPanel(innerLayout);

                final Border border = BorderFactory.createTitledBorder(null, setting.getGroupName(), TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, font);
                inner.setBorder(border);

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
