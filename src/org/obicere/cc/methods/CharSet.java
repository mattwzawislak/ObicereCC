package org.obicere.cc.methods;

/**
 * @author Obicere
 */
public class CharSet {

    public static final CharSet NUMBERS         = new CharSet('0', '9');
    public static final CharSet LOWERCASE_ALPHA = new CharSet('a', 'z');
    public static final CharSet UPPERCASE_ALPHA = new CharSet('A', 'Z');
    public static final CharSet ALPHA           = new CharSet(UPPERCASE_ALPHA, LOWERCASE_ALPHA);
    public static final CharSet ALPHANUMERIC    = new CharSet(ALPHA, NUMBERS);
    public static final CharSet ALL             = new CharSet(0, Short.MAX_VALUE);
    private final char[] chars;
    private final int    size;

    private CharSet(final int minBound, final int maxBound) {
        this.size = maxBound - minBound;
        this.chars = new char[size + 1];

        for (int i = 0; i < size; i++) {
            chars[i] = (char) (minBound + i);
        }
    }

    private CharSet(final CharSet... group) {
        int size = 0;
        for (final CharSet set : group) {
            size += set.size();
        }
        this.size = size;
        this.chars = new char[size];
        int n = 0;
        for (final CharSet set : group) {
            final int length = set.size;
            System.arraycopy(set.chars, 0, chars, n, length);
            n += length;
        }
    }

    protected char charAt(final int n) {
        return chars[n];
    }

    protected char nextChar(final SimpleRandom seed) {
        return chars[seed.nextInt(size)];
    }

    public int size() {
        return size;
    }

}
