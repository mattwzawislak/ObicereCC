package org.obicere.cc.executor.compiler;

import org.obicere.cc.executor.ProcessRunner;
import org.obicere.cc.methods.StringSubstitute;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class Compiler {

    private static final String ERROR_NO_JDK = "Could not find JAVAC. Make sure your path is set correctly.";

    private final StringSubstitute substitute;

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

        final Map<String, String> map = new LinkedHashMap<>();
        map.put("sext", sourceExt);
        map.put("cext", compiledExt);

        this.substitute = new StringSubstitute(map);
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

        final String exec = command.getFormat();
        final StringSubstitute substitute = (StringSubstitute) this.substitute.clone();

        substitute.put("exec", command.getProgram());
        substitute.put("path", file.getParent());
        substitute.put("name", file.getName());
        substitute.put("file", file.getAbsolutePath());

        return substitute.apply(exec).trim();
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
