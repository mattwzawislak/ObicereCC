package org.obicere.cc.executor.language;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.process.StartingProcess;
import org.obicere.cc.util.Reflection;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Loads all the classes that directly extend the {@link
 * org.obicere.cc.executor.language.Language} class. This works through the
 * {@link org.obicere.cc.util.Reflection} utility on every class in the
 * class path.
 * <p>
 * This will look for a <code>Language(Domain)</code> constructor. The
 * domain will be provided through reflection and then added to the
 * manager's pool.
 *
 * @author Obicere
 * @version 1.0
 */

public class LanguageManager extends StartingProcess {

    private final Map<String, Language> supported = new HashMap<>();

    public LanguageManager(final Domain access) {
        super(access);
    }

    /**
     * Retrieves the list of the names of the loaded languages.
     *
     * @return The set of loaded language names.
     */

    public Set<String> getSupportedLanguageNames() {
        return supported.keySet();
    }

    /**
     * Retrieves the list of loaded languages. There is no guarantee the
     * same language may not be implemented but under a different name.
     *
     * @return The list of loaded languages.
     */

    public Collection<Language> getSupportedLanguages() {
        return supported.values();
    }

    /**
     * Retrieves the language implementation by name. <code>null</code> if
     * no such language exists. This is case sensitive.
     *
     * @param name The name to search for.
     * @return The language implementation if found.
     */

    public Language getLanguage(final String name) {
        return supported.get(name);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Set to priority <code>1</code>, after the {@link
     * org.obicere.cc.configuration.Paths} task, since this needs the
     * folders to be created for the language directories.
     */

    @Override
    public int priority() {
        return 1;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Attempts to load each language. Each language requires a
     * <code>Language(Domain)</code> constructor. Should it not, the class
     * will be specified in the console as such. Otherwise, any other
     * failure to load is printed to the error stream.
     */

    @Override
    public void run() {
        try {
            final Stream<Class<?>> languageClasses = Reflection.subclassOf(Language.class);
            languageClasses.forEach(cls -> {
                try {
                    final Constructor<?> constructor = cls.getConstructor(Domain.class);
                    final Language language = (Language) constructor.newInstance(access);
                    final String name = language.getName();
                    supported.put(name, language);

                    log.log(Level.INFO, "Loaded language {0}", name);
                } catch (final NoSuchMethodException e) {
                    log.log(Level.SEVERE, "Language {0} does not have a Language(Domain) constructor.", cls.getSimpleName());
                    e.printStackTrace();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
