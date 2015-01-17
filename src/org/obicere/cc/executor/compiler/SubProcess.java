package org.obicere.cc.executor.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Used for running an additional program along with this program. The only
 * use for this in the current build is to run a compiler.
 * <p>
 * This works by creating a new {@link java.lang.ProcessBuilder} under the
 * default working directory. If the given program requires a non-default
 * directory, the command itself must issue it. Should such a directory not
 * be supported, the user can specify the location by adding the directory
 * to the "PATH" system variable.
 */

public class SubProcess {

    private static final int MAX_MESSAGE_LENGTH = 100;

    /**
     * Starts a process on a given command and feeds the console output to
     * a collector. There is an enforced limit of 100 lines that can be
     * received from the process. If this cap is met, then a next line will
     * follow containing the string <code>"..."</code> to signify the
     * continuation.
     * <p>
     * After all the contents have been read from the stream, it is closed
     * and the process is destroyed to free the system.
     * <p>
     * The given {@link java.lang.String} should be trimmed to ensure empty
     * strings are not accidentally passed as arguments.
     *
     * @param command The command to run and to monitor the output of.
     * @return The list of lines from the output from the process.
     * @throws IOException Should there be any issues with creating the
     *                     process or reading the output from the process.
     */

    public static String[] run(final String command) throws IOException {
        final Process proc = createProcess(command);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line;
        final List<String> list = new ArrayList<>(MAX_MESSAGE_LENGTH);

        int count = 0;
        while ((line = reader.readLine()) != null) {
            if (++count > MAX_MESSAGE_LENGTH) { // Checks to ensure contract is met.
                list.add("...");
                break;
            }
            list.add(line);
        }
        reader.close();
        proc.destroy();
        return list.toArray(new String[list.size()]);
    }

    /**
     * Starts the process by splitting the command into individual
     * parameters. This is done by splitting on whitespace not contained
     * within double quotation marks. One this is done, a {@link
     * java.lang.ProcessBuilder} is used to start the process.
     * <p>
     * The error stream is redirected to the current program to make the
     * viewing of errors easier for the developer. However, when it comes
     * to compilers most will not utilize the error stream - so this is
     * merely a commodity.
     * <p>
     * The strings being fed into this method should also have the proper
     * syntax.
     * <p>
     * For example, given the string <code>cd C:/Program Files/</code>,
     * with proper syntax there should be quotes: <code>cd "C:/Program
     * Files/"</code>.
     * <p>
     * The two commands will parse respectively as:
     * <pre>
     * <code>["cd", "C:/Program", "Files/"]</code>
     * <code>["cd", "C:/Program Files/"]</code>
     * </pre>
     * <p>
     * Since the command most likely refers to the Windows directory, the
     * second would be favored. Due to this, properly wrapping commands
     * with double quotes is needed. This method will contract to that
     * design so as to not improperly parse commands.
     *
     * @param command The command to parse and start a process on.
     * @return The started process, if available.
     * @throws IOException If the process failed to start or parse
     *                     correctly.
     */

    private static Process createProcess(final String command) throws IOException {
        final String[] split = command.split(" +(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // This is so unsafe right now
        final ProcessBuilder builder = new ProcessBuilder(split);
        builder.redirectErrorStream(true);
        return builder.start();
    }
}
