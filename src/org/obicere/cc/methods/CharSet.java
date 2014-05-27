package org.obicere.cc.methods;

/**
 * @author Obicere
 */
public class CharSet {

    public static final CharSet WORD = new CharSet("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ");
    public static final CharSet NUMBERS = new CharSet('0', '9');
    public static final CharSet LOWERCASE_ASCII = new CharSet('a', 'z');
    public static final CharSet UPPERCASE_ASCII = new CharSet('A', 'Z');
    public static final CharSet ALL = new CharSet(0, Short.MAX_VALUE);

    private final int minBound;
    private final int maxBound;

    private final String values;

    private CharSet(final int minBound, final int maxBound){
        this.minBound = minBound;
        this.maxBound = maxBound;
        this.values = null;
    }

    private CharSet(final String values){
        this.values = values;
        this.minBound = -1;
        this.maxBound = -1;
    }

    protected char nextChar(final SimpleRandom seed){
        if(values != null){
            return values.charAt(seed.nextInt(0, values.length()));
        }
        return (char) seed.nextInt(minBound, maxBound + 1);
    }

}
