package org.obicere.cc;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.gui.SwingFrameManager;
import org.obicere.cc.process.AbstractLauncher;
import org.obicere.cc.process.SwingLauncher;
import org.obicere.cc.util.Argument;
import org.obicere.cc.util.ArgumentParser;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Obicere
 * @version 1.0
 * @see #main(String[])
 */

public class Boot {

    /**
     * Starts the default application-based launcher. As of v1.0 this is
     * the only supported method of running the application.
     * <p>
     * No specific VM arguments are needed.
     * <p>
     * If on a Windows machine, enabling the arguments for the TCP LoopBack
     * may increase speeds when compiling non-Java languages. This can be
     * done (if supported on your VM) through <code></code>windows.enableFastLocalTcpLoopback</code>.
     * <p>
     * The list of supported program arguments and their usage is as
     * follows:
     * <pre>
     * -launcher &lt;launcher-type&gt;
     *     Aliases: l
     *     Available Arguments: swing (default)
     * </pre>
     * <p>
     *
     * @param args As specified above under the list of supported program
     *             arguments.
     */

    public static void main(final String[] args) {
        final Logger log = Logger.getLogger(Boot.class.getCanonicalName());
        final long startBoot = System.currentTimeMillis();
        final ArgumentParser parser = new ArgumentParser(args);

        final Argument launcherName = new Argument("launcher", "swing", "l");

        parser.provide(launcherName);

        parser.parse();

        final Domain access = new Domain();
        final AbstractLauncher launcher;
        switch (launcherName.get()) {
            case "javafx":
            case "awt":
                log.warning("Launcher type not supported at this time. Defaulting to swing.");
            case "swing":
                launcher = new SwingLauncher(access);
                access.setFrameManager(new SwingFrameManager(access));
                break;
            default:
                throw new IllegalArgumentException("Invalid argument for default launcher: " + launcherName.get());
        }
        launcher.launch();

        final long bootTime = System.currentTimeMillis() - startBoot;
        log.log(Level.INFO, "Boot time: {0}ms", bootTime);
    }
}

