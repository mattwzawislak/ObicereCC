package org.obicere.cc.executor.compiler;

import org.obicere.cc.executor.ProcessRunner;

import java.io.File;

public class Compiler {

    private static final String ERROR_NO_JDK = "Could not find JAVAC. Make sure your path is set correctly.";

    private final String    name;
    private final String    sourceExt;
    private final String    compiledExt;
    private final Command[] commands;

    private Command workingCommand;

    public Compiler(final String name, final String sourceExt, final String compiledExt, final Command[] commands) {
        this.name = name;
        this.sourceExt = sourceExt;
        this.compiledExt = compiledExt;
        this.commands = commands;
    }

    public Command getCompilerCommand() {
        if (workingCommand != null) {
            return workingCommand;
        }
        for (final Command command : commands) {
            if (command.check()) {
                workingCommand = command;
                return workingCommand;
            }
        }
        // Throw a message
        return null;
    }

    public String[] compile(final File file) {
        try {
            final String command = getCommand(file);
            if (command == ERROR_NO_JDK) {
                return new String[]{ERROR_NO_JDK};
            }
            return ProcessRunner.run(command);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return new String[]{"Internal Error"};
    }

    public String getCommand(final File file) {
        final Command command = getCompilerCommand();
        if (command == null) {
            return ERROR_NO_JDK;
        }
        String exec = command.getFormat();
        exec = exec.replace("$exec", command.getProgram());
        exec = exec.replace("$path", file.getParent());
        exec = exec.replace("$name", file.getName());
        exec = exec.replace("$file", file.getAbsolutePath());
        exec = exec.replace("$sext", sourceExt);
        exec = exec.replace("$cext", compiledExt);
        return exec.trim();
    }

    public String getName() {
        return name;
    }

    public String getSourceExtension() {
        return sourceExt;
    }

    public String getCompiledExtension() {
        return compiledExt;
    }

}
