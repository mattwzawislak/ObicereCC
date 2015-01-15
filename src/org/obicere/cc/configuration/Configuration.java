package org.obicere.cc.configuration;

import java.awt.AWTError;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.logging.Logger;

/**
 * The configuration is meant for handling system-specific settings and
 * resources. All of the loaded resources are static so an instance is
 * neither needed or accessible. The class itself replicates a utility
 * class, but pertains to information for I/O - which is part of the
 * configuration system.
 * <p>
 * Operating system information is also contained within the configuration.
 * As of v1.0 only the OS type is registered.
 * <p>
 * todo: consider adding JVM information, OS properties and architecture
 *
 * @author Obicere
 * @version 1.0
 */

public class Configuration {

    private static final Logger log = Logger.getLogger(Configuration.class.getCanonicalName());

    public static final Image CLOSE_IMAGE;
    public static final Image COMPLETE_IMAGE;
    public static final Image ICON;

    private static final OS SYSTEM_OS;

    static {
        CLOSE_IMAGE = loadImage(Paths.RESOURCES_CLOSE);
        COMPLETE_IMAGE = loadImage(Paths.RESOURCES_COMPLETE);
        ICON = loadImage(Paths.RESOURCES_ICON);

        SYSTEM_OS = parseOS();
    }

    /**
     * Should not be used.
     *
     * @throws java.lang.IllegalAccessError if attempting to instantiate
     *                                      the class through reflection or
     *                                      some other means.
     */

    private Configuration() {
        throw new IllegalAccessError();
    }

    /**
     * Loads the image from the specified URL through the default class
     * loader. This method has been tested and works on the following
     * formats:
     * <pre>
     *     png
     *     jpeg, jpeg2000
     *     gif
     * </pre>
     * <p>
     * The image is also prepared by the default {@link java.awt.Toolkit}
     *
     * @param url The URL to load the image from.
     * @return The loaded image, if successful. Otherwise
     * <code>null</code>.
     */

    private static Image loadImage(final String url) {
        try {
            final Toolkit tk = Toolkit.getDefaultToolkit();
            final URL path = Configuration.class.getClassLoader().getResource(url);
            final Image img = tk.createImage(path);
            tk.prepareImage(img, -1, -1, null);
            return img;
        } catch (final SecurityException e) {
            log.severe("Insufficient permissions to load image.");
            return null;
        } catch (final AWTError e) {
            log.severe("Could not instantiate default toolkit to load images.");
            return null;
        }
    }

    /**
     * Retrieves the default application storage folder for programs. This
     * is based off of the current operating system.
     * <p>
     * <pre>
     *     For Windows machines:    <code>%APPDATA%</code>
     *     For other machines:      <code>user.home</code>
     * </pre>
     * <p>
     * This has only been tested on Windows machines.
     *
     * @return The default application data folder.
     * @see #getOS()
     */

    public static String getAppData() {
        return getOS() == OS.WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
    }

    /**
     * Returns the value of the parsed operating system.
     *
     * @return The parsed OS value.
     * @see org.obicere.cc.configuration.Configuration.OS
     * @see #parseOS()
     */

    public static OS getOS() {
        return SYSTEM_OS;
    }

    /**
     * Attempts to parse the operating system from the system properties.
     * Namely the <code>os.name</code> property. If one cannot be found, it
     * will return the {@link OS#OTHER} value. Otherwise it will return the
     * appropriate value - I hope - in accordance to the operating system.
     * <p>
     * If the <code>os.name</code> contains ignoring case:
     * <pre>
     *     "windows";   returns {@link OS#WINDOWS}
     *     "mac":       returns {@link OS#MAC}
     *     "linux":     returns {@link OS#LINUX}
     *     default:     returns {@link OS#OTHER}
     * </pre>
     * <p>
     * Should there be any error, help would be great getting the right
     * values.
     *
     * @return Hopefully the correct operating system value.
     */

    private static OS parseOS() {
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return OS.WINDOWS;
        }
        if (os.contains("mac")) {
            return OS.MAC;
        }
        if (os.contains("linux")) {
            return OS.LINUX;
        }
        return OS.OTHER;
    }

    /**
     * The set of possible operating systems supported by the program.
     * There is a default value: {@link #OTHER}. Apart from that, the
     * values are related by name. The initial notion was in the case the
     * available operating system values need to be listed.
     * <p>
     * Pretty straight forward. These really could be replaced by just
     * constant <code>int</code> values as opposed to an
     * <code>enum</code>.
     */

    public static enum OS {
        WINDOWS, MAC, LINUX, OTHER
    }
}
