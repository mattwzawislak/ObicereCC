package org.obicere.cc.gui.swing.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

/**
 * @author Obicere
 */
public class BooleanSetting extends SettingPanel {

    private JCheckBox box;

    public BooleanSetting(final SettingsShutDownHook hook, final String value, final String description) {
        super(hook, value, description);
    }

    @Override
    protected void buildPanel() {
        final SettingsShutDownHook hook = getHook();
        final String value = getKey();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        final JLabel label = new JLabel(getDescriptor());

        this.box = new JCheckBox();

        box.setSelected(hook.getPropertyAsBoolean(value));
        box.addChangeListener(e -> {
            final boolean selected = box.isSelected();
            hook.toggleSetting(value, selected);
        });

        add(label);
        add(Box.createHorizontalGlue());
        add(box);
    }

    public JCheckBox getCheckBox() {
        return box;
    }
}
