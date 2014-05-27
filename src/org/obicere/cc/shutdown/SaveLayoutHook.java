package org.obicere.cc.shutdown;

public class SaveLayoutHook extends ShutDownHook {

    public static final String NAME = "save.layout";

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

    public SaveLayoutHook() {
        super(true, "Save Layout", NAME, PRIORITY_WINDOW_CLOSING);
    }
}
