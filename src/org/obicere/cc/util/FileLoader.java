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
 * @author Obicere
 */
public class FileLoader {

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

    private final LinkedList<String> list = new LinkedList<>();

    private final String extension;
    private final String prefix;

    private FileLoader(final String extension) {
        this.extension = extension;
        this.prefix = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
    }

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

    public static List<String> searchClassPath(final String path, final String extension) {
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

        String classpath = null;

        try {
            final ClassLoader loader = ClassLoader.getSystemClassLoader();
            final Method method = loader.getClass().getMethod("getClassPath", (Class<?>) null);
            if (method != null) {
                classpath = (String) method.invoke(loader, (Object) null);
            }
        } catch (final Exception ignored) {
            classpath = System.getProperty("java.class.path");
        }

        if (prefix != null) {
            search(prefix);
        }

        final StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            search(tokenizer.nextToken());
        }
        return this.list;
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
                    list.add(name + className);
                } else {
                    list.add(name.replace('.', File.separatorChar) + fileName);
                }
            }
            if (file.isDirectory()) {
                lookInDirectory(name + fileName + ".", file);
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
                    final String className = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                    list.add(className);
                } else {
                    list.add(entryName);
                }
            }
        }
    }
}
