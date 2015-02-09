package org.obicere.cc.gui.swing.settings;

import org.obicere.cc.shutdown.SettingsShutDownHook;

import javax.swing.JPanel;

/**
 * @author Obicere
 */
public abstract class SettingPanel extends JPanel {

    private final SettingsShutDownHook hook;
    private final String               key;
    private final String               description;

    protected SettingPanel(final SettingsShutDownHook hook, final String key, final String description) {
        this.hook = hook;
        this.key = key;
        this.description = description;
    }

    protected abstract void buildPanel();

    public SettingsShutDownHook getHook() {
        return hook;
    }

    public String getKey() {
        return key;
    }

    public String getDescriptor() {
        return description;
    }

}
