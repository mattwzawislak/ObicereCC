package org.obicere.cc.methods;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Obicere
 */
public class Reflection {

    private static final ClassLoader CLASS_LOADER = Reflection.class.getClassLoader();
    private static Stream<Class<?>> cache;

    static {
        cache = loadClasses();
    }

    public static Stream<Class<?>> stream() {
        return cache.map(e -> e);
    }

    public static Stream<Class<?>> where(final Predicate<Class<?>> predicate) {
        return stream().filter(predicate);
    }

    public static Stream<Class<?>> subclassOf(final Class<?> cls) {
        return where(cls::isAssignableFrom);
    }

    public static Stream<Class<?>> hasAnnotation(final Class<? extends Annotation> cls) {
        return where(e -> e.getAnnotation(cls) != null);
    }

    public static Object newInstance(final Class<?> cls){
        try{
            return cls.newInstance();
        } catch (final Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static Stream<Class<?>> loadClasses() {
        final LinkedList<Class<?>> classes = new LinkedList<>();
        try {
            final Enumeration<URL> paths = CLASS_LOADER.getResources("");
            while (paths.hasMoreElements()) {
                final URL path = paths.nextElement();
                final File root = new File(path.getPath());
                final String rootName = flipSlashes(root.getPath());
                for (final File file : listFiles(root)) {
                    addFiles(classes, file, rootName);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return classes.stream();
    }

    private static void addFiles(final LinkedList<Class<?>> list, final File folder, final String root) {
        for (final File file : listFiles(folder)) {
            if (file.isDirectory()) {
                addFiles(list, file, root);
            } else {
                final String name = file.getName();
                if (name.endsWith(".class")) {
                    final String filePath = flipSlashes(file.getPath());
                    if (filePath.startsWith(root)) {
                        list.add(forName(normalizeClassName(filePath, root), CLASS_LOADER));
                    }
                }
            }
        }
    }

    private static File[] listFiles(final File file) {
        final File[] files = file.listFiles();
        if (files != null) {
            return files;
        }
        return new File[0];
    }

    private static String flipSlashes(final String path) {
        return path.replace("\\", "/");
    }

    private static String normalizeClassName(final String name, final String root) {
        return name.substring(root.length() + 1).replace("/", ".").replace(".class", "");
    }

    private static Class<?> forName(final String name, final ClassLoader loader) {
        try {
            return loader.loadClass(name);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
