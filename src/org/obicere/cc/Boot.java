package org.obicere.cc;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.process.AbstractLauncher;
import org.obicere.cc.process.SwingLauncher;
import org.obicere.cc.util.Argument;
import org.obicere.cc.util.ArgumentParser;

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
        final ArgumentParser parser = new ArgumentParser(args);

        final Argument<String> launcherName = new Argument<String>("launcher", "swing", "l");

        parser.parse();
        final AbstractLauncher launcher;
        switch (launcherName.get()){
            case "jfx":
            case "awt":
                log.warning("Launcher type not supported at this time. Defaulting to swing.");
            case "swing":
                launcher = new SwingLauncher();
                break;
                default:
                    throw new IllegalArgumentException("Invalid argument for default launcher: " + launcherName.get());
        }
        launcher.launch();

        final long bootTime = System.currentTimeMillis() - startBoot;
        log.log(Level.INFO, "Boot time: {0}ms", bootTime);
    }
}
