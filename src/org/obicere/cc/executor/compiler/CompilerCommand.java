package org.obicere.cc.executor.compiler;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.ProcessRunner;

/**
 * @author Obicere
 */
public class CompilerCommand {

    private final String command;
    private final String format;

    private boolean installed;

    public CompilerCommand(final String command, final String format) {
        this.command = command;
        this.format = format;
    }

    public boolean check() {
        try {
            final String[] str = ProcessRunner.run(command);
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
            return !str[0].equals(String.format(failure, command));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
