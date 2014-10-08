package org.obicere.cc.executor.language;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Obicere
 */
public enum Casing {

    LOWER_CAMEL_CASE,
    CAMEL_CASE,
    LOWERCASE,
    LOWERCASE_UNDERSCORE;

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[A-Z]?[a-z]+|[0-9]+");

    private List<String> splitWords(final String word) {
        final LinkedList<String> list = new LinkedList<>();
        final Matcher matcher = SPLIT_PATTERN.matcher(word);
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    public String performCase(final String word) {
        if (this == LOWERCASE) {
            return word.toLowerCase();
        }
        final List<String> words = splitWords(word);
        final Iterator<String> iterator = words.iterator();
        final StringBuilder builder = new StringBuilder(word.length());
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
                while(iterator.hasNext()) {
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
