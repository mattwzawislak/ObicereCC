package org.obicere.cc.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Provides access to loading the resources from the given directory or
 * classpath for classes that meet a certain predicate. This will
 * recursively enumerate all the possible files by accessing directories,
 * jars and archives. This is done by utilizing the {@link
 * org.obicere.cc.util.FileLoader} resource.
 * <p>
 * This can therefore also be used as a {@link java.net.URLClassLoader}, as
 * loading from a single directory will function in much the same way.
 * <p>
 * As a standard, the system class loader will be used to first attempt to
 * define a class. Should this fail, a backup class loader will attempt to
 * define the class by {@link java.io.File} contents.
 * <p>
 * This can be changed by reassigning the system property
 * <code>java.system.class.loader</code> to a specified class loader.
 * <p>
 * In general, errors during the loading of a class are ignored. This will
 * only attempt to handle classes that are successfully loaded. Therefore,
 * absent classes are caused by errors in the loading of the class. Such
 * errors include:
 * <pre>
 * <li> Errors in the static initialization
 * <li> Missing resources in the classpath
 * <li> No definition of the class can be found: {@link
 * NoClassDefFoundError}
 * </pre>
 * <p>
 * As of <code>v1.0</code>, no verbose property has been added to notify of
 * failed class loading.
 *
 * @author Obicere
 * @see ClassLoader
 */
public class Reflection {

    private static final ClassDefiner DEFINER = new ClassDefiner();
    private static final ClassLoader  LOADER  = ClassLoader.getSystemClassLoader();

    /**
     * Loads all the classes in the class path and then filters the classes
     * by a given predicate.
     * <p>
     * Should one wish for the <code>predicate</code> to follow the form:
     * <pre>
     * <code>
     * List&lt;Class&lt;?&gt;&gt; list = Reflection.where(e -&gt; true);
     * </code>
     * </pre>
     * <p>
     * The instead, just calling {@link Reflection#loadClasses()} would
     * suffice. The same list of classes will be returned. This method
     * makes the same call; but, filters the generated list decreasing
     * performance.
     * <p>
     * This method complies with the standard set by {@link
     * Reflection#loadClasses()}, such that no <code>null</code> classes
     * will be returned. However, the empty list may be returned, if no
     * classes meet the <code>predicate</code>.
     *
     * @param predicate The predicate to filter the classes by.
     * @return The filtered list of classes that were loaded and met the
     * <code>predicate</code>'s requirements.
     * @throws NullPointerException if the <code>predicate</code> is
     *                              <code>null</code>.
     */

    public static List<Class<?>> where(final Predicate<Class<?>> predicate) {
        Objects.requireNonNull(predicate);
        final List<Class<?>> list = loadClasses();
        list.removeIf(predicate.negate());
        return list;
    }

    /**
     * Filters the given <code>list</code> by classes that are assignable
     * by the specified <code>cls</code>. Any <code>null</code> classes are
     * removed from the list, since a non-existent class is not assignable
     * from any class.
     * <p>
     * The filtering class is also removed from the list, if present. This
     * was chosen, as a class is not a subclass of itself, but a class can
     * be assignable from itself.
     * <p>
     * This is not thread safe, modifying the list elsewhere may produce
     * conflicts with the filtering applied here.
     *
     * @param cls  The class to filter by. Only classes that are assignable
     *             from this class will remain in the given list, assuming
     *             proper execution.
     * @param list The list of classes to filter.
     * @throws NullPointerException if the given <code>cls</code> or
     *                              <code>list</code> is <code>null</code>.
     *                              Both are needed to provide the
     *                              functionality of this method.
     */

    public static void filterAsSubclassOf(final Class<?> cls, final List<Class<?>> list) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(list);
        final ListIterator<Class<?>> listIterator = list.listIterator();
        while (listIterator.hasNext()) {
            final Class<?> next = listIterator.next();
            if (next == null || !cls.isAssignableFrom(next) || cls.equals(next)) {
                listIterator.remove();
            }
        }
    }

    /**
     * Loads all classes in the class path that is a subclass of the given
     * class <code>cls</code>. This is done by checking if the filtering
     * class is assignable by the each class.
     * <p>
     * The classes are loaded from the classpath. Due to this, no
     * <code>null</code> classes will be present in the resulting list.
     * <p>
     * This is the same functionality as calling the {@link
     * org.obicere.cc.util.Reflection#loadClasses()} method, then utilizing
     * the {@link org.obicere.cc.util.Reflection#filterAsSubclassOf(Class,
     * java.util.List)} function.
     *
     * @param cls The class to filter by. Only classes that are assignable
     *            from this class will remain in the given list, assuming
     *            proper execution.
     * @return The list of filtered classes.
     * @throws NullPointerException if the given <code>cls</code> is
     *                              <code>null</code>.
     * @see org.obicere.cc.util.Reflection#where(java.util.function.Predicate)
     * @see org.obicere.cc.util.Reflection#loadClasses()
     */

    public static List<Class<?>> subclassOf(final Class<?> cls) {
        Objects.requireNonNull(cls);
        return where(c -> cls.isAssignableFrom(c) && !cls.equals(c));
    }

    /**
     * Loads all classes in the class path that denotes the given
     * annotation of type <code>cls</code>.
     * <p>
     * Any classes in the resulting list can be asserted that: {@link
     * Class#getAnnotation(Class)} will not return <code>null</code>.
     * <p>
     * The given class of <code>cls</code> must employ a {@link
     * java.lang.annotation.RetentionPolicy#RUNTIME} retention, so that
     * this program can pick them up.
     * <p>
     * This can be done by using the code:
     * <pre>
     * <code>package myPackage;
     *
     * //imports
     *
     * {@literal @}java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
     * public {@literal @}interface Foo {}
     * </code>
     * </pre>
     * <p>
     * After this is done, the <code>Foo</code> annotation will be detected
     * and usable by this method.
     *
     * @param cls The class to filter by. Only classes that have the
     *            annotation specified with the right retention will be
     *            considered.
     * @return The list of filtered classes.
     * @throws NullPointerException if the given <code>cls</code> is
     *                              <code>null</code>.
     */

    public static List<Class<?>> hasAnnotation(final Class<? extends Annotation> cls) {
        Objects.requireNonNull(cls);
        return where(e -> e.getAnnotation(cls) != null);
    }

    /**
     * Constructs a new parameter-less instance of the class. This will
     * utilize the default constructor for the class. Should one not be
     * found or an error occurs, <code>null</code> will be returned.
     * <p>
     * As of <code>v1.0</code>, constructors with parameters are not
     * handled and should be instead handled by {@link
     * Class#getDeclaredConstructor(Class[])}.
     *
     * @param cls The class to create a new instance of.
     * @return The new instance of the class <code>cls</code> if the
     * construction was completed successfully. Otherwise
     * <code>null</code>.
     */

    public static <T> T newInstance(final Class<T> cls) {
        try {
            final Constructor<T> constructor = cls.getConstructor();
            if (constructor == null) {
                return null;
            }
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Attempts to load the classes from the given directory. This is
     * handled by the recursive definition in the {@link
     * org.obicere.cc.util.FileLoader}.
     * <p>
     * Errors in the loading of an individual class are ignored. Due to
     * this, notifications of absent classes won't be generated. Should an
     * expected class not be present, it is up to the caller to handle the
     * situation.
     * <p>
     * Reasons for a class load to fail are:
     * <pre>
     * <li>Error in static initialization
     * <li>Missing resources
     * <li>Class definition failed to load {@link NoClassDefFoundError}
     * </pre>
     * <p>
     * Due to the way errors are controlled, no class present in the
     * resulting list will be <code>null</code>. However, the empty list
     * may be returned, if no classes were loaded. Should this happen,
     * either a global fault has happened in the loading process, or the
     * directory contained no classes.
     * <p>
     * Even though the definition is a list, duplicates will not be added
     * to the list by this method.
     *
     * @param directory The directory to recursively search for classes
     *                  in.
     * @return The list of all found classes.
     * @throws NullPointerException if the given <code>directory</code> is
     *                              <code>null</code>.
     * @see org.obicere.cc.util.FileLoader#search(String)
     */

    public static List<Class<?>> loadClassesFrom(final String directory) {
        Objects.requireNonNull(directory);
        final LinkedList<Class<?>> classes = new LinkedList<>();
        try {
            final List<String> loader = FileLoader.searchPath(directory, ".class");
            loader.forEach(path -> {
                try {
                    final Class<?> cls = forName(directory, path);
                    if (cls != null && !classes.contains(cls)) {
                        classes.add(cls);
                    }
                } catch (final Throwable ignored) {
                    // Failed to load an individual class
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * Attempts to load the classes from the class path only. This is
     * handled by the recursive definition in the {@link
     * org.obicere.cc.util.FileLoader}.
     * <p>
     * Errors in the loading of an individual class are ignored. Due to
     * this, notifications of absent classes won't be generated. Should an
     * expected class not be present, it is up to the caller to handle the
     * situation.
     * <p>
     * Reasons for a class load to fail are:
     * <pre>
     * <li>Error in static initialization
     * <li>Missing resources
     * <li>Class definition failed to load {@link NoClassDefFoundError}
     * </pre>
     * <p>
     * Due to the way errors are controlled, no class present in the
     * resulting list will be <code>null</code>. However, the empty list
     * may be returned, if no classes were loaded. Should this happen,
     * either a global fault has happened in the loading process, or the
     * directory contained no classes.
     * <p>
     * Even though the definition is a list, duplicates will not be added
     * to the list by this method.
     *
     * @return The list of all found classes.
     * @see org.obicere.cc.util.FileLoader#search(String)
     */

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
                } catch (final Throwable ignored) {
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * Attempts to load a class by name, and if that fails - by file. This
     * differs from {@link org.obicere.cc.util.Reflection#forName(String,
     * String)}, as this will function off of the working directory.
     * <p>
     * However, the core functionality is the same, as this delegates to
     * the alternative <code>forName</code> method.
     *
     * @param name The name of the class attempting to load.
     * @return The loaded class, if done successfully. Otherwise,
     * <code>null</code>.
     * @see org.obicere.cc.util.Reflection#forName(String, String)
     */

    private static Class<?> forName(final String name) {
        return forName("", name);
    }

    /**
     * Attempts to load a class by name, and if that fails - by file. This
     * will call the system class loader's {@link ClassLoader#loadClass(String)}
     * function, to check to see if the class loader can load the class
     * from the class path.
     * <p>
     * Since the system class loader can be easily modified, this is
     * counter-measured by the {@link org.obicere.cc.util.Reflection.ClassDefiner}
     * to ensure that loads by a defective system class loader do not
     * persist.
     * <p>
     * Exceptions occurring during the loading of the class are ignored.
     *
     * @param directory The The directory to locate the file in.
     * @param name      The name of the class attempting to load.
     * @return The loaded class, if done successfully. Otherwise,
     * <code>null</code>.
     */

    private static Class<?> forName(final String directory, final String name) {
        try {
            final Class<?> cls = LOADER.loadClass(name);
            if (cls != null) {
                return cls;
            }
        } catch (final Throwable ignored) {
        }
        return DEFINER.attemptDefine(directory, name);
    }

    /**
     * A simple class loader used to load up the file by name as a class.
     * This functions as a single-class {@link java.net.URLClassLoader} in
     * the sense that the given class might not be in the class path.
     * <p>
     * This is meant as a backup for if the system class loader fails to
     * load a class. This will happen if the given class is not in the
     * classpath, but is rather in a directory.
     * <p>
     * In the case that the regular class loader failed to load a class
     * when it should have, the load will be attempted a second time to
     * reassure the result.
     */

    private static class ClassDefiner extends ClassLoader {

        /**
         * Attempts to load a class from the given name in the given
         * directory. If <code>directory</code> is <code>null</code>, then
         * the class loader will check to see if the class was already
         * defined by name.
         * <p>
         * Should the <code>directory</code> not be <code>null</code>, then
         * the class will attempted to be loaded in a more direct way.
         * First this will attempt to load through two means:
         * <pre>
         * 1) The class loader will check to see if the class - by name -
         * has already been defined.
         * 2) The file will be loaded and a class definition will attempt
         * to be created from the <code>byte[]</code> data of the file.
         * </pre>
         * <p>
         * The <code>name</code> should include the package declaration,
         * such as: <code>java.lang.String</code>.
         *
         * @param directory The directory to locate the file in.
         * @param name      The name of the class attempting to load.
         * @return The loaded class if done successfully, otherwise
         * <code>null</code>.
         * @throws NullPointerException name is <code>null</code>.
         * @see ClassLoader#defineClass(String, byte[], int, int)
         * @see ClassLoader#findLoadedClass(String)
         */

        public Class<?> attemptDefine(final String directory, final String name) {
            Objects.requireNonNull(name);
            if (directory == null) {
                return findLoadedClass(name);
            }
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
                return null;
            }
        }

    }

}