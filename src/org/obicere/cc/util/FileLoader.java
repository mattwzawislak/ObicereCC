package org.obicere.cc.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A tool to recursively load files by a certain extension in a specified
 * location. If no location is specified, the classpath will be used
 * instead, enumerating each value. The classpath is accessible through the
 * Java System property by: <code>java.class.path</code>. Independent
 * arguments in the classpath should be split by the system file separator.
 * This is denoted by the system property <code>path.separator</code>. On
 * UNIX, it is most commonly <code>':'</code>. On Windows, it is most
 * commonly <code>';'</code>.
 * <p>
 * The file loader will also ignore certain jars, according to the
 * specification. By default, jars packed from the JVM, JDK and the
 * IntelliJ IDE are blocked.
 * <p>
 * These jars can be re-enabled through the {@link org.obicere.cc.util.FileLoader#removeIgnoreJar(
 *String)}, and other jars can be disabled through the {@link
 * org.obicere.cc.util.FileLoader#addIgnoreJar(String)}.
 * <p>
 * Any class files loaded by this utility will be handled separately. When
 * loading from a directory, the package name can only be approximated. The
 * specified directory to load from should be the directory containing the
 * top-level package.
 * <p>
 * For example, a class <code>Foo.class</code> with package
 * <code>org.example</code>. This class, if stored in the file
 * <code>/Files/org/example/Foo.class</code>, should be handled as
 * properly:
 * <p>
 * Since the file is contained within the package contents, the directory
 * containing the top-level package would be <code>/Files/</code>. This
 * directory should be the directory being searched to get the correct
 * package declaration, based off of the subdirectories
 * <code>org/example/</code>. This will therefore generate the package
 * value of <code>org.example</code>, which is indeed correct. However, if
 * we used the parent of <code>Files</code>, the generated package would
 * have been: <code>Files.org.example</code>, which is incorrect.
 * <p>
 * Likewise, classes contained within jars should be treated the same. The
 * jar should contain the top-level package directly in the first set of
 * entries.
 * <p>
 * Most jars generated from a tool will follow this form, so using the
 * above example again, we can see how the jar should be structured to
 * correctly parse the package. Assume the contents are in a jar file named
 * <code>Files.jar</code>:
 * <p>
 * Upon opening the jar file, either with a zip utility or {@link
 * java.util.jar.JarFile} instance, the first set of entries should contain
 * the directory <code>org</code>. Within there, the directory
 * <code>example</code>. And within there, the <code>Foo.class</code> file.
 * This means that relative to the jar file, the <code>Foo</code> class
 * would have a location:
 * <p>
 * <code>Files.jar!org/example/Foo.class</code>
 * <p>
 * Which would parse the package <code>org.example</code>, the correct
 * package.
 * <p>
 * Once the package has been resolved, the <code>.class</code> extension is
 * dropped. Meaning that the {@link String} representation of the class
 * loaded could be directly fed into a {@link ClassLoader}.
 * <p>
 * For example: <code>
 * <pre>
 * // By using the .class extension,
 * // all the files will be in the class form described above
 * final List&lt;String&gt; files = FileLoader.searchClassPath(".class");
 * final List&lt;Class&lt;?&gt;&gt; classes = new LinkedList&lt;&gt;();
 * for(final String file : files){
 *     try{
 *         classes.add(ClassLoader.getSystemClassLoader().loadClass("SomeClass"));
 *     } catch (final ClassNotFoundException e){
 *         System.err.println("Could not load class: " + file);
 *     }
 * }
 * </pre>
 * </code>
 * <p>
 * However, if the <code>.class</code> extension is <b>not</b> specified,
 * then classes loaded by this utility will <b>not</b> be in the class
 * notation. This means that the above code would not work if a generic
 * extension was used, since no class found would be loadable from that
 * method call.
 *
 * @author Obicere
 * @version 1.0
 * @see org.obicere.cc.util.FileLoader#addIgnoreJar(String)
 * @see org.obicere.cc.util.FileLoader#removeIgnoreJar(String)
 * @see org.obicere.cc.util.FileLoader#searchPath(String, String)
 * @see org.obicere.cc.util.FileLoader#searchClassPath(String)
 */
public class FileLoader {

    /**
     * The set of ignored archives this utility will not enter. This is
     * useful to reduce running time, if certain jars do not need to be
     * searched. Especially on the classpath, where many jars could be on
     * the classpath, but only a handful could contain the type of class.
     */

    private static final HashSet<String> IGNORED_JARS = new HashSet<>();

    static {
        // Ignore IntelliJ jar files.

        IGNORED_JARS.add("charsets.jar");
        IGNORED_JARS.add("deploy.jar");
        IGNORED_JARS.add("javaws.jar");
        IGNORED_JARS.add("jce.jar");
        IGNORED_JARS.add("jfr.jar");
        IGNORED_JARS.add("jfxswt.jar");
        IGNORED_JARS.add("jsse.jar");
        IGNORED_JARS.add("management-agent.jar");
        IGNORED_JARS.add("plugin.jar");
        IGNORED_JARS.add("resources.jar");
        IGNORED_JARS.add("rt.jar");  // Main Java jar for IntelliJ
        IGNORED_JARS.add("access-bridge-64.jar");
        IGNORED_JARS.add("cldrdata.jar");
        IGNORED_JARS.add("dnsns.jar");
        IGNORED_JARS.add("jaccess.jar");
        IGNORED_JARS.add("jfxrt.jar");
        IGNORED_JARS.add("localedata.jar");
        IGNORED_JARS.add("nashorn.jar");
        IGNORED_JARS.add("sunec.jar");
        IGNORED_JARS.add("sunjce_provider.jar");
        IGNORED_JARS.add("sunmscapi.jar");
        IGNORED_JARS.add("sunpkcs11.jar");
        IGNORED_JARS.add("zipfs.jar");
        IGNORED_JARS.add("idea_rt.jar");

    }

    private static volatile String lazyClassPath;

    private final LinkedList<String> list = new LinkedList<>();

    private final String extension;
    private final String prefix;

    /**
     * Constructs a new file loader to search for files with the given
     * <code>extension</code>. This will check each directory on the
     * classpath iteratively.
     *
     * @param extension The extension of files to load.
     * @see org.obicere.cc.util.FileLoader#getClassPath()
     */

    private FileLoader(final String extension) {
        this.extension = extension;
        this.prefix = null;
    }

    /**
     * Constructs a new file loader to search for files in the given
     * directory/archive and subdirectories recursively.
     *
     * @param prefix    The directory or archive to search recursively.
     * @param extension The extension of files to load.
     */

    private FileLoader(final String prefix, final String extension) {
        this.extension = extension;
        this.prefix = prefix;
    }

    /**
     * Registers a new archive to be ignored by the file loader utility.
     * This is done by file name, not by file location and name. Ignoring
     * an archive by name, but only in a specific directory is not possible
     * as of <code>v1.0</code>.
     *
     * @param name The archive name to ignore.
     * @return <code>true</code> if the archive was not already ignored.
     */

    public static boolean addIgnoreJar(final String name) {
        return IGNORED_JARS.add(name);
    }

    /**
     * Specifies that the file loader utility should no longer be ignored,
     * if it was ignored to begin with. This is done by file name, not by
     * file location and name. Ignoring an archive by name, but only in a
     * specific directory is not possible as of <code>v1.0</code>.
     * <p>
     * To allow access to the standard Java jar, calling this method with
     * the jar name <code>"rt.jar"</code> will do so. This will however
     * have an adverse affect on the running time.
     *
     * @param name The archive name to allow access to.
     * @return <code>true</code> if the archive was being ignored and is no
     * longer being ignored.
     */

    public static boolean removeIgnoreJar(final String name) {
        return IGNORED_JARS.remove(name);
    }

    /**
     * Creates a new file loader query on the given <code>path</code>,
     * searching for files with the given <code>extension</code>. This is
     * done recursively, in alphabetical order using an in-order search.
     * <p>
     * Classes returned here, if searching for classes, will be in class
     * notation.
     *
     * @param path      The path to search in. All files will be headed
     *                  with this directory or archive.
     * @param extension The extension of files to load. If the extension is
     *                  <code>".class"</code>, then all classes found will
     *                  be in a standard class notation: <code>org.example.Foo</code>,
     *                  for example. Otherwise, standard file naming
     *                  systems will be used: <code>/user/test/foo.txt</code>.
     * @return The list of strings corresponding to files or classes loaded
     * by this run.
     */

    public static List<String> searchPath(final String path, final String extension) {
        final FileLoader loader = new FileLoader(path, extension);
        return loader.find();
    }

    /**
     * Creates a new file loader query on the classpath, searching for
     * files with the given <code>extension</code>. The classpath is
     * searched iteratively. The first directory in the path will be
     * searched before the second, before the third and so on.
     * <p>
     * The classpath is modifiable by using the <code>java.class.path</code>
     * system property. Unless, the main class loader has its own classpath
     * defined, which is done by having a method such as:
     * <p>
     * <code>
     * <pre>
     * public String getClassPath(final Class&lt;?&gt; cls){
     *     // Return custom classpath here.
     * }
     * </pre>
     * </code>
     * <p>
     * The class loader's method will be checked first, then the system
     * property will be polled.
     *
     * @param extension The extension of files to load. The extension of
     *                  files to load. If the extension is <code>".class"</code>,
     *                  then all classes found will be in a standard class
     *                  notation: <code>org.example.Foo</code>, for
     *                  example. Otherwise, standard file naming systems
     *                  will be used: <code>/user/test/foo.txt</code>.
     * @return The list of strings corresponding to files or classes loaded
     * by this run.
     */

    public static List<String> searchClassPath(final String extension) {
        final FileLoader loader = new FileLoader(extension);
        return loader.find();
    }

    /**
     * Runs the query on the given file loader properties. This is
     * synchronized, so multiple queries won't clash. Should the loaders by
     * multiple-use, query caching would be performed here.
     * <p>
     * The classpath is split by the {@link java.io.File#pathSeparator}. On
     * UNIX this is commonly <code>':'</code>. On Windows this is commonly
     * <code>';'</code>. This is modifiable through the
     * <code>path.separator</code> system property.
     *
     * @return The list of all files loaded from this query.
     * @see org.obicere.cc.util.FileLoader#getClassPath()
     * @see org.obicere.cc.util.FileLoader#search(String)
     */

    private List<String> find() {
        if (prefix != null) {
            // If we have a specified a directory to search.
            search(prefix);
        } else {
            // Otherwise we have to check the classpath.
            final String classpath = getClassPath();
            final StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
            while (tokenizer.hasMoreTokens()) {
                search(tokenizer.nextToken());
            }
        }
        return list;
    }

    /**
     * Lazily evaluates the classpath. This will persist across the
     * independent file loaders, since theoretically, the classpath should
     * not change once the program has been started.
     *
     * @return The classpath for this JVM instance.
     */

    private synchronized String getClassPath() {
        if (lazyClassPath == null) {
            return lazyClassPath = loadClassPath();
        }
        return lazyClassPath;
    }

    /**
     * Attempts to load the classpath through two mutable sources. The
     * first source is through the current system class loader. This is if
     * the main class loader has its own classpath defined, which is done
     * by having a method such as:
     * <p>
     * <code>
     * <pre>
     * public String getClassPath(final Class&lt;?&gt; cls){
     *     // Return custom classpath here.
     * }
     * </pre>
     * </code>
     * <p>
     * The classpath is modifiable by using the <code>java.class.path</code>
     * system property otherwise.
     * <p>
     * The class loader's classpath will be called first, as it should take
     * precedence.
     *
     * @return The class path for this JVM instance.
     */

    private String loadClassPath() {
        try {
            final ClassLoader loader = ClassLoader.getSystemClassLoader();
            final Method method = loader.getClass().getMethod("getClassPath", (Class<?>) null);
            if (method != null) {
                return (String) method.invoke(loader, (Object) null);
            }
        } catch (final Exception ignored) { // Whatever, worth a shot.
        }
        return System.getProperty("java.class.path");
    }

    /**
     * Searches the given <code>file</code> recursively. This is
     * accomplished based on the type of the file.
     * <p>
     * If it is a directory, {@link org.obicere.cc.util.FileLoader#lookInDirectory(String,
     * java.io.File)} is called to recursively expand on the directories.
     * <p>
     * If it is an archive, {@link org.obicere.cc.util.FileLoader#lookInArchive(java.io.File)}
     * is called to extract the archive and enumerate its elements. Then a
     * recursive directory search takes over, but modified from the
     * standard search, especially attuned for archives.
     *
     * @param file The file to start searching in.
     */

    private void search(final String file) {
        final File dir = new File(file);
        if (dir.isDirectory()) {
            lookInDirectory("", dir);
        }
        if (dir.isFile()) {
            final String name = dir.getName().toLowerCase();
            if (name.endsWith(".zip") || name.endsWith(".jar")) {
                if (IGNORED_JARS.contains(name)) {
                    return;
                }
                lookInArchive(dir);
            }
        }
    }

    /**
     * Recursively searches the directory for files that match the query.
     * The ordering is in depth-first search. Files will be accessed in
     * alphabetical order, or based off of the default {@link
     * java.io.FileSystem} file listing preferences.
     * <p>
     * If the extension name is equal to <code>.class</code>, this will
     * approximate the package of the class based on the value of
     * <code>dir</code>. <code>dir</code> should contain the top-level
     * package for the class to provide proper parsing. Since this utility
     * merely loads the file locations, checking the package name to the
     * actual class information is not manageable.
     * <p>
     * For example, given a class <code>org.example.Foo</code>, located in
     * the directory <code>"/user/bin/"</code>:
     * <p>
     * <code>dir</code> should be equal to <code>"/user/bin/"</code>.
     * <code>Foo</code> should be in the <code>/user/bin/org/example/</code>
     * directory. This will provide the proper parsing evaluation of
     * <code>org.example.Foo</code>.
     * <p>
     * Comparing to when <code>dir</code> equals <code>"/user/"</code>,
     * which would provide the improper parse of <code>bin.org.example.Foo</code>.
     * <p>
     * The <code>.class</code> extension will also be dropped, allowing
     * easy delegation to a class loader to load the found class.
     * <p>
     * For regular files, no changes to the file's location will take place
     * and the extension will persist. All files will be headed with the
     * found directory, which if specified through the {@link
     * org.obicere.cc.util.FileLoader#searchPath(String, String)} method,
     * is defined by the user. Otherwise it will be a directory found in
     * the classpath.
     *
     * @param name The intermediate file. The current file being indexed is
     *             therefore equal to <code>name + dir</code>, where
     *             <code>+</code> is string concatenation.
     * @param dir  The top level directory the search started from.
     */

    private void lookInDirectory(final String name, final File dir) {
        final File[] files = dir.listFiles();
        Objects.requireNonNull(files);
        for (final File file : files) {
            final String fileName = file.getName();
            if (file.isFile() && fileName.toLowerCase().endsWith(extension)) {
                if (extension.equalsIgnoreCase(".class")) {
                    final String className = fileName.substring(0, fileName.length() - 6);
                    list.add(name.replaceAll("[\\\\/]", ".") + className);
                } else {
                    list.add(name + fileName);
                }
            }
            if (file.isDirectory()) {
                lookInDirectory(name + fileName + File.separator, file);
            }
        }

    }

    /**
     * Flattens an archive and enumerates the contents searching for the
     * values.
     * <p>
     * If the extension name is equal to <code>.class</code>, then the
     * package name has to be approximated. However, compared to the
     * directory search, this can be done with more certainty. Assuming
     * most jars are packed properly, the first level of the jar should
     * contain the top-level package of the class. For example, the class
     * <code>org.example.Foo</code>
     * <p>
     * This should be located in the directory:
     * <p>
     * <pre>
     * MyJar.jar
     * |-- org
     * |    |-- example
     * |           |-- Foo.class
     * </pre>
     * <p>
     * This will translate to the file directory, <code>/org/example/Foo.class</code>.
     * The format will then convert the file to the class notation:
     * <code>org.example.Foo</code>.
     * <p>
     * The <code>.class</code> extension will also be dropped, allowing
     * easy delegation to a class loader to load the found class.
     * <p>
     * For regular files, no changes to the file's location will take place
     * and the extension will persist. All files will be headed with the
     * found directory, which if specified through the {@link
     * org.obicere.cc.util.FileLoader#searchPath(String, String)} method,
     * is defined by the user. Otherwise it will be a directory found in
     * the classpath.
     *
     * @param archive The archive to enumerate.
     */

    private void lookInArchive(final File archive) {
        final JarFile jarFile;
        try {
            jarFile = new JarFile(archive);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String entryName = entry.getName();
            if (entryName.toLowerCase().endsWith(extension)) {
                if (extension.equalsIgnoreCase(".class")) {
                    final String className = entryName.substring(0, entryName.length() - 6).replaceAll("[\\\\/]", ".");
                    list.add(className);
                } else {
                    list.add(entryName);
                }
            }
        }
    }
}
