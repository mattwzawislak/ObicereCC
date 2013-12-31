/*
This file is part of ObicereCC.

ObicereCC is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ObicereCC is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ObicereCC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.obicere.cc.configuration;

import org.obicere.cc.gui.Splash;
import org.obicere.cc.methods.Updater;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Used for file, directory and image creations. Contains information regarding
 * URLs, operating system information and file directories.
 *
 * @author Obicere
 * @see URLs
 * @see Paths
 * @since 1.0
 */
public class Global {

    public static final Image ICON_IMAGE;
    public static final Image CLOSE_IMAGE;
    public static final Image COMPLETE_IMAGE;
    public static final Image ANIMATION_IMAGE;

    /**
     * Loads the images. Preferably only ran once in the boot to avoid
     * unnecessary loading.
     *
     * @since 1.0
     */

    static {
        ICON_IMAGE = load(URLs.ICON);
        CLOSE_IMAGE = load(URLs.CLOSE);
        COMPLETE_IMAGE = load(URLs.COMPLETE);
        ANIMATION_IMAGE = load(URLs.ANIMATION);
    }

    private static Image load(final String url) {
        try {
            final Toolkit tk = Toolkit.getDefaultToolkit();
            final URL path = Global.class.getResource(url);
            final Image img = tk.createImage(path);
            tk.prepareImage(img, -1, -1, null);
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * For path configurations. Depends on {@link OS} for certain dependencies.
     *
     * @since 1.0
     */

    public static class Paths {

        public static final String APP_DATA = getAppData();
        public static final String HOME = APP_DATA + File.separator + "ObicereCC";
        public static final String SOURCE = HOME + File.separator + "src";
        public static final String SETTINGS = HOME + File.separator + "data";
        public static final String JAVA = SETTINGS + File.separator + "java";
        public static final String LANGUAGE = SETTINGS + File.separator + "language";
        public static final String LAYOUT_SAVE_FILE = SETTINGS + File.separator + "layout.properties";
        public static final String[] PATHS = new String[]{APP_DATA, HOME, SOURCE, SETTINGS, JAVA, LANGUAGE};

        /**
         * Used to build external folder sets. Internal use only.
         *
         * @since 1.0
         */

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
            final File complete = new File(SETTINGS + File.separator + "data.dat");
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

    /**
     * The default URLs for all of ObicereCC's necessities such as images,
     * version information and packaged Runners.
     *
     * @see {@link Updater#update()}
     * @since 1.0
     */

    public static class URLs {

        public static final String HOME = "http://www.obicere.uni.me";
        public static final String BIN = HOME + "/ccbin/";
        public static final String RESOURCES = "/resource/";
        public static final String ICON = RESOURCES + "icon.png";
        public static final String CLOSE = RESOURCES + "close.png";
        public static final String COMPLETE = RESOURCES + "complete.png";
        public static final String ANIMATION = RESOURCES + "gear_spin.gif";
    }

    /**
     * To switch and find the current Operating system.
     *
     * @see Global#getAppData()
     * @see Global#getOS()
     * @since 1.0
     */
    public enum OS {
        WINDOWS, MAC, LINUX, OTHER
    }

    /**
     * Returns the location of the parent storage directory.
     *
     * @return <tt>String</tt> representation of the <tt>AppData</tt> or
     *         <tt>user.home</tt> directory
     * @see Global#getOS()
     * @since 1.0
     */
    public static String getAppData() {
        return getOS() == OS.WINDOWS ? System.getenv("APPDATA") : System.getProperty("user.home");
    }

    /**
     * Returns the OS system for basic configuration.
     *
     * @return Returns the <tt>enumerator</tt> of type {@link OS}
     * @see Global#getAppData()
     * @since 1.0
     */
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
