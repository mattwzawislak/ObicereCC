package org.obicere.cc;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.process.AbstractLauncher;
import org.obicere.cc.process.SwingLauncher;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Boot {

    public static void main(final String[] args) {
        final Logger log = Logger.getLogger(Boot.class.getCanonicalName());
        final long startBoot = System.currentTimeMillis();

        final AbstractLauncher launcher;
        if(args.length == 0){
            launcher = new SwingLauncher();
        } else {
            launcher = null;
        }
        if(launcher == null){
            log.severe("Failed to launch program with specified program arguments.");
            return;
        }
        launcher.launch();

        final long bootTime = System.currentTimeMillis() - startBoot;
        log.log(Level.INFO, "Boot time: {0}ms", bootTime);
    }
}
