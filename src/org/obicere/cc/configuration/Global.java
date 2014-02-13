

package org.obicere.cc.configuration;

import org.obicere.cc.gui.Splash;
import org.obicere.cc.methods.Updater;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;


public class Global {

    public static final Image ICON_IMAGE;
    public static final Image CLOSE_IMAGE;
    public static final Image COMPLETE_IMAGE;
    public static final Image ANIMATION_IMAGE;



    static {
        ICON_IMAGE = load(URLs.ICON);
        CLOSE_IMAGE = load(URLs.CLOSE);
        COMPLETE_IMAGE = load(URLs.COMPLETE);
        ANIMATION_IMAGE = load(URLs.ANIMATION);
    }

    private static Image load(final String url) {
        try {
            final Toolkit tk = Toolkit.getDefaultToolkit();
            final URL path = Global.class.getClassLoader().getResource(url);
            final Image img = tk.createImage(path);
            tk.prepareImage(img, -1, -1, null);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public static class Paths {

        public static final String APP_DATA = getAppData();
        public static final String HOME = APP_DATA + File.separator + "ObicereCC";
        public static final String SOURCE = HOME + File.separator + "src";
        public static final String DATA = HOME + File.separator + "data";
        public static final String LANGUAGE = DATA + File.separator + "language";
        public static final String LAYOUT_SAVE_FILE = DATA + File.separator + "layout.properties";
        public static final String[] PATHS = new String[]{APP_DATA, HOME, SOURCE, DATA, LANGUAGE};



        public static void build() {
            for (final String s : PATHS) {
                final File file = new File(s);
                if (!file.exists()) {
                    Splash.setStatus("Creating Directory: " + s);
                    if (file.mkdir()) {
                        System.out.println("Created " + file);
                    }
                }
            }
            final File complete = new File(DATA + File.separator + "data.dat");
            if (!complete.exists()) {
                try {
                    if (complete.createNewFile()) {
                        System.out.println("Created " + complete);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to create data file.");
                    System.err.println("Completion progress will not be saved, code still will be.");
                }
            }
        }
    }



    public static class URLs {

        public static final String HOME = "http://www.obicere.uni.me";
        public static final String BIN = HOME + "/ccbin/";
        public static final String RESOURCES = "resource/";
        public static final String ICON = RESOURCES + "icon.png";
        public static final String CLOSE = RESOURCES + "close.png";
        public static final String COMPLETE = RESOURCES + "complete.png";
        public static final String ANIMATION = RESOURCES + "gear_spin.gif";
        public static final String COMPILERS = RESOURCES + "compilers/";
        public static final String LANGUAGES = RESOURCES + "languages/";
    }


    public static enum OS {
        WINDOWS, MAC, LINUX, OTHER
    }


    public static String getAppData() {
        return getOS() == OS.WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
    }


    public static OS getOS() {
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
}
