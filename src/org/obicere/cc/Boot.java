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

public class Boot {

    public static void main(final String[] args) throws URISyntaxException, ClassNotFoundException {
        LanguageHandler.load();
        ShutDownHookManager.setup();
        final SplashScreenHook hook = ShutDownHookManager.hookByName(SplashScreenHook.class, SplashScreenHook.NAME);
        final boolean splash = !hook.getPropertyAsBoolean(SplashScreenHook.NO_SPLASH);
        try {
            if (splash) {
                SwingUtilities.invokeAndWait(Splash::display);
            }
        } catch (final InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Paths.build();
        Updater.update();
        Splash.setStatus("Loading framework");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                FrameManager.buildGUI();
                if (splash) {
                    Splash.getInstance().shouldDispose(true);
                    Splash.getInstance().getFrame().dispose();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
    }
}
