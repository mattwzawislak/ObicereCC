package org.obicere.cc.shutdown;

/**
 * @author Obicere
 */
public class NoSplashHook extends ShutDownHook {

    public static final String NAME = "no.splash";

    @HookValue(defaultValue = "false")
    public static final String NO_SPLASH = "no.splash";

    public NoSplashHook() {
        super(true, "Display no splash screen.", NAME, PRIORITY_RUNTIME_SHUTDOWN);
    }
}
