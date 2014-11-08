package org.obicere.cc.executor.compiler;

import org.obicere.cc.util.StringSubstitute;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProcessExecutor {

    private static final String ERROR_NO_PROCESS = "Could not find process. Make sure your path is set correctly.";

    private final StringSubstitute substitute;

    private final String    name;
    private final Command[] commands;

    private Command workingCommand;

    public ProcessExecutor(final String name, final String sourceExt, final String compiledExt, final Command[] commands) {
        this.name = name;
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

    public String[] process(final File file, final Object... varargs) {
        try {
            final String command = getCommand(file, varargs);
            if (command.equals(ERROR_NO_PROCESS)) {
                return new String[]{ERROR_NO_PROCESS};
            }
            return SubProcess.run(command);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return new String[]{"Internal Error"};
    }

    public String getCommand(final File file, final Object... varargs) {
        final Command command = getCompilerCommand();
        if (command == null) {
            return ERROR_NO_PROCESS;
        }
        final StringBuilder args = new StringBuilder();
        final int length = varargs.length;
        for (int i = 0; i < length; i++) {
            if (i != 0) {
                args.append(' ');
            }
            final Object obj = varargs[i];
            final String value = String.valueOf(obj);
            if (value.contains(" ")) {
                args.append('"');
                args.append(value);
                args.append('"');
                continue;
            }
            args.append(value);
        }

        final String exec = command.getFormat();
        final StringSubstitute substitute = (StringSubstitute) this.substitute.clone();

        substitute.put("exec", command.getProgram());
        substitute.put("path", file.getParent());
        substitute.put("name", file.getName());
        substitute.put("file", file.getAbsolutePath());
        substitute.put("varargs", args.toString());

        return substitute.apply(exec).trim();
    }

    public String getName() {
        return name;
    }
}
