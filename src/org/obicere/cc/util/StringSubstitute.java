package org.obicere.cc.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Obicere
 */
public class StringSubstitute implements Cloneable {

    private static final String WORD_FORMAT  = "${%s}";
    private static final String MATCH_FORMAT = "\\$\\{.+\\}";

    private final Map<String, String> words;

    public StringSubstitute() {
        this.words = new LinkedHashMap<>();
    }

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
