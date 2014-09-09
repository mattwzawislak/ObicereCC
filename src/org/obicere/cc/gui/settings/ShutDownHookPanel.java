package org.obicere.cc.gui.settings;

import org.obicere.cc.gui.projects.WrapLayout;
import org.obicere.cc.shutdown.SettingsShutDownHook;
import org.obicere.cc.shutdown.ShutDownHook;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Obicere
 */
public class ShutDownHookPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(ShutDownHook.class.getCanonicalName());

    public ShutDownHookPanel(final SettingsShutDownHook hook) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new TitledBorder(hook.getGroupName()));

        final Map<String, String> options = hook.getSettingDescriptions();
        options.forEach(
                (key, description) -> {
                    final SettingPanel panel;
                    final Object value = hook.getDynamicObject(key);
                    if (value instanceof Boolean) {
                        panel = new BooleanSetting(hook, key, description);
                    } else if (value instanceof Integer) {
                        panel = new IntegerSetting(hook, key, description);
                    } else if (value instanceof Color) {
                        panel = new ColorSetting(hook, key, description);
                    } else if (value instanceof String) {
                        panel = new FontSetting(hook, key, description);
                    } else {

                        // TODO: Not every string will be a font. Fix this ASAP before adding new settings
                        LOGGER.log(Level.WARNING, "Unsupported option: " + value);
                        return;
                    }

                    panel.buildPanel();
                    add(panel);

                });
    }

}
