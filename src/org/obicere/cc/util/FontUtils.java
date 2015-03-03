package org.obicere.cc.util;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * Basic font utilities for handling the loading of fonts. This utility
 * should be used as a shortcut for verbose font patterns and for caching
 * font information.
 * <p>
 * As of <code>v1.0</code>, the only support added for fonts is loading all
 * possible font family names.
 *
 * @author Obicere
 * @version 1.0
 */
public class FontUtils {

    private static final String[] FONT_LISTING;

    static {
        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        FONT_LISTING = env.getAvailableFontFamilyNames();
    }

    private FontUtils() {
        throw new AssertionError("Nope");
    }

    /**
     * Retrieves a mutable list of loaded font names. This list, loaded
     * from the {@link java.awt.GraphicsEnvironment} should be already
     * sorted. If not, this method can be used as a pass-through to perform
     * a sort operation.
     *
     * @return The list of font family names loaded by the {@link
     * java.awt.GraphicsEnvironment}
     * @see java.awt.GraphicsEnvironment#getAvailableFontFamilyNames()
     */

    public static String[] getLoadedFonts() {
        return FONT_LISTING;
    }

    /**
     * Checks to see if the given string matches the name of a font family.
     * Matching is handled case-insensitive, so <code>vErDaNa</code> will
     * match if and only if <code>Verdana</code> matches.
     * <p>
     * The search is handled through binary order, so the given font
     * listing should be already sorted. All {@link java.awt.GraphicsEnvironment}s
     * I've encountered so far automatically sorts the list of font names.
     * Presumably for this very reason.
     *
     * @param str The {@link java.lang.String} to search the list of family
     *            fonts for a search.
     * @return <code>true</code> if the given <code>str</code> matches a
     * font family name, without considering casing.
     * @see java.awt.GraphicsEnvironment#getAvailableFontFamilyNames()
     */

    public static boolean isFont(final String str) {
        if (str == null) {
            return false;
        }
        if (str.length() == 0) {
            return false;
        }
        final int index = Arrays.binarySearch(FONT_LISTING, str, String.CASE_INSENSITIVE_ORDER);
        return index >= 0;
    }

}
