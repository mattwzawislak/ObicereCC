package org.obicere.cc.shutdown;

import org.obicere.cc.gui.swing.settings.BooleanSetting;
import org.obicere.cc.gui.swing.settings.ColorSetting;
import org.obicere.cc.gui.swing.settings.FontSetting;
import org.obicere.cc.gui.swing.settings.IntegerSetting;

/**
 * @author Obicere
 */
public class EditorHook extends SettingsShutDownHook {

    private static final String GROUP_NAME = "Editor Settings";

    public static final String NAME = "editor.settings";

    @HookValue("true")
    public static final String ENABLED_STYLING             = "color.styling.enabled";
    public static final String ENABLED_STYLING_DESCRIPTION = "Allow styling of editor";

    @HookValue("40,116,167")
    public static final String STRING_STYLING_COLOR       = "color.styling.string";
    public static final String STRING_STYLING_DESCRIPTION = "Styling color for literals: ";

    @HookValue("120,43,8")
    public static final String KEYWORD_STYLING_COLOR       = "color.styling.keyword";
    public static final String KEYWORD_STYLING_DESCRIPTION = "Styling color for keywords: ";

    @HookValue("36,36,36")
    public static final String NORMAL_STYLING_COLOR       = "color.styling.normal";
    public static final String NORMAL_STYLING_DESCRIPTION = "Editor text color: ";

    @HookValue("Consolas")
    public static final String EDITOR_FONT_TYPE             = "font.type";
    public static final String EDITOR_FONT_TYPE_DESCRIPTION = "Font Type: ";

    @HookValue("14")
    public static final String EDITOR_FONT_SIZE             = "font.size";
    public static final String EDITOR_FONT_SIZE_DESCRIPTION = "Font Size: ";

    private long lastEditorUpdate = 0;

    public EditorHook() {
        super(GROUP_NAME, NAME, PRIORITY_WINDOW_CLOSING);
        providePanel(ENABLED_STYLING, new BooleanSetting(this, ENABLED_STYLING, ENABLED_STYLING_DESCRIPTION));
        providePanel(STRING_STYLING_COLOR, new ColorSetting(this, STRING_STYLING_COLOR, STRING_STYLING_DESCRIPTION));
        providePanel(KEYWORD_STYLING_COLOR, new ColorSetting(this, KEYWORD_STYLING_COLOR, KEYWORD_STYLING_DESCRIPTION));
        providePanel(NORMAL_STYLING_COLOR, new ColorSetting(this, NORMAL_STYLING_COLOR, NORMAL_STYLING_DESCRIPTION));
        providePanel(EDITOR_FONT_SIZE, new IntegerSetting(this, EDITOR_FONT_SIZE, EDITOR_FONT_SIZE_DESCRIPTION));
        providePanel(EDITOR_FONT_TYPE, new FontSetting(this, EDITOR_FONT_TYPE, EDITOR_FONT_TYPE_DESCRIPTION));
    }

    public long getLastEditorUpdate() {
        return lastEditorUpdate;
    }

    @Override
    public void setProperty(final String key, final Object value) {
        super.setProperty(key, value);
        lastEditorUpdate = System.currentTimeMillis();
    }

}
