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

import java.util.Random;

/**
 * Used for the splash screen to display useful information about the client.
 *
 * @author Obicere
 * @since 1.0
 */

public class Message {

    private final static String[] MESSAGES = new String[]{
            "Han shot first.",
            "There is no cow level.",
            "More polygons than before!",
            "Now with 0 calories!"
    };

    private static final Random SEED = new Random();

    /**
     * Returns a method for which you can return a tip from the list of tips.
     * This should only really be used in the initial boot of the application.
     *
     * @return A random tip to be used in the splash screen.
     * @since 1.0
     */

    public static String getRandom() {
        return MESSAGES[SEED.nextInt(MESSAGES.length)];
    }

}
