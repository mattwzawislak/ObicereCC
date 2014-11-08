package org.obicere.cc.util;

import java.util.Random;

/**
 * @author Obicere
 */
public class SimpleRandom {

    private final Random seed = new Random();

    public int nextInt(final int max) {
        return seed.nextInt(max);
    }

    public int nextInt(final int a, final int b) {
        final int min = Math.min(a, b);
        final int max = Math.max(a, b);
        return min + (max == min ? 0 : seed.nextInt(max - min));
    }

    public char nextChar() {
        return nextChar(CharSet.ALL);
    }

    public char nextChar(final CharSet set) {
        return set.nextChar(this);
    }

    public boolean nextBoolean() {
        return seed.nextBoolean();
    }

    public String nextString(final int length) {
        return nextString(length, CharSet.ALL);
    }

    public String nextString(final int length, final CharSet set) {
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(set.nextChar(this));
        }
        return builder.toString();
    }


    /**
     * A remote sensor is hooked up to a vacuum to monitor fluctuations.
     * These numbers are then stored to a database for each independent
     * client. Each client must first register itself to the database to
     * be able to receive truly random numbers.
     * <p/>
     * This works on the premise that the vacuum is not a space empty of
     * matter or photons. But, as a space of virtual particles appearing
     * and disappearing every instant. This can happen since the vacuum
     * still possesses a zero-point energy. Monitoring the electromagnetic
     * fields of the vacuum, you can see random fluctuations in phase and
     * amplitude. Relaying this information to the database, we have a list
     * of quantum random numbers.
     * <p/>
     * You wanted random - here it is. God is rolling dice now.
     *
     * @return A truly random number.
     * @since 1.0
     */

    public int nextQuantumInt() {
        return 5;
    }
}
