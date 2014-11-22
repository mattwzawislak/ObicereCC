package org.obicere.cc.util;

import com.sun.org.apache.xpath.internal.Arg;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author Obicere
 */
public class ArgumentParser {

    private final List<Argument<?>> arguments = new LinkedList<>();

    private final String[] args;

    public ArgumentParser(final String... args) {
        this.args = args;
    }

    public void provide(final Argument<?> argument) {
        Objects.requireNonNull(argument);
        arguments.add(argument);
    }

    public void parse() {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            String name = args[i];
            String assignment = null;
            if (name == null) {
                continue;
            }
            name = name.replaceFirst("-*", ""); // Get rid of the preceding dashes to show it is an
                                                // argument and not just a variable.
            final int assignmentIndex = name.indexOf('=');
            if(assignmentIndex > 0){
                // We have an assignment
                name = name.substring(0, assignmentIndex);
                assignment = name.substring(assignmentIndex + 1); // +1 to avoid the =
            }
            Argument<?> argument = null;
            top:
            for (final Argument<?> arg : arguments) {
                if (arg.getName().equals(name)) {
                    argument = arg;
                    break;
                }
                for (final String alias : arg.getAliases()) {
                    if (name.equals(alias)) {
                        argument = arg;
                        break top;
                    }
                }
            }
            if(argument == null){
                continue;
            }
            arguments.remove(argument);
            if(assignment == null){ // Dealing with true/false expression iff the type is of boolean
                if(argument.get().getClass().equals(Boolean.class)){
                } else {
                    if(i + 1 == args.length) {
                        return; // We are at the end of the cycle anyway, no way to deal with this value
                    }
                }
            }
        }
    }

}
