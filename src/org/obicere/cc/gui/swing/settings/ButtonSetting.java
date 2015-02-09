package org.obicere.cc.gui.swing.settings;

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

    private JButton button;

    public ButtonSetting(final SettingsShutDownHook hook, final String key, final String description, final String action, final Runnable task) {
        super(hook, key, description);
        this.action = action;
        this.task = task;
    }

    @Override
    protected void buildPanel() {

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        final JLabel descriptor = new JLabel(getDescriptor());
        this.button = new JButton(action);

        button.addActionListener(e -> task.run());

        add(descriptor);
        add(Box.createHorizontalGlue());
        add(button);
    }

    public JButton getButton() {
        return button;
    }
}
