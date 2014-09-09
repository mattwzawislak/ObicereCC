package org.obicere.cc.configuration;

import java.util.Random;

public class Message {

    private final static String[] MESSAGES = new String[]{
            //"Han shot first.",
            //"There is no cow level.",
            //"More polygons than before!",
            //"Now with 0 calories!",
            //"Rxd4!!",
            //"PC Master Race",
            //"Now with a O(nlogn) complexity!",
            //"How Can Mirrors Be Real If Our Eyes Aren't Even Real?",
            //"import javax.swing.*;",
            //"i++;",
            //"Isn't a parsec a unit of distance?",
            //"0 < i, i + 1 < 0",
            ""

    };

    private static final Random SEED = new Random();

    public static String getRandom() {
        return MESSAGES[SEED.nextInt(MESSAGES.length)];
    }

}
