package org.obicere.cc.tasks.projects;

import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.gui.projects.ProjectPanel;
import org.obicere.cc.gui.projects.ProjectSelector;
import org.obicere.cc.methods.IOUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;

public class Project {

    public static final String[] DIFFICULTY = new String[]{"Beginner", "Intermediate", "Advanced", "Challenging", "Legendary"};
    public static final LinkedList<Project> DATA = new LinkedList<>();

    private static final File RUNNER_LOCATION = new File(Paths.SOURCE);
    private static final ClassLoader RUNNER_CLASS_LOADER;

    static {
        ClassLoader loader = null;
        try {
            final URL url = RUNNER_LOCATION.toURI().toURL();
            final URL[] urls = new URL[]{url};
            loader = URLClassLoader.newInstance(urls);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }
        RUNNER_CLASS_LOADER = loader;
    }

    private final String name;
    private final Manifest manifest;
    private final Class<?> runner;
    private boolean complete;

    public Project(final String name, final boolean complete) throws ClassNotFoundException {
        this.runner = loadRunner(name);
        this.name = name;
        this.complete = complete;
        this.manifest = runner.getAnnotation(Manifest.class);
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

    public String getSortName() {
        return manifest.difficulty() + getName();
    }

    @Override
    public String toString() {
        return getName();
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

    public Class<?> getRunner() {
        return runner;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + manifest.difficulty() * 17;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
        for (final ProjectPanel panel : ProjectSelector.getProjectList()) {
            if (panel.getProject().equals(this)) {
                panel.setComplete(complete);
                return;
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Project && o.hashCode() == this.hashCode();
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

    private Class<?> loadRunner(final String name) throws ClassNotFoundException {
        return RUNNER_CLASS_LOADER.loadClass(name + "Runner");
    }

    public static void loadCurrent() {
        String in;
        try {
            final File data = new File(Paths.DATA + File.separator + "data.dat");
            in = new String(IOUtils.readData(data));
        } catch (final Exception e) {
            e.printStackTrace();
            return;
        }
        final File root = new File(Paths.SOURCE);
        if (!root.exists()) {
            return;
        }
        final String[] list = root.list();
        for (final String name : list) {
            if (name != null) {
                final int idx = name.indexOf("Runner.class");
                if (idx == -1) {
                    continue;
                }
                final String projectName = name.substring(0, idx);
                final String data = String.format("|%040x|", new BigInteger(projectName.getBytes()));
                try {
                    final Project project = new Project(projectName, in.contains(data));
                    DATA.add(project);
                } catch (final ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}