package org.obicere.cc.configuration;

import java.util.Random;

public class Message {

    private final String[] messages = new String[]{
            // Music
            "4 x 4 = 12",
            "                          SAIL",

            // Games
            "There is no cow level.",
            "PC Master Race",

            // Chess
            "Rxd4!!",

            // Shitty advertising
            "Now with 0 calories!",
            "Soon™",

            // Jaden Smith Quotes
            "How Can Mirrors Be Real If Our Eyes Aren't Even Real?",
            "Trees Are Never Sad Look At Them Every Once In Awhile\nThey're Quite Beautiful.",

            // Programming
            "Now with an O(nlogn) complexity!",
            "import java.awt.*;",
            "import javax.swing.*;",
            "0x04C11DB7",
            "i++;",
            "0 < i, i + 1 < 0",
            "More polygons than before!",

            // Star Wars
            "Isn't a parsec a unit of distance?",
            "Han shot first.",

            // I can't even
            "What if birds aren't really singing but are screaming\nbecause they are afraid of heights?",

            // Other movies
            "Say \"what\" again, I dare you."

    };

    private final Random seed = new Random();

    public String getRandom() {
        return messages[seed.nextInt(messages.length)];
    }

}
