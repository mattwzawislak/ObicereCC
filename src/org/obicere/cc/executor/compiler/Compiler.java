package org.obicere.cc.executor.compiler;

import org.obicere.cc.executor.Result;
import org.obicere.cc.gui.GUI;
import org.obicere.cc.gui.projects.Editor;
import org.obicere.cc.tasks.projects.Project;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author Obicere
 */
public abstract class Compiler {

    public abstract boolean isInstalled();

    public abstract String getName();

    public abstract String prepareCommand(final Project project, final File file);

    public abstract Result[] runAndGetResults(final Project project);

    public final boolean compile(final Project project, final File file){
        try {
            final Editor editor = GUI.tabByName(project.getName());
            final String command = prepareCommand(project, file);
            final Process p = Runtime.getRuntime().exec(command, null, null);
            p.waitFor();
            final BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            final StringBuilder replacement = new StringBuilder();
            while ((line = error.readLine()) != null) {
                replacement.append(line);
                replacement.append('\n');
            }
            if (replacement.length() != 0) {
                editor.setInstructionsText(replacement.toString(), true);
                return false;
            }
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
