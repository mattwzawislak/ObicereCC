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
 * These jars can be re-enabled through the {@link org.obicere.cc.util.FileLoader#removeIgnoreJar(String)},
 * and other jars can be disabled through the {@link
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
 * dropped. Meaning that the {@link java.lang.String} representation of the
 * class loaded could be directly fed into a {@link java.lang.ClassLoader}.
 * <p>
 * For example:
 * <pre>
 * <code>// By using the .class extension,
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
 * </code>
 * </pre>
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
     * <code>extension</code> on the working directory. The working
     * directory is defined as the {@link Class#getProtectionDomain()}.
     * This will also search the classpath.
     *
     * @param extension The extension of files to load.
     */

    private FileLoader(final String extension) {
        this.extension = extension;
        this.prefix = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
    }

    /**
     * Constructs a new file loader to search for files in the given
     * directory/archive and subdirectories recursively. This will also
     * search the classpath.
     *
     * @param prefix    The directory or archive to search recursively.
     * @param extension The extension of files to load.
     */

    private FileLoader(final String prefix, final String extension) {
        this.extension = extension;
        this.prefix = prefix;
    }

    public static boolean addIgnoreJar(final String name) {
        return IGNORED_JARS.add(name);
    }

    public static boolean removeIgnoreJar(final String name) {
        return IGNORED_JARS.remove(name);
    }

    public static List<String> searchPath(final String path, final String extension) {
        final FileLoader loader = new FileLoader(path, extension);
        return loader.find();
    }

    public static List<String> searchClassPath(final String extension) {
        final FileLoader loader = new FileLoader(extension);
        return loader.find();
    }

    private List<String> find() {
        if (!list.isEmpty()) {
            return list;
        }

        final String classpath = getClassPath();

        if (prefix != null) {
            search(prefix);
        }

        final StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            search(tokenizer.nextToken());
        }
        return this.list;
    }

    private synchronized String getClassPath() {
        if (lazyClassPath == null) {
            return lazyClassPath = loadClassPath();
        }
        return lazyClassPath;
    }

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

    private void search(final String token) {
        final File dir = new File(token);
        if (dir.isDirectory()) {
            lookInDirectory("", dir);
        }
        if (dir.isFile()) {
            final String name = dir.getName().toLowerCase();
            if (name.endsWith(".zip") || name.endsWith(".jar")) {
                if (IGNORED_JARS.contains(name)) {
                    return;
                }
                this.lookInArchive(dir);
            }
        }
    }

    private void lookInDirectory(final String name, final File dir) {
        final File[] files = dir.listFiles();
        Objects.requireNonNull(files);
        for (final File file : files) {
            final String fileName = file.getName();
            if (file.isFile() && fileName.toLowerCase().endsWith(extension)) {
                if (extension.equalsIgnoreCase(".class")) {
                    final String className = fileName.substring(0, fileName.length() - 6);
                    list.add(name.replace(File.separatorChar, '.') + className);
                } else {
                    list.add(name + fileName);
                }
            }
            if (file.isDirectory()) {
                lookInDirectory(name + fileName + File.separator, file);
            }
        }

    }

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
                    final String className = entryName.substring(0, entryName.length() - 6).replace(File.separatorChar, '.');
                    list.add(className);
                } else {
                    list.add(entryName);
                }
            }
        }
    }
}
