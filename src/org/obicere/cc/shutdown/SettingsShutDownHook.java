package org.obicere.cc.shutdown;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Obicere
 */
public abstract class SettingsShutDownHook extends ShutDownHook {

    private final Map<String, String> settingDescriptions = new HashMap<>();

    private final String groupName;

    public SettingsShutDownHook(final String groupName, final String name, final int priority) {
        super(name, priority);
        this.groupName = groupName;
    }

    public Map<String, String> getSettingDescriptions() {
        return settingDescriptions;
    }

    public void putDescription(final String value, final String description) {
        settingDescriptions.put(value, description);
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
