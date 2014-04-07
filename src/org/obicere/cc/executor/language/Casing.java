package org.obicere.cc.executor.language;

/**
 * @author Obicere
 */
public enum Casing {

    LOWER_CAMEL_CASE,
    CAMEL_CASE,
    UPPERCASE,
    UPPERCASE_UNDERSCORE;

    public static Casing forName(final String name) {
        final String search = name.replaceAll("[^a-zA-Z_]", "").replace(' ', '_').toUpperCase();
        for (final Casing casing : values()) {
            if (casing.name().equals(search)) {
                return casing;
            }
        }
        return LOWER_CAMEL_CASE;
    }

    public String performCase(final String word) {
        final String[] words = getWords(word);
        final StringBuilder builder = new StringBuilder(word.length());
        switch (this) {
            case CAMEL_CASE:
                for (final String token : words) {
                    if (!token.isEmpty()) {
                        builder.append(Character.toUpperCase(token.charAt(0)));
                        builder.append(token.toLowerCase().substring(1));
                    }
                }
                break;
            case LOWER_CAMEL_CASE:
                for (int i = 0; i < words.length; i++) {
                    final String token = words[i];
                    if (!token.isEmpty() && i != 0) {
                        builder.append(Character.toUpperCase(token.charAt(0)));
                        builder.append(token.toLowerCase().substring(1));
                    }
                }
                break;
            case UPPERCASE:
                for (final String token : words) {
                    if (!token.isEmpty()) {
                        builder.append(token.toUpperCase());
                    }
                }
                break;
            case UPPERCASE_UNDERSCORE:
                for (int i = 0; i < words.length; i++) {
                    final String token = words[i];
                    if (!token.isEmpty()) {
                        if (i != 0) {
                            builder.append('_');
                        }
                        builder.append(token.toUpperCase());
                    }
                }
                break;
        }
        return builder.toString();
    }

    private String[] getWords(final String phrase) {
        return phrase.split("[_\\s]");
    }

}
