package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Obicere
 */
public class StringSetting extends SettingPanel {

    public StringSetting(final SettingsShutDownHook hook, final String key, final String description) {
        super(hook, key, description);
    }

    @Override
    protected void buildPanel() {

        final SettingsShutDownHook hook = getHook();
        final String key = getKey();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        final JLabel descriptor = new JLabel(getDescriptor());
        final JTextField field = new JTextField(hook.getPropertyAsString(key)); // Set default

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                update();
            }

            private void update(){
                hook.setProperty(key, field.getText());
            }
        });

        add(descriptor);
        add(Box.createHorizontalGlue());
        add(field);
    }
}
