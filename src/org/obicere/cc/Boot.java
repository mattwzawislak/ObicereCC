package org.obicere.cc;

import com.alee.laf.WebLookAndFeel;
import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.executor.language.LanguageHandler;
import org.obicere.cc.gui.GUI;
import org.obicere.cc.gui.Splash;
import org.obicere.cc.methods.Reflection;
import org.obicere.cc.methods.Updater;
import org.obicere.cc.shutdown.NoSplashHook;
import org.obicere.cc.shutdown.ShutDownHook;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.tasks.projects.Manifest;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class Boot {

    public static void main(final String[] args) throws URISyntaxException {
        LanguageHandler.load();
        ShutDownHookManager.setup();
        final NoSplashHook hook = ShutDownHookManager.hookByName(NoSplashHook.class, NoSplashHook.NAME);
        final boolean splash = !hook.getPropertyAsBoolean(NoSplashHook.NO_SPLASH);
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
                UIManager.setLookAndFeel(new WebLookAndFeel());
                GUI.buildGUI();
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
