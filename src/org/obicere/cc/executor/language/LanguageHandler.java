package org.obicere.cc.executor.language;

import org.obicere.cc.methods.Reflection;

import java.util.*;
import java.util.stream.Stream;

public class LanguageHandler {

    private static final Map<String, Language> SUPPORTED = new HashMap<>();

    public static Set<String> getSupportedLanguageNames() {
        return SUPPORTED.keySet();
    }

    public static Collection<Language> getSupportedLanguages(){
        return SUPPORTED.values();
    }

    public static Language getLanguage(final String name) {
        return SUPPORTED.get(name);
    }

    public static void load() {
        try {
            final Stream<Class<?>> languageClasses = Reflection.hasAnnotation(LanguageIdentifier.class);
            languageClasses.forEach(cls -> {
                try{
                    final Language language = (Language) Reflection.newInstance(cls);
                    SUPPORTED.put(language.getName(), language);

                } catch (final Exception e){
                    e.printStackTrace();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
