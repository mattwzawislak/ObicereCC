package org.obicere.cc.executor.language;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies a casing to a specified {@link String}. The casings listed here
 * are common programming forms. This is to be used for any variety of
 * languages. More unorthodox casings are not listed, yet could be easily
 * supported.
 * <p>
 * The default implementations are:
 * <pre>
 *     1. lower camel case:     fooBarTest
 *     2. camel case:           FooBarTest
 *     3. lowercase:            foobartest
 *     4. lowercase underscore: foo_bar_test
 * </pre>
 *
 * @author Obicere
 * @version 1.0
 */
public enum Casing {

    LOWER_CAMEL_CASE,
    CAMEL_CASE,
    LOWERCASE,
    LOWERCASE_UNDERSCORE;

    // Meant to match words base off of casing if available.
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[A-Z]?[a-z]+|[0-9]+");

    /**
     * Splits the words by what would appear to be a word. Either split by
     * non-alphabetical characters or by a change in casings.
     * <p>
     * For example, the string <code>"testFoo-bar"</code> would be split
     * into the following words:
     * <p>
     * {@code [test, Foo, bar]}
     *
     * @param phrase The string to split into words.
     * @return The list of split words.
     */

    private List<String> splitWords(final String phrase) {
        final LinkedList<String> list = new LinkedList<>();
        final Matcher matcher = SPLIT_PATTERN.matcher(phrase);
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    /**
     * Performs a case on the given string. First by splitting the string
     * into individual words, then by applying the appropriate casing
     * technique to each word.
     * <p>
     * In the case of {@link #LOWERCASE}, merely {@link
     * String#toLowerCase()} is called, since they are equivalent under all
     * terms once the non-alphanumerical characters have been removed.
     * <p>
     * Beyond that, each word does have to be split and does have to be
     * cased correctly. Once this has all been completed, the correct
     * casing should have been applied.
     * <p>
     * The strings should only contain alphanumeric characters. Other
     * characters are tossed out. The resulting string will always be
     * matched either by {@link String#isEmpty()} or by the regex matching
     * group <code>\W</code>.
     *
     * @param phrase The phrase to perform the casing on.
     * @return The cased phrase.
     */

    public String performCase(final String phrase) {
        Objects.requireNonNull(phrase);
        if (this == LOWERCASE) {
            return phrase.replaceAll("\\W+", "").toLowerCase();
        }
        final List<String> words = splitWords(phrase);
        final Iterator<String> iterator = words.iterator();
        final StringBuilder builder = new StringBuilder(phrase.length());
        switch (this) {
            case CAMEL_CASE:
                while (iterator.hasNext()) {
                    final String token = iterator.next();
                    if (!token.isEmpty()) {
                        builder.append(Character.toUpperCase(token.charAt(0)));
                        builder.append(token.toLowerCase().substring(1));
                    }
                }
                break;
            case LOWER_CAMEL_CASE:
                boolean lowered = false;
                while (iterator.hasNext()) {
                    final String token = iterator.next();
                    if (!token.isEmpty()) {
                        if (!lowered) {
                            builder.append(token.toLowerCase());
                            lowered = true;
                            continue;
                        }
                        builder.append(Character.toUpperCase(token.charAt(0)));
                        builder.append(token.toLowerCase().substring(1));
                    }
                }
                break;
            case LOWERCASE_UNDERSCORE:

                for (int i = 0; iterator.hasNext(); i++) {
                    final String token = iterator.next();
                    if (!token.isEmpty()) {
                        if (i != 0) {
                            builder.append('_');
                        }
                        builder.append(token.toLowerCase());
                    }
                }
                break;
        }
        return builder.toString();
    }
}
