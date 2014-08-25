package org.obicere.cc.shutdown;

/**
 * @author Obicere
 */
public class SplashScreenHook extends SettingsShutDownHook {

    public static final String NAME = "no.splash";

    @HookValue("false")
    public static final String NO_SPLASH = "no.splash";

    public SplashScreenHook() {
        super("Splash Screen.", NAME, PRIORITY_RUNTIME_SHUTDOWN);

        putDescription(NO_SPLASH, "Display no splash screen.");
    }
}
