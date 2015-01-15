package org.obicere.cc.executor.compiler;

import org.obicere.cc.configuration.Configuration;

import java.io.IOException;

/**
 * Stores the command-line information for testing a specific compiler. One
 * such example would be with the Java Compiler:
 * <p>
 * <pre>
 * <code> new Command("javac", "${exec} -g -nowarn \"${file}\"") </code>
 * </pre>
 * <p>
 * Is the code used to test for the java compiler's validity on the
 * computer. When {@link #check()} is called, say on a Windows machine:
 * <p>
 * <pre>
 *     1. The program name <code>"javac"</code> is retrieved.
 *     2. {@link org.obicere.cc.configuration.Configuration#getOS()} is
 *          called.
 *     3. Based on the OS - which should be {@link org.obicere.cc.configuration.Configuration.OS#WINDOWS}
 *          in this case, the command is generated.
 *     4. In this case the generated command would be <code>where
 *          javac</code>
 *     5. A {@link org.obicere.cc.executor.compiler.SubProcess} runs the
 *          generated command. Then, the returned messages - if one exists
 *          - is compared to the command-based error that gets thrown if
 *          the program ("javac") does not exist.
 * </pre>
 * <p>
 * Note that this is merely a guideline and it is very hard to accommodate
 * for the number of different operating systems and builds.
 * <p>
 * With that in mind, help with covering multiple systems along with the
 * {@link org.obicere.cc.configuration.Configuration.OS} data is always
 * greatly appreciated.
 *
 * @author Obicere
 * @version 1.0
 */

public class Command {

    private final String program;
    private final String format;

    public Command(final String program, final String format) {
        this.program = program;
        this.format = format;
    }

    /**
     * Retrieves the {@link java.lang.String} format to be used when
     * running a command - which should be a compiler - on a specific file.
     * The way this should be processed is through the {@link
     * String#format(String, Object...)} method along with the program name
     * from this instance.
     * <p>
     * As a security concern, no checks are made that a command that - for
     * example deletes System32 - is called. Always check any files you
     * receive meant to run with this program and NEVER use them if they
     * are NOT open sourced and secured properly. SHA checks help.
     *
     * @return The format to run the given compiler on a specified file.
     * @see #getProgram()
     */

    public String getFormat() {
        return format;
    }

    /**
     * The name of the compiler. This is stored separately from the initial
     * command such that multiple compilers can be used and the first
     * available one will be lazily set.
     *
     * @return The program specific name.
     * @see #check()
     */

    public String getProgram() {
        return program;
    }

    /**
     * Checks to see if the given program exists on the computer and is
     * accessible through a command-line. The exact process described at
     * the top of this file is performed here.
     * <p>
     * This has as of v1.0 only been tested on a Windows machine and
     * loosely tested on a Mac.
     * <p>
     * Linux has not been tested at all and at this point, will always
     * return <code>true</code>. This is so that a user on a Linux may
     * still be able to use the program if possible - but not with the same
     * proficiency as other operating systems.
     *
     * @return <code>true</code> if the program will most-likely work.
     * Otherwise <code>false</code>.
     */

    public boolean check() {
        try {
            final String command;
            final String failure;
            switch (Configuration.getOS()) {
                case WINDOWS:
                    command = "where " + program;
                    failure = "INFO: Could not find files for the given pattern(s).";
                    break;
                case MAC:
                    command = "command " + program;
                    failure = "-bash: "; // Needs testing
                    break;
                case LINUX:
                    // Don't have a Linux machine to test.
                    // assume that it worked so the program will
                    // still function.
                    command = "whereis " + program;
                    failure = program + ": "; // TODO: Fix this.
                    return true;
                default:
                    return false;
            }
            final String[] str = SubProcess.run(command);
            return str.length != 0 && !str[0].startsWith(failure);
        } catch (final IOException e) {

            e.printStackTrace();
        }
        return false;
    }

}
