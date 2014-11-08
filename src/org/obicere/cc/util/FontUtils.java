package org.obicere.cc.util;

import java.awt.GraphicsEnvironment;

/**
 * @author Obicere
 */
public class FontUtils {

    private static final String[] FONT_LISTING;

    static {
        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        FONT_LISTING = env.getAvailableFontFamilyNames();
    }

    public static String[] getLoadedFonts() {
        return FONT_LISTING;
    }

    public static boolean isFont(final String str) {
        if (str == null) {
            return false;
        }
        for (final String font : FONT_LISTING) {
            if (str.equalsIgnoreCase(font)) {
                return true;
            }
        }
        return false;
    }

}
