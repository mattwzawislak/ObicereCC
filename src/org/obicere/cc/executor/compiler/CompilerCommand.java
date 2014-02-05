package org.obicere.cc.executor.compiler;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.ProcessRunner;

/**
 * @author Obicere
 */
public class CompilerCommand {

    private final String program;
    private final String format;

    public CompilerCommand(final String program, final String format) {
        this.program = program;
        this.format = format;
    }

    public String getFormat(){
        return format;
    }

    public String getProgram(){
        return program;
    }

    public boolean check() {
        try {
            final String[] str = ProcessRunner.run(program);
            if (str.length <= 0) {
                return false;
            }
            final String failure;
            switch (Global.getOS()) {
                case WINDOWS:
                    failure = "'%s' is not recognized as an internal or external command,";
                    break;
                default:
                    failure = "%s";
                    break;
            }
            return !str[0].equals(String.format(failure, program));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
