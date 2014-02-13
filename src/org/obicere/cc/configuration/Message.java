

package org.obicere.cc.configuration;

import java.util.Random;



public class Message {

    private final static String[] MESSAGES = new String[]{
            "Han shot first.",
            "There is no cow level.",
            "More polygons than before!",
            "Now with 0 calories!"
    };

    private static final Random SEED = new Random();



    public static String getRandom() {
        return MESSAGES[SEED.nextInt(MESSAGES.length)];
    }

}
