package org.obicere.cc.shutdown;

import org.obicere.cc.gui.settings.BooleanSetting;

/**
 * @author Obicere
 */
public class SplashScreenHook extends SettingsShutDownHook {

    public static final String NAME = "splash.screen";

    @HookValue("false")
    public static final String NO_SPLASH             = "no.splash";
    public static final String NO_SPLASH_DESCRIPTION = "Display no splash screen: ";

    public SplashScreenHook() {
        super("Splash Screen.", NAME, PRIORITY_RUNTIME_SHUTDOWN);
        providePanel(NO_SPLASH, new BooleanSetting(this, NO_SPLASH, NO_SPLASH_DESCRIPTION));
    }
}
