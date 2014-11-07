package org.obicere.cc;

import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.executor.language.LanguageHandler;
import org.obicere.cc.gui.FrameManager;
import org.obicere.cc.gui.Splash;
import org.obicere.cc.methods.Updater;
import org.obicere.cc.shutdown.ShutDownHookManager;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Boot {

    private static final Logger log = Logger.getLogger(Boot.class.getCanonicalName());

    public static void main(final String[] args) {
        final long startBoot = System.currentTimeMillis();
        Paths.build();
        LanguageHandler.load();
        ShutDownHookManager.setup();
        try {
            SwingUtilities.invokeAndWait(Splash::display);
        } catch (final InterruptedException ignored) {
            return;
        } catch (final InvocationTargetException e) {
            log.log(Level.SEVERE, "Failed to create splash instance.");
            e.printStackTrace();
            return;
        }
        Updater.update();
        Splash.setStatus("Loading framework");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (final ClassNotFoundException e) {
                log.warning("Could not find system look and feel.");
                e.printStackTrace();
            } catch (final UnsupportedLookAndFeelException e) {
                log.log(Level.WARNING, "Look and feel is not supported by this JVM; LAF={0}, VM={1}.", new Object[]{UIManager.getSystemLookAndFeelClassName(), System.getProperty("java.version")});
                e.printStackTrace();
            } catch (final InstantiationException e) {
                log.log(Level.WARNING, "Failed to create look and feel instance for LAF={0}.", UIManager.getSystemLookAndFeelClassName());
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                log.log(Level.WARNING, "Look and feel class is not accessible for LAF={0}.", UIManager.getSystemLookAndFeelClassName());
                e.printStackTrace();
            }
            FrameManager.buildGUI();
            Splash.getInstance().shouldDispose(true);
            Splash.getInstance().dispose();
        });
        final long bootTime = System.currentTimeMillis() - startBoot;
        log.log(Level.INFO, "Boot time: {0}ms", bootTime);
    }
}
