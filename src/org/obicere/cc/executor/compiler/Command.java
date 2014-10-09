package org.obicere.cc.executor.compiler;

import java.io.IOException;
import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.ProcessRunner;

public class Command {

    private final String program;
    private final String format;

    public Command(final String program, final String format) {
        this.program = program;
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public String getProgram() {
        return program;
    }

    public boolean check() {
        try {
            final String command;
            final String failure;
            switch (Global.getOS()) {
                case WINDOWS:
                    command = "where " + program;
                    failure = "INFO: Could not find files for the given pattern(s).";
                    break;
                case MAC:
                    command = "command " + program;
                    failure = "-bash: "; // Needs testing
                    break;
                case LINUX:
                    command = "whereis " + program;
                    failure = program + ": "; // TODO: Fix this.
                    break;
                default:
                    return false;
            }
            final String[] str = ProcessRunner.run(command);
            return str.length != 0 && !str[0].startsWith(failure);
        } catch (final IOException e) {

            e.printStackTrace();
        }
        return false;
    }

}
