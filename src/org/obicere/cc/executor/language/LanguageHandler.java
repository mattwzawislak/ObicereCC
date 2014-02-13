package org.obicere.cc.executor.language;

import java.util.LinkedList;
public class LanguageHandler {

    private static final String[] SUPPORTED = new String[]{
            "Java"
    };

    private static final LinkedList<Language> LOADED_LANGUAGES = new LinkedList<>();

    public static String[] getSupportedLanguages() {
        return SUPPORTED;
    }

    public static Language getLanguage(final String name) {
        for (final Language language : LOADED_LANGUAGES) {
            if (language.getName().equals(name)) {
                return language;
            }
        }
        final Language language;
        switch (name) {
            case "Java":
                language = new JavaExecutor();
                break;
            default:
                throw new NullPointerException("Can't find language");
        }
        LOADED_LANGUAGES.add(language);
        return language;
    }

}
