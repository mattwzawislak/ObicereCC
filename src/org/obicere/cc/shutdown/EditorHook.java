package org.obicere.cc.shutdown;

import org.obicere.cc.gui.settings.ColorSetting;
import org.obicere.cc.gui.settings.FontSetting;
import org.obicere.cc.gui.settings.IntegerSetting;

/**
 * @author Obicere
 */
public class EditorHook extends SettingsShutDownHook {

    public static final String NAME = "editor.settings";

    @HookValue("java.awt.Color[r=40,g=116,b=167]")
    public static final String STRING_HIGHLIGHT_COLOR       = "color.highlight.string";
    public static final String STRING_HIGHLIGHT_DESCRIPTION = "Highlight Color for Strings: ";

    @HookValue("java.awt.Color[r=120,g=43,b=8]")
    public static final String KEYWORD_HIGHLIGHT_COLOR       = "color.highlight.keyword";
    public static final String KEYWORD_HIGHLIGHT_DESCRIPTION = "Highlight Color for Keywords: ";

    @HookValue("java.awt.Color[r=36,g=36,b=36]")
    public static final String NORMAL_HIGHLIGHT_COLOR       = "color.highlight.normal";
    public static final String NORMAL_HIGHLIGHT_DESCRIPTION = "Editor font Color: ";

    @HookValue("Consolas")
    public static final String EDITOR_FONT_TYPE             = "font.type";
    public static final String EDITOR_FONT_TYPE_DESCRIPTION = "Font Type: ";

    @HookValue("14")
    public static final String EDITOR_FONT_SIZE             = "font.size";
    public static final String EDITOR_FONT_SIZE_DESCRIPTION = "Font Size: ";

    private long lastEditorUpdate = 0;

    public EditorHook() {
        super("Editor Settings", NAME, PRIORITY_WINDOW_CLOSING);
        providePanel(STRING_HIGHLIGHT_COLOR, new ColorSetting(this, STRING_HIGHLIGHT_COLOR, STRING_HIGHLIGHT_DESCRIPTION));
        providePanel(KEYWORD_HIGHLIGHT_COLOR, new ColorSetting(this, KEYWORD_HIGHLIGHT_COLOR, KEYWORD_HIGHLIGHT_DESCRIPTION));
        providePanel(NORMAL_HIGHLIGHT_COLOR, new ColorSetting(this, NORMAL_HIGHLIGHT_COLOR, NORMAL_HIGHLIGHT_DESCRIPTION));
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
