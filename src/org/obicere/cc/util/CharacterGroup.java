package org.obicere.cc.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

/**
 * A list of characters, often associated to a logical group of characters.
 * Such sets include: upper and lower case alphabetic Latin characters, all
 * alphabetic Latin characters, numbers, and alphanumeric characters.
 * Custom groups can also be specified by specifying a minimum and maximum
 * bound for the characters; both bounds would be inclusive.
 * <p>
 * Joint character groups can also be formed, by concatenating multiple
 * character groups together. For example, the group {@link
 * org.obicere.cc.util.CharacterGroup#ALPHA} group is equal to:
 * <p>
 * <code>LOWERCASE_ALPHA + UPPERCASE_ALPHA</code>
 * <p>
 * Where <code>+</code> is list concatenation.
 * <p>
 * Note however, joint groups do not remove existing characters. So
 * <code>LOWERCASE_ALPHA + ALPHA</code> would contain 2 copies of each of
 * the characters in {@link org.obicere.cc.util.CharacterGroup#LOWERCASE_ALPHA}.
 * <p>
 * The character groups allow pseudo-random strings to easily be generated
 * from a list of possible characters.
 *
 * @author Obicere
 * @version 1.0
 */
public class CharacterGroup {

    /**
     * The set of characters including the Latin digits:
     * <p>
     * <code>[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]</code>.
     * <p>
     *
     * @see java.lang.Character#isDigit(char)
     */

    public static final CharacterGroup NUMBERS = new CharacterGroup('0', '9');

    /**
     * The set of characters including the Latin lowercase alphabetical
     * characters.
     * <p>
     * <code>[a, b, c, ..., x, y, z]</code>
     *
     * @see java.lang.Character#isLowerCase(char)
     */

    public static final CharacterGroup LOWERCASE_ALPHA = new CharacterGroup('a', 'z');

    /**
     * The set of characters including the Latin uppercase alphabetical
     * characters.
     * <p>
     * <code>[A, B, C, ..., X, Y, Z]</code>
     *
     * @see java.lang.Character#isUpperCase(char)
     */

    public static final CharacterGroup UPPERCASE_ALPHA = new CharacterGroup('A', 'Z');

    /**
     * The set of characters including both the Latin uppercase and Latin
     * lowercase alphabetical characters. This set is the union of the
     * {@link org.obicere.cc.util.CharacterGroup#LOWERCASE_ALPHA} and
     * {@link org.obicere.cc.util.CharacterGroup#UPPERCASE_ALPHA} groups.
     * <p>
     * <code>[A, B, C, ..., X, Y, Z, a, b, c, ..., x, y, z]</code>
     *
     * @see org.obicere.cc.util.CharacterGroup#UPPERCASE_ALPHA
     * @see org.obicere.cc.util.CharacterGroup#LOWERCASE_ALPHA
     * @see java.lang.Character#isAlphabetic(int)
     */

    public static final CharacterGroup ALPHA = new CharacterGroup(UPPERCASE_ALPHA, LOWERCASE_ALPHA);

    /**
     * The set of characters including both the Latin alphabetic characters
     * and the Latin numerical characters. This set is the union of the
     * {@link org.obicere.cc.util.CharacterGroup#ALPHA} and {@link
     * org.obicere.cc.util.CharacterGroup#NUMBERS} groups.
     * <p>
     * <code>[A, B, C, ..., X, Y, Z, a, b, c, ..., x, y, z, 0, 1, 2, 3, 4,
     * 5, 6, 7, 8, 9]</code>
     */

    public static final CharacterGroup ALPHANUMERIC = new CharacterGroup(ALPHA, NUMBERS);

    private final char[] chars;
    private final int    size;

    /**
     * Creates a new character group specified by the bound of character
     * points in <code>[minBound, maxBound]</code>.
     * <p>
     * The code points provided are unchecked. This has been provided since
     * no major complications can arise from potentially invalid code
     * points. However, leaving it unchecked could lead to unsafe, yet
     * really awesome character groups.
     *
     * @param minBound The minimum bound of the character group. Must be
     *                 less than or equal to <code>maxBound</code>.
     * @param maxBound The maximum bound of the character group. Must be
     *                 greater than or equal to <code>minBound</code>.
     * @throws java.lang.IllegalArgumentException if the <code>minBound</code>
     *                                            is less than or equal to
     *                                            <code>maxBound</code>.
     *                                            This is thrown to ensure
     *                                            that errors in the code
     *                                            points are not thrown.
     */

    public CharacterGroup(final int minBound, final int maxBound) {
        if (minBound < maxBound) {
            throw new IllegalArgumentException("Minimum bound must be less than or equal to maximum bound.");
        }
        this.size = 1 + maxBound - minBound;
        this.chars = new char[size];

        for (int i = 0; i < size; i++) {
            chars[i] = (char) (minBound + i);
        }
    }

    /**
     * Creates a new character group by the union of the individual
     * character groups. This can also be used to copy an individual
     * character group.
     * <p>
     * Given that <code>+</code> is list concatenation, then this is
     * defined as:
     * <p>
     * <code> newGroup = group<sub>0</sub> + group<sub>1</sub> +
     * group<sub>2</sub> + ... + group<sub>n</sub> </code>
     * <p>
     * Where <code>n</code> is the number of groups. And
     * <code>newGroup</code> is this resulting group.
     * <p>
     * Note that calling this with no groups will result in the empty
     * group. The empty group has <code>size = 0</code> and contains no
     * characters.
     * <p>
     * Characters contained within 2 or more groups will exist more than
     * once. So if a set is desirable, then the groups individually must be
     * sets and have no intersection with any of the other sets.
     *
     * @param group The list of groups to join to create a joint group,
     *              containing each of characters in each group.
     */

    public CharacterGroup(final CharacterGroup... group) {
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

    /**
     * Constructs the empty character group, which can be used by
     * subclasses to avoid a memory overhead if they don't depend on this
     * list implementation.
     */

    protected CharacterGroup() {
        this.size = 0;
        this.chars = null;
    }

    /**
     * Provides the character at the given index. This has been granted
     * <code>protected</code> access so subclasses can modify the way the
     * character group handles character access.
     * <p>
     * With this implementation, the index should already be checked and
     * access only happens through a pseudo-random number generator. This
     * is done by simply calling <code>random.nextInt(size)</code>,
     * providing a number between <code>[0, size)</code>, which is within
     * bounds.
     *
     * @param n The index to access.
     * @return The <code>char</code> at index <code>n</code>.
     */

    protected char charAt(final int n) {
        return chars[n];
    }

    /**
     * Provides the next character by calling {@link java.util.Random#nextInt(int)}
     * on the given <code>size</code> of this group.
     *
     * @param seed The seed to provide the next index.
     * @return The character and the next pseudo-random index.
     * @throws java.util.NoSuchElementException if {@link CharacterGroup#isEmptyGroup()}
     *                                          is <code>true</code>. This
     *                                          is because the empty group
     *                                          contains no characters and
     *                                          therefore can't possibly
     *                                          return any logical character.
     * @throws java.lang.NullPointerException   if the given <code>seed</code>
     *                                          instance is <code>null</code>.
     */

    public char nextChar(final Random seed) {
        Objects.requireNonNull(seed);
        if (isEmptyGroup()) {
            throw new NoSuchElementException("No elements within the empty group.");
        }
        return charAt(seed.nextInt(size));
    }

    /**
     * Provides a pseudo-random string of a given length from characters in
     * this group. This is done by calling {@link org.obicere.cc.util.CharacterGroup#nextChar(java.util.Random)}
     * for each character in order.
     * <p>
     * If the provided <code>length = 0</code>, then the empty string is
     * returned.
     *
     * @param seed   The seed to provide the indices of the characters.
     * @param length The length of the {@link java.lang.String} to
     *               provide.
     * @return The {@link java.lang.String} of size <code>length</code>,
     * generated from characters only in this character group.
     * @throws java.lang.IllegalArgumentException if the <code>length</code>
     *                                            is non-negative.
     * @throws java.util.NoSuchElementException   if {@link CharacterGroup#isEmptyGroup()}
     *                                            is <code>true</code>.
     *                                            This is because the empty
     *                                            group contains no
     *                                            characters and therefore
     *                                            can't possibly return any
     *                                            logical character.
     * @throws java.lang.NullPointerException     if the given <code>seed</code>
     *                                            instance is <code>null</code>.
     */

    public String nextString(final Random seed, final int length) {
        Objects.requireNonNull(seed);
        if (length < 0) {
            throw new IllegalArgumentException("Length of the array must be non-negative. Given length: " + length);
        }
        if (length == 0) {
            return "";
        }
        if (isEmptyGroup()) {
            throw new NoSuchElementException("No elements within the empty group.");
        }
        final char[] content = new char[length];
        for (int i = 0; i < length; i++) {
            content[i] = nextChar(seed);
        }
        return new String(content);
    }

    /**
     * Retrieves the size of this group. When the <code>size = 0</code>,
     * {@link CharacterGroup#isEmptyGroup()} will also return
     * <code>true</code>.
     * <p>
     * All characters in the group are within the bound: <code>[0,
     * size)</code>.
     *
     * @return The size of the group.
     */

    public int size() {
        return size;
    }

    /**
     * Returns <code>true</code> if and only if the group contains no
     * characters. By default this is checked to see if {@link
     * CharacterGroup#size()} is equal to <code>0</code>. If it is, then no
     * possible characters can be contained within the group, and therefore
     * the group is considered empty.
     * <p>
     * The union of any group <code>n</code> with the empty group is the
     * group <code>n</code>.
     * <p>
     * The intersection of any group <code>n</code> with the empty group is
     * the empty group.
     * <p>
     * The complement of the empty group is set of all characters.
     *
     * @return <code>true</code> if and only if the group is logcally
     * empty.
     */

    public boolean isEmptyGroup() {
        return size == 0;
    }

}
