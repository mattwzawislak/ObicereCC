package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.*;
import java.awt.*;

/**
 * @author Obicere
 */
public class FontSetting extends SettingPanel {

    private static final String[] FONT_LISTING;

    static {
        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        FONT_LISTING = env.getAvailableFontFamilyNames();
    }

    public FontSetting(SettingsShutDownHook hook, String value, String description) {
        super(hook, value, description);
    }

    @Override
    protected void buildPanel() {

        final SettingsShutDownHook hook = getHook();
        final String key = getKey();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        final JLabel description = new JLabel(getDescriptor());
        final JComboBox<String> fontSelection = new JComboBox<>(FONT_LISTING);

        fontSelection.setSelectedItem(hook.getPropertyAsString(key));
        fontSelection.addItemListener(e -> {
            final String selected = (String) e.getItem();
            hook.setProperty(key, selected);
        });

        add(description);
        add(Box.createHorizontalGlue());
        add(fontSelection);
    }
}
