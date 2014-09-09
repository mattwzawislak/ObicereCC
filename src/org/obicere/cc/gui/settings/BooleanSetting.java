package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.*;
import java.awt.*;

/**
 * @author Obicere
 */
public class BooleanSetting extends SettingPanel {

    protected BooleanSetting(final SettingsShutDownHook hook, final String value, final String description) {
        super(hook, value, description);
    }

    @Override
    protected void buildPanel() {
        final SettingsShutDownHook hook = getHook();
        final String value = getKey();

        final JCheckBox allowed = new JCheckBox(getDescriptor());
        allowed.setSelected(hook.getPropertyAsBoolean(value));
        allowed.addChangeListener(e -> {
            final boolean selected = allowed.isSelected();
            hook.toggleSetting(value, selected);
        });
        add(allowed);
    }
}
