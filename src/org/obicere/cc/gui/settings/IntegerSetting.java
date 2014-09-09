package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.EditorHook;
import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.*;
import java.awt.*;

/**
 * @author Obicere
 */
public class IntegerSetting extends SettingPanel {

    protected IntegerSetting(final SettingsShutDownHook hook, final String key, final String description) {
        super(hook, key, description);
    }

    @Override
    protected void buildPanel() {

        final SettingsShutDownHook hook = getHook();
        final String key = getKey();

        final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        final JLabel description = new JLabel(getDescriptor());

        final SpinnerNumberModel model = new SpinnerNumberModel(hook.getPropertyAsInt(key), 0, 70, 1);
        final JSpinner spinner = new JSpinner(model);

        spinner.addChangeListener(e -> {
            hook.setProperty(key, spinner.getValue());
        });

        panel.add(description);
        panel.add(spinner);

        add(panel);
    }
}