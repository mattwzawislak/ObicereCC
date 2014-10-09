package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * @author Obicere
 */
public class ButtonSetting extends SettingPanel {

    private final String   action;
    private final Runnable task;

    public ButtonSetting(final SettingsShutDownHook hook, final String key, final String description, final String action, final Runnable task) {
        super(hook, key, description);
        this.action = action;
        this.task = task;
    }

    @Override
    protected void buildPanel() {

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        final JLabel descriptor = new JLabel(getDescriptor());
        final JButton button = new JButton(action);

        button.addActionListener(e -> task.run());

        add(descriptor);
        add(Box.createHorizontalGlue());
        add(button);
    }
}
