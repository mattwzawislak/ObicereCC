package org.obicere.cc.configuration;

import java.util.Random;

/**
 * Contains a list of messages to display on the splash screen. These are
 * just jokes and not to be taken seriously.
 * <p>
 * The lines in each message is separated by the standard <code>\n</code>
 * escape sequence. Due to the size of the standard splash screen, up to
 * 4-5 lines may only be visible. No restrictions have been placed, but
 * this is the suggested maximum. The splash screen should be utilizing a
 * monospaced font. With this, up to about 55 characters will fit
 * comfortably per line.
 *
 * @author Obicere
 * @version 1.0
 */

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

    /**
     * Retrieves a random message from the set of messages. Each with
     * roughly the same probability.
     *
     * @return The pseudo-randomly selected message.
     */

    public String getRandom() {
        return messages[seed.nextInt(messages.length)];
    }

}
