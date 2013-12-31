package org.obicere.cc.executor.compiler;

import org.obicere.cc.tasks.projects.Project;

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
        return String.format("javac -g  -d %s %s", file.getParent(), file.getPath());
    }

}
