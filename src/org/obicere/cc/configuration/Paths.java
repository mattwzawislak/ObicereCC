package org.obicere.cc.configuration;

import org.obicere.cc.process.StartingProcess;

import java.io.File;
import java.util.logging.Level;

/**
 * Contains the set of dependent paths the program will use. These are used
 * globally and provided through the {@link org.obicere.cc.configuration.Domain}
 * as a {@link org.obicere.cc.process.StartingProcess}. During the startup
 * process the folder-based paths will be created. Should they fail, an
 * error will be displayed in the console notifying which folder failed to
 * be created.
 * <p>
 * This also contains default site information. This way if the site
 * registration changes, this can be reflected application-wide.
 * <p>
 * The resource images are also stored here. Notably the
 * <code>close.png</code>, <code>complete.png</code>, and
 * <code>icon.png</code> image locations.
 * <p>
 * As of v1.0 no support has been added for modification-based custom
 * folders. This would have to be handled by the mod itself.
 *
 * @author Obicere
 * @version 1.0
 */
public class Paths extends StartingProcess {

    // Site
    public static final String SITE_HOME = "http://www.obicere.uni.me";
    public static final String SITE_BIN  = SITE_HOME + "/ccbin/";

    // Working directory/jar
    public static final String RESOURCES_HOME     = "resource/";
    public static final String RESOURCES_CLOSE    = RESOURCES_HOME + "close.png";
    public static final String RESOURCES_COMPLETE = RESOURCES_HOME + "complete.png";
    public static final String RESOURCES_ICON     = RESOURCES_HOME + "icon.png";

    // System folders
    public static final String APP_DATA        = Configuration.getAppData();
    public static final String FOLDER_HOME     = APP_DATA + File.separator + "ObicereCC";
    public static final String FOLDER_SOURCES  = FOLDER_HOME + File.separator + "sources";
    public static final String FOLDER_DATA     = FOLDER_HOME + File.separator + "data";
    public static final String FOLDER_LANGUAGE = FOLDER_DATA + File.separator + "language";

    // List of system folders to create and the order in which they should be created.
    public static final String[] PATHS = new String[]{APP_DATA, FOLDER_HOME, FOLDER_SOURCES, FOLDER_DATA, FOLDER_LANGUAGE};

    public Paths(final Domain access) {
        super(access);
    }

    /**
     * The first startup task with priority <code>0</code>. This is to
     * ensure that any other processes are able to create any files needed.
     * One such example would be the {@link org.obicere.cc.util.Updater}.
     * Should the paths not be instantiated, the updater would not be able
     * to download the required files upon first startup.
     *
     * @return The priority of this task: <code>0</code>.
     */

    @Override
    public int priority() {
        return 0;
    }

    /**
     * Attempts to create all the folders in specified in the system paths.
     * This list is not checked for order and {@link java.io.File#mkdirs()}
     * is not called, in the case that there are base folders that are
     * invalid. Should this happen, the user would most likely not want a
     * large mass of folders potentially being created in the wrong
     * directory.
     * <p>
     * In the case such an error happens - or another error for that
     * matter, the failed directory's location will be printed to the
     * console.
     */

    @Override
    public void run() {
        for (final String s : PATHS) {
            final File file = new File(s);
            if (!file.exists() && !file.mkdir()) {
                log.log(Level.WARNING, "Failed to create folder {0}.", file);
            }
        }
    }
}
