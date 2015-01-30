package org.obicere.cc.shutdown;

import org.obicere.cc.gui.settings.SettingPanel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Obicere
 */
public abstract class SettingsShutDownHook extends ShutDownHook {

    private final Map<String, SettingPanel> settingPanels = new LinkedHashMap<>();

    private final String groupName;

    public SettingsShutDownHook(final String groupName, final String name, final int priority) {
        super(name, priority);
        this.groupName = groupName;
    }

    public Map<String, SettingPanel> getSettingPanels() {
        return settingPanels;
    }

    public void providePanel(final String value, final SettingPanel panel) {
        settingPanels.put(value, panel);
    }

    public void toggleSetting(final String name, final boolean value) {
        if (name != null) {
            setProperty(name, value);
        }
    }

    public String getGroupName() {
        return groupName;
    }

}
