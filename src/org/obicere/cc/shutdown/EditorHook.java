package org.obicere.cc.shutdown;

/**
 * @author Obicere
 */
public class EditorHook extends SettingsShutDownHook {

    public static final String NAME = "editor.settings";

    @HookValue("java.awt.Color[r=40,g=116,b=167]")
    public static final String STRING_HIGHLIGHT_COLOR = "color.highlight.string";

    @HookValue("java.awt.Color[r=120,g=43,b=8]")
    public static final String KEYWORD_HIGHLIGHT_COLOR = "color.highlight.keyword";

    @HookValue("java.awt.Color[r=36,g=36,b=36]")
    public static final String NORMAL_HIGHLIGHT_COLOR = "color.highlight.normal";

    @HookValue("Consolas")
    public static final String EDITOR_FONT_TYPE = "font.type";

    @HookValue("11")
    public static final String EDITOR_FONT_SIZE = "font.size";

    private long lastEditorUpdate = 0;

    public EditorHook() {
        super("Editor Settings", NAME, PRIORITY_WINDOW_CLOSING);
        putDescription(STRING_HIGHLIGHT_COLOR, "Highlight Color for Strings: ");
        putDescription(KEYWORD_HIGHLIGHT_COLOR, "Highlight Color for Keywords: ");
        putDescription(NORMAL_HIGHLIGHT_COLOR, "Editor font Color: ");
        putDescription(EDITOR_FONT_TYPE, "Font Type: ");
        putDescription(EDITOR_FONT_SIZE, "Font Size: ");
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
