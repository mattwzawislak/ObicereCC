package org.obicere.cc.executor.language;

import org.obicere.cc.configuration.Global;

import java.io.File;
import java.util.*;

public class LanguageHandler {

    private static final Map<String, Language> SUPPORTED = new HashMap<String, Language>() {{
    }};

    public static Set<String> getSupportedLanguages() {
        return SUPPORTED.keySet();
    }

    public static Language getLanguage(final String name) {
        return SUPPORTED.get(name);
    }

    public static void load() {
        try {
            final File[] languages = Global.streamFiles("/resource/languages");
            final List<File> languageList = Arrays.asList(languages);
            for (final File file : languageList) {
                final String name = file.getName();
                if (name.equals("Java")) {
                    SUPPORTED.put("Java", new JavaLanguage(file));
                    return; // for now
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
