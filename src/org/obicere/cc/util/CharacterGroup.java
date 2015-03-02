package org.obicere.cc.util;

import java.util.Random;

/**
 * @author Obicere
 */
public class CharacterGroup {

    public static final CharacterGroup NUMBERS         = new CharacterGroup('0', '9');
    public static final CharacterGroup LOWERCASE_ALPHA = new CharacterGroup('a', 'z');
    public static final CharacterGroup UPPERCASE_ALPHA = new CharacterGroup('A', 'Z');
    public static final CharacterGroup ALPHA           = new CharacterGroup(UPPERCASE_ALPHA, LOWERCASE_ALPHA);
    public static final CharacterGroup ALPHANUMERIC    = new CharacterGroup(ALPHA, NUMBERS);
    private final char[] chars;
    private final int    size;

    private CharacterGroup(final int minBound, final int maxBound) {
        this.size = 1 + maxBound - minBound;
        this.chars = new char[size];

        for (int i = 0; i < size; i++) {
            chars[i] = (char) (minBound + i);
        }
    }

    private CharacterGroup(final CharacterGroup... group) {
        int size = 0;
        for (final CharacterGroup set : group) {
            size += set.size();
        }
        this.size = size;
        this.chars = new char[size];
        int n = 0;
        for (final CharacterGroup set : group) {
            final int length = set.size;
            System.arraycopy(set.chars, 0, chars, n, length);
            n += length;
        }
    }

    protected char charAt(final int n) {
        return chars[n];
    }

    protected char nextChar(final Random seed) {
        return charAt(seed.nextInt(size));
    }

    public int size() {
        return size;
    }

}
