package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author Obicere
 */
public class IntegerSetting extends SettingPanel {

    private static final int DEFAULT_MIN       = 0;   // For now, integer settings are only used for
    private static final int DEFAULT_MAX       = 70;  // setting font sizes. This can be adjusted
    private static final int DEFAULT_INCREMENT = 1;   // if requested.

    public IntegerSetting(final SettingsShutDownHook hook, final String key, final String description) {
        super(hook, key, description);
    }

    @Override
    protected void buildPanel() {

        final SettingsShutDownHook hook = getHook();
        final String key = getKey();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        final JLabel description = new JLabel(getDescriptor());

        final SpinnerNumberModel model = new SpinnerNumberModel(hook.getPropertyAsInt(key), DEFAULT_MIN, DEFAULT_MAX, DEFAULT_INCREMENT);
        final JSpinner spinner = new JSpinner(model);

        spinner.addChangeListener(e -> hook.setProperty(key, spinner.getValue()));

        add(description);
        add(Box.createHorizontalGlue());
        add(spinner);
    }
}