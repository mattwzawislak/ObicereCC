/*
This file is part of ObicereCC.

ObicereCC is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ObicereCC is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ObicereCC.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.obicere.cc.executor;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.gui.GUI;
import org.obicere.cc.gui.projects.Editor;
import org.obicere.cc.methods.CustomClassLoader;
import org.obicere.cc.tasks.projects.Project;
import org.obicere.cc.tasks.projects.Runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Source code compiler and executor. This is used internally during the run of
 * the input code.
 *
 * Lots of magic happens here. Do not touch anything.
 *
 * @author Obicere
 * @since 1.0
 */

public class Executor {

    public static boolean hasJDKInstalled() {
        try {
            final Process r = Runtime.getRuntime().exec("javac -version");
            final BufferedReader read = new BufferedReader(new InputStreamReader(r.getErrorStream()));
            return !read.readLine().startsWith("'javac' is not recognized");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Runs the compiled code from the {@link Executor#compileClass(Project)
     * compileClass(Project)} method. This returns a list of <tt>Result</tt>s
     * dependent on the users input and the provided answer set.
     *
     * @param project The specific project to compile and run to provide results
     *                for.
     * @return An array of type {@link Result Result}.
     * @see Executor#compileClass(Project)
     * @since 1.0
     */

    public static Result[] runAndGetResults(final Project project) {
        if (project != null && compileClass(project)) {
            try {
                final String fileName = Global.Paths.JAVA + File.separator + project.getName() + ".class";
                final Class<?> ref = project.getRunner();
                final Class<?> compiled = CustomClassLoader.loadClassFromFile(fileName);
                final Runner runner = (Runner) ref.newInstance();
                return runner.getResults(compiled);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * This will use the located JDK directory during initialization and compile
     * the code provided by the user. This file is then handed off to the
     * {@link Executor#runAndGetResults(Project) runAndGetResults(Project)}
     * method where it is processed. This method will only return true should
     * the code be successfully compiled.
     *
     * @param project The project source you wish to compile, as provided by the
     *                user.
     * @return <tt>true</tt> should the class successfully compile and write to
     *         a file.
     * @see Executor#runAndGetResults(Project)
     * @since 1.0
     */

    private static boolean compileClass(Project project) {
        try {
            final Editor editor = GUI.tabByName(project.getName());
            final String command = "javac -g  -d " + project.getFile().getParent() + " " + project.getFile().getPath();
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
