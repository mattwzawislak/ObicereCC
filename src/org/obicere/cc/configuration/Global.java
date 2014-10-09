package org.obicere.cc.configuration;

import java.awt.AWTError;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Global {

    private static final Logger log = Logger.getLogger(Global.class.getCanonicalName());

    public static final Image CLOSE_IMAGE;
    public static final Image COMPLETE_IMAGE;
    public static final Image ICON;

    private static OS selectedOS;

    static {
        CLOSE_IMAGE = loadImage(URLs.CLOSE);
        COMPLETE_IMAGE = loadImage(URLs.COMPLETE);
        ICON = loadImage(URLs.ICON);
    }

    private static Image loadImage(final String url) {
        try {
            final Toolkit tk = Toolkit.getDefaultToolkit();
            final URL path = Global.class.getClassLoader().getResource(url);
            final Image img = tk.createImage(path);
            tk.prepareImage(img, -1, -1, null);
            return img;
        } catch (final SecurityException e) {
            log.warning("Insufficient permissions to load image.");
            return null;
        } catch (final AWTError e) {
            log.warning("Could not instantiate default toolkit.");
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

    public static class Paths {

        public static final String   APP_DATA = getAppData();
        public static final String   HOME     = APP_DATA + File.separator + "ObicereCC";
        public static final String   SOURCES  = HOME + File.separator + "sources";
        public static final String   DATA     = HOME + File.separator + "data";
        public static final String   LANGUAGE = DATA + File.separator + "language";
        public static final String[] PATHS    = new String[]{APP_DATA, HOME, SOURCES, DATA, LANGUAGE};

        public static void build() {
            for (final String s : PATHS) {
                final File file = new File(s);
                if (!file.exists() && !file.mkdir()) {
                    log.log(Level.WARNING, "Failed to create folder {0}.", file);
                }
            }
        }
    }

    public static class URLs {

        public static final String HOME      = "http://www.obicere.uni.me";
        public static final String BIN       = HOME + "/ccbin/";
        public static final String RESOURCES = "resource/";
        public static final String CLOSE     = RESOURCES + "close.png";
        public static final String COMPLETE  = RESOURCES + "complete.png";
        public static final String ICON      = RESOURCES + "icon.png";

    }
}
