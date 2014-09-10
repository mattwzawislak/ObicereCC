package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.*;

/**
 * @author Obicere
 */
public class IntegerSetting extends SettingPanel {

    public IntegerSetting(final SettingsShutDownHook hook, final String key, final String description) {
        super(hook, key, description);
    }

    @Override
    protected void buildPanel() {

        final SettingsShutDownHook hook = getHook();
        final String key = getKey();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        final JLabel description = new JLabel(getDescriptor());

        final SpinnerNumberModel model = new SpinnerNumberModel(hook.getPropertyAsInt(key), 0, 70, 1);
        final JSpinner spinner = new JSpinner(model);

        spinner.addChangeListener(e -> hook.setProperty(key, spinner.getValue()));

        add(description);
        add(Box.createHorizontalGlue());
        add(spinner);
    }
}