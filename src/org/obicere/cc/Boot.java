

package org.obicere.cc;

import com.alee.laf.WebLookAndFeel;
import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.executor.ProcessRunner;
import org.obicere.cc.gui.GUI;
import org.obicere.cc.gui.Splash;
import org.obicere.cc.methods.Updater;
import org.obicere.cc.shutdown.ShutDownHookManager;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;



public class Boot {



    public static void main(final String[] args) {
        ShutDownHookManager.setup();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Splash.display();
                }
            });
        } catch (final InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.exit(0);
        }
        Paths.build();
        Updater.update();
        Splash.setStatus("Loading framework");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(new WebLookAndFeel());
                    GUI.buildGUI();
                    Splash.getInstance().shouldDispose(true);
                    Splash.getInstance().getFrame().dispose();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
