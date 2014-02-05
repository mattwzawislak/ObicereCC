package org.obicere.cc.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * org.obicere.cc.executor
 * Created: 2/4/14 3:03 PM
 *
 * @author Obicere
 * @version 1.0
 */
public class ProcessRunner {

    private static final Runtime RUNTIME = Runtime.getRuntime();

    private static Process runProcess(final String command) throws IOException {
        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        return builder.start();
    }

    public static String[] run(final String command) throws IOException {
        final Process proc = runProcess(command);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line;
        final LinkedList<String> list = new LinkedList<>();

        while ((line = reader.readLine()) != null) {
            list.add(line);
            try {
                if (proc.exitValue() == 0) {
                    break;
                }
            } catch (final IllegalThreadStateException t) {
                proc.destroy();
            }
        }
        return list.toArray(new String[list.size()]);
    }

}
