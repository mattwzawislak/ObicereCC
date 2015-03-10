package org.obicere.cc.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Used to parse arguments during the startup of the program. Arguments
 * must be registered here, as well as the program arguments generated from
 * main. <code>
 * <pre>
 * public static void main(final String[] args){
 *     // ...
 *     final ArgumentParser parser = new ArgumentParser(args);
 *     // ...
 * }
 * </pre>
 * </code>
 * <p>
 * Arguments - by aliases - are not checked for collisions. However
 * arguments - by name - will be checked for collisions and the latest
 * argument by that name will be used. This is to avoid potentially
 * ambiguous errors and to easily allow argument-implementation-overriding.
 * However, if this behaviour is not intended, names can be checked with
 * {@link org.obicere.cc.util.ArgumentParser#getArgument(String)} to ensure
 * this does not happen.
 * <p>
 * The order of argument parsing is left-to-right. So the first arguments
 * supplied will be the first to be parsed.
 * <p>
 * The format of the argument handling is as follows:
 * <p>
 * If the argument is not conditional, then it can accept the following
 * formats: <code>
 * <pre>
 * name=value
 * -name=value
 * --name=value
 * ...
 * name value
 * -name value
 * --name value
 * ...
 * </pre>
 * </code>
 * <p>
 * Where <code>name</code> corresponds to the argument name, preceded by
 * any number of dashes, and <code>value</code> is the new value for this
 * argument.
 * <p>
 * For the <code>name value</code> arguments, if there is no
 * <code>value</code> argument, no assignment will take place.
 * <p>
 * If the argument is conditional, then then it can accept the following
 * formats: <code>
 * <pre>
 * name
 * -name
 * --name
 * ...
 * name=value
 * -name=value
 * --name=value
 * ...
 * </pre>
 * </code>
 * <p>
 * Where <code>name</code> corresponds to the argument name, preceded by
 * any number of dashes, and <code>value</code> is the new
 * <code>true/false</code> values for this argument.
 * <p>
 * For the arguments that just specify a name, this is short hand for
 * <code>name=true</code>.
 * <p>
 * For arguments that instead specify the value, they must be one of:
 * <code>name=true</code> or <code>name=false</code>.
 * <p>
 * All assignments that do take place will be in lowercase form. As of
 * <code>v1.0</code> there is no support for case-sensitive argument
 * assignments.
 *
 * @author Obicere
 * @version 1.0
 */
public class ArgumentParser {

    private final HashMap<String, Argument> arguments = new HashMap<>();

    private final String[] args;

    private boolean finalized;

    /**
     * Constructs a new argument parser that functions on the given
     * arguments. The arguments are finalized: so once parsed, they cannot
     * be reassigned.
     *
     * @param args The arguments to parse.
     */

    public ArgumentParser(final String... args) {
        this(true, args);
    }

    /**
     * Constructs a new argument parser that functions on the given
     * arguments. This has conditional finalization, which if
     * <code>true</code>: upon parsing an argument, the argument is removed
     * from the set of possible arguments.
     *
     * @param finalized Whether or not the arguments are finalized.
     * @param args      The arguments to parse.
     */

    public ArgumentParser(final boolean finalized, final String... args) {
        this.finalized = finalized;
        this.args = args;
    }

    /**
     * Provides a new argument to the set. Any argument with the same name
     * will be removed and ignored here. Also, the later arguments are
     * given higher precedence in the case of a collision. So adding two
     * arguments with the same name, the first will be discarded and the
     * second one will take its place.
     *
     * @param argument The argument to add to the set.
     */

    public void provide(final Argument argument) {
        Objects.requireNonNull(argument);
        arguments.put(argument.getName(), argument);
    }

    /**
     * Retrieves the set of arguments this parser is functioning on. These
     * should be supplied by the <code>main</code> method, or its
     * equivalent.
     *
     * @return The list of arguments this parser is functioning on.
     */

    public String[] getArguments() {
        return args;
    }

    /**
     * Parses the arguments, this should be called after all the
     * corresponding arguments are added. This can however be called
     * multiple times; however, this is considered unconventional, as this
     * is a fairly costly operation and any arguments that should be parsed
     * should have already been added.
     * <p>
     * The format of the argument handling is as follows:
     * <p>
     * If the argument is not conditional, then it can accept the following
     * formats: <code>
     * <pre>
     * name=value
     * -name=value
     * --name=value
     * ...
     * name value
     * -name value
     * --name value
     * ...
     * </pre>
     * </code>
     * <p>
     * Where <code>name</code> corresponds to the argument name, preceded
     * by any number of dashes, and <code>value</code> is the new value for
     * this argument.
     * <p>
     * For the <code>name value</code> arguments, if there is no
     * <code>value</code> argument, no assignment will take place.
     * <p>
     * If the argument is conditional, then then it can accept the
     * following formats: <code>
     * <pre>
     * name
     * -name
     * --name
     * ...
     * name=value
     * -name=value
     * --name=value
     * ...
     * </pre>
     * </code>
     * <p>
     * Where <code>name</code> corresponds to the argument name, preceded
     * by any number of dashes, and <code>value</code> is the new
     * <code>true/false</code> values for this argument.
     * <p>
     * For the arguments that just specify a name, this is short hand for
     * <code>name=true</code>.
     * <p>
     * For arguments that instead specify the value, they must be one of:
     * <code>name=true</code> or <code>name=false</code>.
     * <p>
     * This will respect the contract on the finalized requirement. So if
     * the parser is set to a finalized mode, then the arguments cannot be
     * reassigned.
     * <p>
     * So given an argument named <code>"foo"</code>, and the arguments:
     * <code>["foo=bar", "foo", "car"]</code> the following will happen:
     * <p>
     * If this parser is finalized:
     * <p>
     * <code>foo</code> will be set to <code>"bar"</code>, <code>foo</code>
     * will be removed from the set, and then <code>"car"</code> value will
     * be ignored.
     * <p>
     * If this parser is not finalized:
     * <p>
     * Initially <code>foo</code> will be set to <code>"bar"</code>, but
     * then the next assignment will be reached and <code>foo</code> will
     * then be set to <code>"car"</code>.
     * <p>
     * Note however, that the difference in finalization can result in
     * different results.
     * <p>
     * Consider we have two arguments:
     * <p>
     * <pre>
     * <code>foo</code>, which is non-conditional. Default value: "test"
     * <code>bar</code>, which is conditional. Default value: false
     * </pre>
     * <p>
     * Then, parsing the following:
     * <p>
     * <code>["foo", "set", "foo", "bar"]</code>
     * <p>
     * In the case where the parse is finalized:
     * <p>
     * <code>foo</code> is set to <code>"set"</code>. <code>foo</code> is
     * then removed. <code>"foo"</code> then doesn't match an argument and
     * is ignored. <code>"bar"</code> is parsed and matched to the
     * conditional argument.
     * <p>
     * Which results in:
     * <p>
     * <pre>
     * <code>foo = set</code>
     * <code>bar = true</code>
     * </pre>
     * <p>
     * However is parsing is not finalized:
     * <p>
     * <code>foo</code> is set to <code>"set"</code>. <code>foo</code> is
     * <b>not</b> removed. <code>foo</code> is set to <code>"bar</code>.
     * <p>
     * Which results in:
     * <p>
     * <pre>
     * <code>foo = bar</code>
     * <code>bar = false</code>
     * </pre>
     * <p>
     * This change is noticeable and as of <code>v1.0</code> persists.
     */

    public void parse() {
        if (args == null || args.length == 0 || isEmpty()) {
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
            argumentProcessed(name);

            final int assignmentIndex = name.indexOf('=');
            if (assignmentIndex > 0) {
                // We have an assignment
                if (assignmentIndex + 1 == name.length()) { // No value available
                    argument.set(null);
                    continue;
                }
                name = name.substring(0, assignmentIndex);
                final String assignment = name.substring(assignmentIndex + 1); // +1 to avoid the =
                if (argument.isConditional()) {

                    // check to ensure the assignment is accepted for conditional values
                    if (assignment.equalsIgnoreCase("true") || assignment.equalsIgnoreCase("false")) {
                        argument.set(assignment);
                    }
                } else {
                    argument.set(assignment);
                }
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

            if (isEmpty()) { // no more arguments can be performed
                return;
            }
        }
    }

    /**
     * Checks to see if the argument parser's set of arguments is empty.
     * This can happen if no arguments have been added, or every argument
     * that was added was matched and the parser is finalized.
     *
     * @return <code>true</code> if and only if the arguments are empty.
     */

    public boolean isEmpty() {
        return arguments.isEmpty();
    }

    /**
     * Retrieves the given argument by name. If the parser is set to
     * finalized, then this will not retrieve arguments that were
     * processed. Therefore, arguments that were added by a specific name
     * might not be accessible later by that name.
     *
     * @param name The name of the argument to retrieve.
     * @return The argument, if it exists. Otherwise <code>null</code>.
     */

    public Argument getArgument(final String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Supplied argument name is invalid.");
        }
        return argumentFor(name);
    }

    /**
     * Retrieves the given argument by name. If the parser is set to
     * finalized, then this will not retrieve arguments that were
     * processed. Therefore, arguments that were added by a specific name
     * might not be accessible later by that name.
     * <p>
     * The <code>name</code> should be a properly valid argument name. It
     * should be not equal to <code>null</code> and should not be empty.
     *
     * @param name The name of the argument to retrieve.
     * @return The argument, if it exists. Otherwise <code>null</code>.
     */

    protected Argument argumentFor(final String name) {
        return arguments.get(name);
    }

    /**
     * Notifies this argument - by name - as processed. Effectively, this
     * will remove the argument from the set of arguments as of
     * <code>v1.0</code> if the parser if finalized.
     *
     * @param name The argument - by name - to notify.
     */

    protected void argumentProcessed(final String name) {
        if (finalized) {
            arguments.remove(name);
        }
    }

}
