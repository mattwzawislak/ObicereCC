package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

/**
 * @author Obicere
 */
public class BooleanSetting extends SettingPanel {

    public BooleanSetting(final SettingsShutDownHook hook, final String value, final String description) {
        super(hook, value, description);
    }

    @Override
    protected void buildPanel() {
        final SettingsShutDownHook hook = getHook();
        final String value = getKey();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        final JLabel label = new JLabel(getDescriptor());
        final JCheckBox allowed = new JCheckBox();
        allowed.setSelected(hook.getPropertyAsBoolean(value));
        allowed.addChangeListener(e -> {
            final boolean selected = allowed.isSelected();
            hook.toggleSetting(value, selected);
        });

        add(label);
        add(Box.createHorizontalGlue());
        add(allowed);
    }
}
