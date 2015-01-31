package org.obicere.cc.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Obicere
 */
public class ArgumentParser {

    private final List<Argument> arguments = new LinkedList<>();

    private final String[] args;

    public ArgumentParser(final String... args) {
        this.args = args;
    }

    public void provide(final Argument argument) {
        Objects.requireNonNull(argument);
        arguments.add(argument);
    }

    public void parse() {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            String name = args[i];
            if (name == null) {
                continue;
            }
            // Get rid of the preceding dashes to show it is an
            // argument and not just a variable.
            name = name.replaceFirst("-*", "");

            final Argument argument = argumentFor(name);
            if (argument == null) {
                continue;
            }
            arguments.remove(argument);

            final int assignmentIndex = name.indexOf('=');
            if (assignmentIndex > 0) {
                // We have an assignment
                if (assignmentIndex + 1 == name.length()) { // No value available
                    argument.set(null);
                    continue;
                }
                name = name.substring(0, assignmentIndex);
                final String assignment = name.substring(assignmentIndex + 1); // +1 to avoid the =
                argument.set(assignment);
            } else {
                // Dealing with true/false expression iff the type is of boolean
                if (argument.isConditional()) {
                    argument.set(true);
                } else {
                    if (i + 1 == args.length) {
                        return; // We are at the end of the cycle, no way to deal with this value
                    }
                    final String next = args[++i];
                    if (next == null) {
                        return;
                    }
                    argument.set(next);
                }
            }
        }
    }

    private Argument argumentFor(final String name) {
        for (final Argument arg : arguments) {
            if (arg.getName().equals(name)) {
                return arg;
            }
            for (final String alias : arg.getAliases()) {
                if (name.equals(alias)) {
                    return arg;
                }
            }
        }
        return null;
    }

}
