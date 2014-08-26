package org.obicere.cc.methods;

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

    public String nextString() {
        return nextString(Integer.MAX_VALUE);
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

}
