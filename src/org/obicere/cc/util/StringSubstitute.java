package org.obicere.cc.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Performs string substitution given a format and a list strings to
 * replace in the format. The formatting is done on a name-to-value basis.
 * Meaning that formats with the same name will be evaluated to the same
 * value.
 * <p>
 * The format notation is:
 * <p>
 * <code>${name}</code>
 * <p>
 * Then, the user can provide a mapping <code>name -> value</code>. Upon
 * replacement the format containing the name will be completely replaced.
 * <p>
 * For example:
 * <p>
 * <pre>
 * <code>format: "${foo} test ${bar}
 * "foo"    -> "123"
 * "${bar}" -> "foo"
 *
 * output: "123 test foo"
 * </code>
 * </pre>
 * <p>
 * Note that invalid formats will be normalized. So should the developer
 * goof and provide an invalid mapping where the format is wrong:
 * <p>
 * <code> "{$bar}" -> "foo" </code>
 * <p>
 * This should have been <code>${bar}</code>! Instead, the following
 * mappings will be provided upon normalization:
 * <p>
 * <code> "${{$bar}}" -> "foo" </code>
 * <p>
 * Due to this, it is suggested to just supply the name and not and
 * attempted format, unless the format was provided from this class.
 *
 * @author Obicere
 * @version 1.0
 */
public class StringSubstitute implements Cloneable {

    /**
     * Formats the given name to the correct format pattern.
     */
    private static final String WORD_FORMAT = "${%s}";

    /**
     * Regular expression used to check if the given name is already in the
     * correct format. Should this be done, no additional formatting will
     * be applied.
     */
    private static final String MATCH_FORMAT = "^\\$\\{.+\\}$";

    private final Map<String, String> words;

    /**
     * Constructs an empty string substitute formatter.
     */

    public StringSubstitute() {
        this.words = new LinkedHashMap<>();
    }

    /**
     * Constructs a new formatter based off of the given format mappings.
     *
     * @param words
     */

    public StringSubstitute(final Map<String, String> words) {
        this(words, false);
    }

    private StringSubstitute(final Map<String, String> words, final boolean safeCopy) {
        if (safeCopy) {
            this.words = words;
            return;
        }
        Objects.requireNonNull(words);
        final Map<String, String> fixedWords = new LinkedHashMap<>();
        words.keySet().stream().forEach(key -> {
            final String value = words.get(key);
            if (key.matches(MATCH_FORMAT)) {
                fixedWords.put(key, value);
            } else {
                fixedWords.put(normalize(key), value);
            }
        });
        this.words = fixedWords;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        final Map<String, String> newWords = new LinkedHashMap<>();
        words.forEach(newWords::put);
        return new StringSubstitute(newWords, true);
    }

    public void put(final String key, final String value) {
        if (key.matches(MATCH_FORMAT)) {
            words.put(key, value);
        } else {
            words.put(normalize(key), value);
        }
    }

    public String apply(String input) {
        Objects.requireNonNull(input);
        return replaceFromMap(input, words);
    }

    private String replaceFromMap(final String string, final Map<String, String> words) {
        final StringBuilder builder = new StringBuilder(string);
        words.forEach((key, value) -> {

            int start = builder.indexOf(key);
            while (start > -1) {
                final int end = start + key.length();
                final int nextSearchStart = start + value.length();
                builder.replace(start, end, value);
                start = builder.indexOf(key, nextSearchStart);
            }
        });
        return builder.toString();
    }

    private String normalize(final String word) {
        return String.format(WORD_FORMAT, word);
    }
}
