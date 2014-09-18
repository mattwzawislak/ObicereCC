package org.obicere.cc.gui.settings;

import org.obicere.cc.gui.VerticalFlowLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.Map;

/**
 * @author Obicere
 */
public class ShutDownHookPanel extends JPanel {

    public ShutDownHookPanel(final SettingsShutDownHook hook) {
        final VerticalFlowLayout layout = new VerticalFlowLayout(VerticalFlowLayout.CENTER, 10, 10);
        layout.setMaximizeOtherDimension(true);
        setLayout(layout);
        setBorder(new TitledBorder(hook.getGroupName()));

        final Map<String, SettingPanel> options = hook.getSettingPanels();
        options.forEach(
                (key, panel) -> {
                    panel.buildPanel();
                    add(panel);

                });
    }

}
