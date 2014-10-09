package org.obicere.cc.projects;

import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.methods.IOUtils;
import org.obicere.cc.methods.Reflection;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Project {

    private static final String[]            DIFFICULTY = new String[]{"Beginner", "Intermediate", "Advanced", "Challenging", "Legendary"};
    public static final  LinkedList<Project> DATA       = new LinkedList<>();

    private final String         name;
    private final RunnerManifest manifest;
    private final Class<?>       runnerClass;
    private final Runner         runner;

    public Project(final Class<?> runnerClass, final String name) throws ClassNotFoundException {
        this.runnerClass = runnerClass;
        this.runner = (Runner) Reflection.newInstance(runnerClass);
        this.name = name;
        this.manifest = runnerClass.getAnnotation(RunnerManifest.class);
    }

    public static void loadCurrent() {
        final LinkedList<Class<?>> list = Reflection.loadClassesFrom(Paths.SOURCES);
        final Class<Runner> cls = Runner.class;
        Reflection.filterAsSubclassOf(cls, list);
        list.forEach(Project::add);
    }

    private static void add(final Class<?> cls) {
        try {
            final String name = cls.getSimpleName();
            DATA.add(new Project(cls, name.substring(0, name.length() - 6)));  // Remove Runner
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetData() {
        DATA.clear();
    }

    public static List<Project> getData() {
        return DATA;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return manifest.description();
    }

    public double getVersion() {
        return manifest.version();
    }

    public String getAuthor() {
        return manifest.author();
    }

    public int getDifficulty() {
        return manifest.difficulty();
    }

    public static String getDifficultyString(final int difficulty) {
        return DIFFICULTY[difficulty - 1];
    }

    public String getSortName() {
        return manifest.difficulty() + getName();
    }

    public String getCurrentCode(final Language language) {
        try {
            final File file = getFile(language);
            if (file.exists()) {
                final byte[] data = IOUtils.readData(file);
                return new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return language.getSkeleton(this);
    }

    public File getFile(final Language language) {
        return new File(language.getDirectory(), name + language.getSourceExtension());
    }

    public File getFileName(final Language language) {
        return new File(language.getDirectory(), name);
    }

    public Class<?> getRunnerClass() {
        return runnerClass;
    }

    public Runner getRunner() {
        return runner;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + manifest.difficulty() * 17;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Project && o.hashCode() == this.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean save(final String code, final Language language) {
        try {
            IOUtils.write(getFile(language), code.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}