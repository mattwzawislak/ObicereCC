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

package org.obicere.cc.tasks.projects;

import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.gui.projects.ProjectPanel;
import org.obicere.cc.gui.projects.ProjectSelector;
import org.obicere.cc.methods.CustomClassLoader;
import org.obicere.cc.methods.IOUtils;
import org.obicere.cc.methods.XMLParser;
import org.xml.sax.SAXException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 * Used for handling project attributes and other data
 *
 * @author Obicere
 * @version 1.0
 */

public class Project {

    public static final String[] DIFFICULTY = new String[]{"Beginner", "Intermediate", "Advanced", "Challenging", "Legendary"};
    public static final LinkedList<Project> DATA = new LinkedList<>();

    private final String name;
    private final File file;
    private final Properties properties;
    private final Class<?> runner;
    private boolean complete;

    /**
     * Constructs a new Project and reads file data
     *
     * @param name       Name of the Project
     * @param runnerFile The file to read data from
     */

    public Project(final String name, final File runnerFile) throws IOException, SAXException {
        final File data = new File(Paths.SETTINGS + File.separator + "data.dat");
        final String in = new String(IOUtils.readData(data));
        final XMLParser parser = XMLParser.getInstance();
        final File xml = new File(Paths.SOURCE, name + "Runner.xml");
        parser.prepare(xml);

        this.properties = new Properties(parser.getAttributeMapping());
        this.name = name;
        this.complete = (in.contains(String.format("|%040x|", new BigInteger(name.getBytes()))));
        this.file = new File(Paths.JAVA + File.separator + name + ".java");
        this.runner = CustomClassLoader.loadClassFromFile(runnerFile);
    }

    /**
     * This is used for loading dynamic files, including compiled files and soon to be XML/CSS files
     *
     * @return The name of the Runner
     */

    public String getName() {
        return name;
    }

    /**
     * Used to sort and distribute difficulties
     *
     * @return The category that this Runner falls into
     */

    public String getSortName() {
        return properties.getCategory() + getName();
    }


    public Properties getProperties() {
        return properties;
    }

    /**
     * @see Project#getName()
     */

    @Override
    public String toString() {
        return getName();
    }


    /**
     * This is used only during loading to get the saved code if any
     *
     * @return formatted String containing user's code
     */

    public String getCurrentCode() {
        try {
            if (file.exists()) {
                final byte[] data = IOUtils.readData(file);
                return new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.getSkeleton();
    }


    /**
     * Returns the file of the runner. This is used nicely to read data
     *
     * @return The file type to read and store data
     */

    public File getFile() {
        return file;
    }

    /**
     * Returns the runner class for reflection to run the user-provided code
     *
     * @return the Class instance of the loaded Runner
     */

    public Class<?> getRunner() {
        return runner;
    }

    /**
     * Hashing for adding to a <tt>HashMap</tt> without overriding version sets
     *
     * @return hash code based off the name, file and instructions.
     */

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + file.hashCode() * 17;
    }

    /**
     * Used to filter complete projects and displaying detailed information about the runner
     *
     * @return whether or not the project is complete
     */

    public boolean isComplete() {
        return complete;
    }

    /**
     * Used only internally during execution of code.
     *
     * @param complete <tt>boolean</tt> representing if the project was 100% successful and complete.
     */

    public void setComplete(boolean complete) {
        this.complete = complete;
        for (final ProjectPanel panel : ProjectSelector.getProjectList()) {
            if (panel.getProject().equals(this)) {
                panel.setComplete(complete);
                return;
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Project && o.hashCode() == this.hashCode();
    }

    /**
     * Saves the code so the user can resume the project at a later time
     * <p/>
     * Compilation is not required, so non-working code can be accepted.
     *
     * @param code The <tt>String</tt> of code to save
     * @return <tt>true</tt> if it saved correctly.
     */

    public boolean save(final String code) {
        try {
            IOUtils.write(getFile(), code.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This does a sweep of the source folder to get all 5 categories and
     * respective Runners inside of them. This is should only be called to check
     * for currently loaded Runners, then to add all the updated versions of
     * Runners, if any. The key for the HashMap is the category, with the value
     * being a list of runners.
     *
     * @since 1.0
     */

    public static void loadCurrent() {
        DATA.clear();
        final File root = new File(Paths.SOURCE);
        if (!root.exists()) {
            return;
        }
        final String[] list = root.list();
        for (final String name : list) {
            if (name != null) {
                final File file = new File(root, name);
                final int idx = name.indexOf("Runner.class");
                if (idx == -1) {
                    continue;
                }
                try {
                    DATA.add(new Project(name.substring(0, idx), file));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}