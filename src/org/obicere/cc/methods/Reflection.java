package org.obicere.cc.methods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Obicere
 */
public class Reflection {

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

    public static Stream<Class<?>> subclassOf(final Class<?> cls) {
        Objects.requireNonNull(cls);
        return where(c -> cls.isAssignableFrom(c) && !cls.equals(c));
    }

    public static Stream<Class<?>> hasAnnotation(final Class<? extends Annotation> cls) {
        return where(e -> e.getAnnotation(cls) != null);
    }

    public static Object getStaticField(final Class<?> cls, final String name) {
        try {
            final Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
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

    private static LinkedList<Class<?>> loadClasses() {
        final LinkedList<Class<?>> classes = new LinkedList<>();
        try {
            final List<String> loader = FileLoader.searchClassPath(".class");
            loader.forEach(path -> {
                try {
                    final Class<?> cls = forName(path);
                    if(cls != null) {
                        classes.add(cls);
                    }
                } catch (final Exception ignored) {

                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    private static Class<?> forName(final String name) {
        try {
            return ClassLoader.getSystemClassLoader().loadClass(name);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
