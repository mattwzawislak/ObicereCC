package org.obicere.cc.configuration;

import java.util.Random;

public class Message {

    private final static String[] MESSAGES = new String[]{
            // Games
            "There is no cow level.",
            "PC Master Race",

            // Chess
            "Rxd4!!",

            // Shitty advertising
            "Now with 0 calories!",

            // Jaden Smith Quotes
            "How Can Mirrors Be Real If Our Eyes Aren't Even Real?",
            "Trees Are Never Sad Look At Them Every Once In Awhile\nThey're Quite Beautiful.",

            // Programming
            "Now with an O(nlogn) complexity!",
            "import javax.swing.*;",
            "i++;",
            "0 < i, i + 1 < 0",
            "More polygons than before!",

            // Star Wars
            "Isn't a parsec a unit of distance?",
            "Han shot first.",
            "Say \"what\" again!"

    };

    private static final Random SEED = new Random();

    public static String getRandom() {
        return MESSAGES[SEED.nextInt(MESSAGES.length)];
    }

}
