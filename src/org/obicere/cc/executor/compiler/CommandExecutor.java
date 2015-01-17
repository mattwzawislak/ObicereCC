package org.obicere.cc.executor.compiler;

import org.obicere.cc.util.StringSubstitute;

import java.io.File;

/**
 * Stores the set of commands that are supported for attempting to compile
 * a source file. At first run, the process will attempt to lazily set a
 * working command. A working command is defined as a command that exists.
 * No sure-fire way can be set for all commands to see if compilation
 * actually works pre-compilation. Therefore should an issue arise, there
 * is currently no support for testing a different command if one already
 * passed the initial test as a working command.
 * <p>
 * The executor will also contain the source and compiled extensions for a
 * given language. This can be used to easily support the resulting file.
 * As example with a Java class <code>Foo</code>:
 * <pre>
 *     Source File:   <code>Foo.java</code>  -> <code>Foo{$sext}</code>
 *     Compiled File: <code>Foo.class</code> -> <code>Foo{$cext}</code>
 * </pre>
 * Where <code>{$sext}</code> and <code>{$cext}</code> and the source
 * extensions and compiled extensions respectively.
 *
 * @author Obicere
 * @version 1.0
 */

public class CommandExecutor {

    private static final String ERROR_NO_PROCESS = "Could not find process. Make sure your path is set correctly.";

    private final String sourceExt;
    private final String compiledExt;

    private final Command[] commands;

    private volatile Command workingCommand;

    /**
     * Constructs a new executor that work on the following commands, and
     * utilize the given source and compiled extensions.
     *
     * @param commands    Set of commands to test for a working command.
     * @param sourceExt   The source extension for files to compile.
     * @param compiledExt The compiled extension for files already
     *                    compiled.
     */

    public CommandExecutor(final Command[] commands, final String sourceExt, final String compiledExt) {
        this.commands = commands;
        this.sourceExt = sourceExt;
        this.compiledExt = compiledExt;
    }

    /**
     * Lazily sets the working compiler command if none was already set. If
     * no such command can be found <code>null</code> will be returned. In
     * the case there is already a command set as 'working', then this
     * command will be returned.
     *
     * @return The working command if one is found, otherwise
     * <code>null</code>.
     */

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

    /**
     * Attempts to compile the given program with the given program
     * arguments.
     * <p>
     * This will attempt to generate a command then run this command. Any
     * errors that are detected will be returned. Only internal errors will
     * not be returned, but instead will be printed to the console.
     * <p>
     * Should there be no working command, {@link #ERROR_NO_PROCESS} will
     * instead be returned.
     *
     * @param file    The file to attempt to compile.
     * @param varargs The compiler arguments to use.
     * @return The result of compilation - if any.
     */

    public String[] process(final File file, final Object... varargs) {
        try {
            final String command = getCommand(file, varargs);
            if (command == null) {
                return new String[]{ERROR_NO_PROCESS};
            }
            return SubProcess.run(command);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return new String[]{"Internal Error"};
    }

    /**
     * Formats the command for compilation on the given file with
     * arguments. This will check to see if a space character is present in
     * each of the argument's {@link java.lang.String} representation.
     * Should there be a space present, the argument is automatically
     * wrapped with double-quotes unless the argument already has said
     * double quote.         q
     * <p>
     * It is suggested to use only {@link java.lang.String}s for this
     * matter, but other forms of data are also supported such as integers,
     * booleans, etc.
     * <p>
     * The default supported arguments that are automatically replaced are
     * - in order of replacement:
     * <pre>
     *     1.    <code>sext</code> - Source file extension
     *     2.    <code>cext</code> - Compiled file extension
     *     3.    <code>exec</code> - Compiler's program runnable name
     *     4.    <code>path</code> - Parent folder to compile in
     *     5.    <code>name</code> - The file name to compile
     *     6.    <code>file</code> - The absolute file path and location
     *     7. <code>varargs</code> - The arguments to use for compilation
     * </pre>
     * <p>
     * The resulting output will be trimmed to avoid trailing and leading
     * whitespace - unless a {@link java.lang.RuntimeException} occurs.
     *
     * @param file    The file to build the command for.
     * @param varargs The compiler arguments to use.
     * @return The trimmed formatted command - if any. <code>null</code> if
     * no working command was found.
     */

    public String getCommand(final File file, final Object... varargs) {
        final Command command = getCompilerCommand();
        if (command == null) {
            return null;
        }
        final StringBuilder args = new StringBuilder();
        final int length = varargs.length;
        for (int i = 0; i < length; i++) {
            if (i != 0) {
                args.append(' ');
            }
            final Object obj = varargs[i];
            final String value = String.valueOf(obj);
            if (value.contains(" ") && !value.matches("^\".*\"$")) {
                args.append('"');
                args.append(value);
                args.append('"');
                continue;
            }
            args.append(value);
        }

        final String exec = command.getFormat();
        final StringSubstitute substitute = new StringSubstitute();


        substitute.put("sext", sourceExt);
        substitute.put("cext", compiledExt);

        substitute.put("exec", command.getProgram());
        substitute.put("path", file.getParent());
        substitute.put("name", file.getName());
        substitute.put("file", file.getAbsolutePath());
        substitute.put("varargs", args.toString());

        return substitute.apply(exec).trim();
    }

}
