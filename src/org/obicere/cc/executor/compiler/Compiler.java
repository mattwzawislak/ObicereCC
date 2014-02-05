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
    private final String sourceExt;
    private final String compiledExt;
    private final CompilerCommand[] commands;

    private CompilerCommand workingCommand;

    public Compiler(final String name, final String sourceExt, final String compiledExt, final CompilerCommand[] commands) {
        this.name = name;
        this.sourceExt = sourceExt;
        this.compiledExt = compiledExt;
        this.commands = commands;
    }

    public CompilerCommand getCompilerCommand() {
        if (workingCommand != null) {
            return workingCommand;
        }
        for (final CompilerCommand command : commands) {
            if (command.check()) {
                workingCommand = command;
                break;
            }
        }
        // Throw a message
        return null;
    }

    public Result[] compileAndRun(final Project project) {
        try {
            final String command = getCommand(project);
            final String[] stream = ProcessRunner.run(command);
        } catch (final Exception e) {
            return null;
        }
    }

    public String getCommand(final Project project) {
        final File file = project.getFile();
        final CompilerCommand command = getCompilerCommand();
        String exec = command.getFormat();
        exec = exec.replace("$exec", command.getProgram());
        exec = exec.replace("$path", file.getParent());
        exec = exec.replace("$file", file.getName());
        exec = exec.replace("$sext", sourceExt);
        exec = exec.replace("$cext", compiledExt);
        return exec;
    }

    public String getName() {
        return name;
    }

}
