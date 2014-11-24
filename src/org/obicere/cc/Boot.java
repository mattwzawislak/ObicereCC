package org.obicere.cc;

import org.obicere.cc.process.AbstractLauncher;
import org.obicere.cc.process.SwingLauncher;
import org.obicere.cc.util.Argument;
import org.obicere.cc.util.ArgumentParser;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Boot {

    public static void main(final String[] args) {
        final Logger log = Logger.getLogger(Boot.class.getCanonicalName());
        final long startBoot = System.currentTimeMillis();
        final ArgumentParser parser = new ArgumentParser(args);

        final Argument launcherName = new Argument("launcher", "swing", "l");

        parser.provide(launcherName);

        parser.parse();
        final AbstractLauncher launcher;
        switch (launcherName.get()) {
            case "javafx":
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
