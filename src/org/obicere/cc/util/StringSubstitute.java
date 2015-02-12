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
     * Constructs an empty string substitute formatter. This will not
     * replace any information.
     */

    public StringSubstitute() {
        this.words = new LinkedHashMap<>();
    }

    /**
     * Constructs a new formatter based off of the given format mapping.
     * The map will be copied, as opposed to just referenced. Changes to
     * the map after constructing a substitute with this constructor will
     * not be reflected in the substitution.
     * <p>
     * Changes that must be submitted should instead be submitted through
     * {@link #put(String, String)}.
     * <p>
     * If order is important, use a map that has predictable indices upon
     * insertion. Such as the {@link java.util.LinkedHashMap}.
     *
     * @param words The mapping replacement.
     * @see #put(String, String)
     */

    public StringSubstitute(final Map<String, String> words) {
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

    /**
     * {@inheritDoc}
     */

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        final StringSubstitute substitute = new StringSubstitute();
        words.forEach(substitute::put);
        return substitute;
    }

    /**
     * Submits a new mapping to the current substitute formatter. It is
     * recommended not to apply the proper format to the keys unless
     * copying from an existing formatter. This is to avoid a flawed
     * replacement key resulting in the failed replacement.
     *
     * @param key   The key to replace with the given value.
     * @param value The value to format the string with.
     */

    public void put(final String key, final String value) {
        if (key.matches(MATCH_FORMAT)) {
            words.put(key, value);
        } else {
            words.put(normalize(key), value);
        }
    }

    /**
     * Applies the formatter upon the given {@link java.lang.String}. The
     * given input cannot be <code>null</code>. The map will not be closed
     * after an application, so multiple applications to strings can be
     * done with a given formatter.
     * <p>
     * This functionality will not be done automatically, so calling this
     * method to apply the formatting is needed.
     *
     * @param input The {@link java.lang.String} to format.
     * @return The newly formatted string with the given substitutions
     * applied.
     * @see #replaceFromMap(String, java.util.Map)
     */

    public String apply(final String input) {
        Objects.requireNonNull(input);
        return replaceFromMap(input, words);
    }

    /**
     * Iterates the given input replacing each occurrence with the given
     * formatter. This will be iterative. So a format can be applied upon a
     * formatter in order of insertion.
     * <p>
     * For example:
     * <pre>
     * <code>StringSubstitute substitute = new StringSubstitute();
     * substitute.put("foo", "${bar}");
     * substitute.put("bar", "abc");
     * String abc = substitute.apply("${foo}"); // equals "abc"
     * </code>
     * </pre>
     * <p>
     * The above example will first replace the <code>"foo"</code> key with
     * the correct format for the <code>"bar"</code> key. Due to this, the
     * second mapping - which replaces on a key of <code>"bar"</code> -
     * will then be substituted as well.
     * <p>
     * This allows formats to 'explode' outwards to match a specific
     * grammar.
     * <p>
     * This method was also separated from the calling method in case
     * functionality needs to be overridden.
     *
     * @param string The given {@link java.lang.String} to format.
     * @param words  The mapping replacement.
     * @return The formatted word.
     */

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

    /**
     * Normalizes the given word to fit the format. Note that this will not
     * ensure the word is not in a proper format. If the format is also
     * flawed, such as <code>"{$foo}"</code> instead of
     * <code>"${foo}"</code>, the correct adjustments will not be applied
     * but instead by just interpreted as correct.
     * <p>
     * The possibility of this error is why formatting should not be done
     * by the user, but left to this functionality.
     *
     * @param word The word to normalize for formatting.
     * @return The normalized word.
     */

    private String normalize(final String word) {
        return String.format(WORD_FORMAT, word);
    }
}
