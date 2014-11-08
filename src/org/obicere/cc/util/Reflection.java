package org.obicere.cc.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Obicere
 */
public class Reflection {

    private static final ClassDefiner DEFINER = new ClassDefiner();
    private static final ClassLoader  LOADER  = ClassLoader.getSystemClassLoader();
    private static LinkedList<Class<?>> cache;

    static {
        cache = loadClasses();
    }

    @SuppressWarnings("unchecked")
    public static List<Class<?>> list() {
        return (List<Class<?>>) cache.clone();
    }

    public static Stream<Class<?>> stream() {
        return cache.stream();
    }

    public static Stream<Class<?>> where(final Predicate<Class<?>> predicate) {
        final Stream<Class<?>> stream = stream();
        return stream.filter(predicate);
    }

    public static void filterAsSubclassOf(final Class<?> cls, final List<Class<?>> list) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(list);
        final ListIterator<Class<?>> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            final Class<?> next = listIterator.next();
            if (!cls.isAssignableFrom(next) || cls.equals(next)) {
                listIterator.remove();
            }
        }
    }

    public static Stream<Class<?>> subclassOf(final Class<?> cls) {
        Objects.requireNonNull(cls);
        return where(c -> cls.isAssignableFrom(c) && !cls.equals(c));
    }

    public static Stream<Class<?>> hasAnnotation(final Class<? extends Annotation> cls) {
        return where(e -> e.getAnnotation(cls) != null);
    }

    public static Object getStaticField(final Class<?> cls, final String name) throws NoSuchFieldException, IllegalAccessException {
        final Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    public static Object newInstance(final Class<?> cls) {
        try {
            final Constructor cstr = cls.getConstructor();
            if (cstr == null) {
                return null;
            }
            cstr.setAccessible(true);
            return cstr.newInstance();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LinkedList<Class<?>> loadClassesFrom(final String directory) {
        final LinkedList<Class<?>> classes = new LinkedList<>();
        try {
            final List<String> loader = FileLoader.searchClassPath(directory, ".class");
            loader.forEach(path -> {
                try {
                    final Class<?> cls = forName(directory, path);
                    if (cls != null && !classes.contains(cls)) {
                        classes.add(cls);
                    }
                } catch (final Exception | Error ignored) {
                }
            });
        } catch (final Exception e) {
        }
        return classes;
    }

    private static LinkedList<Class<?>> loadClasses() {
        final LinkedList<Class<?>> classes = new LinkedList<>();
        try {
            final List<String> loader = FileLoader.searchClassPath(".class");
            loader.forEach(path -> {
                try {
                    final Class<?> cls = forName(path);
                    if (cls != null && !classes.contains(cls)) {
                        classes.add(cls);
                    }
                } catch (final Error | Exception ignored) {
                }
            });
        } catch (final Exception e) {
        }
        return classes;
    }

    private static Class<?> forName(final String name) throws Exception {
        return forName("", name);
    }

    private static Class<?> forName(final String directory, final String name) {
        try {
            final Class<?> cls = LOADER.loadClass(name);
            if (cls != null) {
                return cls;
            }
        } catch (final Error | Exception ignored) {
        }
        final Class<?> defined = DEFINER.attemptDefine(directory, name);
        if (defined != null) {
            return defined;
        }
        // todo: implement a system where it actually tries to find the class...
        return null;
    }

    private static class ClassDefiner extends ClassLoader {

        public Class<?> attemptDefine(final String directory, final String name) {
            try {
                final File file = new File(directory, name.replace(".", File.separator) + ".class");
                final byte[] content = IOUtils.readData(file);
                final Class<?> loaded = super.findLoadedClass(name);
                if (loaded != null) {
                    return loaded;
                }
                return defineClass(name, content, 0, content.length);
            } catch (final Exception e) {
                //e.printStackTrace();
            }
            return null;
        }

    }

}