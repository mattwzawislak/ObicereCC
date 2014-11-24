package org.obicere.cc.executor.language;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.process.StartingProcess;
import org.obicere.cc.util.Reflection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

public class LanguageManager extends StartingProcess {

    private final Map<String, Language> supported = new HashMap<>();

    public LanguageManager(final Domain access) {
        super(access);
    }

    public Set<String> getSupportedLanguageNames() {
        return supported.keySet();
    }

    public Collection<Language> getSupportedLanguages() {
        return supported.values();
    }

    public Language getLanguage(final String name) {
        return supported.get(name);
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public void run() {
        try {
            final Stream<Class<?>> languageClasses = Reflection.hasAnnotation(LanguageIdentifier.class);
            languageClasses.forEach(cls -> {
                try {
                    final Language language = (Language) Reflection.newInstance(cls);
                    final String name = language.getName();
                    supported.put(name, language);

                    log.log(Level.INFO, "Loaded language {0}", name);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
