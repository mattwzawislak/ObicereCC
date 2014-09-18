package org.obicere.cc;

import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.executor.language.LanguageHandler;
import org.obicere.cc.gui.FrameManager;
import org.obicere.cc.gui.Splash;
import org.obicere.cc.methods.Updater;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.shutdown.SplashScreenHook;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Boot {

    private static final Logger LOGGER = Logger.getLogger(Boot.class.getCanonicalName());

    public static void main(final String[] args) throws URISyntaxException, ClassNotFoundException {
        final long startBoot = System.currentTimeMillis();
        Paths.build();
        LanguageHandler.load();
        ShutDownHookManager.setup();
        final SplashScreenHook hook = ShutDownHookManager.hookByClass(SplashScreenHook.class);
        final boolean splash = !hook.getPropertyAsBoolean(SplashScreenHook.NO_SPLASH);
        try {
            if (splash) {
                SwingUtilities.invokeAndWait(Splash::display);
            }
        } catch (final InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Updater.update();
        Splash.setStatus("Loading framework");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                FrameManager.buildGUI();
                if (splash) {
                    Splash.getInstance().shouldDispose(true);
                    Splash.getInstance().dispose();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
        final long bootTime = System.currentTimeMillis() - startBoot;
        LOGGER.log(Level.INFO, "Boot time: {0}ms", bootTime);
    }
}
