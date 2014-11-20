package org.obicere.cc.process;

import org.obicere.cc.configuration.Domain;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

/**
 * @author Obicere
 */
public class SwingLauncher extends AbstractLauncher {

    @Override
    public void launch() {
        final Domain access = new Domain();
        access.getPaths().run();
        access.getLanguageManager().run();
        access.getHookManager().run();
        try {
            SwingUtilities.invokeAndWait(access.getSplash()::run);
        } catch (final InterruptedException ignored) {
            return;
        } catch (final InvocationTargetException e) {
            log.log(Level.SEVERE, "Failed to create splash instance.");
            e.printStackTrace();
            return;
        }
        access.getUpdater().run();
        access.getSplash().setStatus("Loading framework");
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
            access.getFrameManager().buildGUI();
            access.getSplash().shouldDispose(true);
            access.getSplash().dispose();
        });
    }
}
