package org.obicere.cc.executor.compiler;

import org.obicere.cc.executor.ProcessRunner;
import org.obicere.cc.executor.Result;
import org.obicere.cc.tasks.projects.Project;

import java.io.File;

/**
 * org.obicere.cc.executor.compiler
 * Created: 2/4/14 3:17 PM
 *
 * @author Obicere
 * @version 1.0
 */
public class Compiler {

    private final String name;

    public Compiler(final String name){
        this.name = name;
    }

    public Result[] compileAndRun(final Project project){
        final String command = getCommand(project);
        return null;
    }

    public String getCommand(final Project project){
        final File file = project.getFile();
        return String.format("javac -g -d %s %s", file.getParent(), file.getPath());
    }

    public String getName(){
        return name;
    }

}
