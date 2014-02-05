package org.obicere.cc.executor.compiler;

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
    private final String[] extensions;
    private final CompilerCommand[] commands;

    private CompilerCommand workingCommand;

    public Compiler(final String name, final String[] extensions, final CompilerCommand[] commands){
        this.name = name;
        this.extensions = extensions;
        this.commands = commands;
    }

    public CompilerCommand getCompilerCommand(){
        if(workingCommand != null){
            return workingCommand;
        }
        for(final CompilerCommand command : commands){
            if(command.check()){
                workingCommand = command;
                break;
            }
        }
        // Throw a message
        return null;
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
