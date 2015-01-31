package org.obicere.cc.shutdown;

import org.obicere.cc.gui.settings.BooleanSetting;

public class LayoutHook extends SettingsShutDownHook {

    public static final String NAME = "save.layout";

    @HookValue("true")
    public static final String SAVE_LAYOUT             = "save.layout";
    public static final String SAVE_LAYOUT_DESCRIPTION = "Save the layout: ";

    @HookValue("900")
    public static final String PROPERTY_FRAME_WIDTH = "frame.width";

    @HookValue("600")
    public static final String PROPERTY_FRAME_HEIGHT = "frame.height";

    @HookValue("0")
    public static final String PROPERTY_FRAME_STATE = "frame.state";

    @HookValue("300")
    public static final String PROPERTY_MAINSPLIT_DIVIDER_LOCATION = "mainsplit.divider.location";

    @HookValue("100")
    public static final String PROPERTY_TEXTSPLIT_DIVIDER_LOCATION = "textsplit.divider.location";

    public LayoutHook() {
        super("Save Layout", NAME, PRIORITY_WINDOW_CLOSING);

        providePanel(SAVE_LAYOUT, new BooleanSetting(this, SAVE_LAYOUT, SAVE_LAYOUT_DESCRIPTION));
    }
}
