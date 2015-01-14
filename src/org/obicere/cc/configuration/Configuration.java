package org.obicere.cc.configuration;

import java.awt.AWTError;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.logging.Logger;

public class Configuration {

    private static final Logger log = Logger.getLogger(Configuration.class.getCanonicalName());

    public static final Image CLOSE_IMAGE;
    public static final Image COMPLETE_IMAGE;
    public static final Image ICON;

    private static volatile OS selectedOS;

    static {
        CLOSE_IMAGE = loadImage(URLs.CLOSE);
        COMPLETE_IMAGE = loadImage(URLs.COMPLETE);
        ICON = loadImage(URLs.ICON);
    }

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

    public static String getAppData() {
        return getOS() == OS.WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
    }

    public static OS getOS() {
        if (selectedOS != null) {
            return selectedOS;
        }
        return (selectedOS = parseOS());
    }

    private static OS parseOS() {
        String os = System.getProperty("os.name").toLowerCase();
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

    public static enum OS {
        WINDOWS, MAC, LINUX, OTHER
    }

    public static class URLs {

        // Site
        public static final String HOME = "http://www.obicere.uni.me";
        public static final String BIN  = HOME + "/ccbin/";

        // Working directory/jar
        public static final String RESOURCES = "resource/";
        public static final String CLOSE     = RESOURCES + "close.png";
        public static final String COMPLETE  = RESOURCES + "complete.png";
        public static final String ICON      = RESOURCES + "icon.png";

    }
}
