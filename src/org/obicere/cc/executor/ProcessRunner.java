package org.obicere.cc.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * org.obicere.cc.executor
 * Created: 2/4/14 3:03 PM
 *
 * @author Obicere
 * @version 1.0
 */
public class ProcessRunner {

    private static Process runProcess(final String command) throws IOException {
        final String[] split = command.split(" +(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        System.out.println(Arrays.toString(split));
        final ProcessBuilder builder = new ProcessBuilder(split);
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
        }
        reader.close();
        proc.destroy();
        return list.toArray(new String[list.size()]);
    }

}
