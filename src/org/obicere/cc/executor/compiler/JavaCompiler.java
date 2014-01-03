package org.obicere.cc.executor.compiler;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.Result;
import org.obicere.cc.methods.CustomClassLoader;
import org.obicere.cc.tasks.projects.Project;
import org.obicere.cc.tasks.projects.Runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author Obicere
 */
public class JavaCompiler extends Compiler {

    @Override
    public boolean isInstalled() {
        try {
            final Process r = Runtime.getRuntime().exec("javac -version");
            final BufferedReader read = new BufferedReader(new InputStreamReader(r.getErrorStream()));
            return !read.readLine().startsWith("'javac' is not recognized");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getName() {
        return "Java";
    }

    @Override
    public String prepareCommand(Project project, File file) {
        return String.format("javac -g -d %s %s", file.getParent(), file.getPath());
    }

    public Result[] runAndGetResults(final Project project) {
        if (project != null && compile(project, project.getFile())) {
            try {
                final String fileName = Global.Paths.JAVA + File.separator + project.getName() + ".class";
                final Class<?> ref = project.getRunner();
                final Class<?> compiled = CustomClassLoader.loadClassFromFile(fileName);
                final Runner runner = (Runner) ref.newInstance();
                return runner.getResults(compiled);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
