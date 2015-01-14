package org.obicere.cc.configuration;

import org.obicere.cc.process.StartingProcess;

import java.io.File;
import java.util.logging.Level;

/**
 * @author Obicere
 */
public class Paths extends StartingProcess {

    public static final String APP_DATA = Configuration.getAppData();
    public static final String HOME     = APP_DATA + File.separator + "ObicereCC";
    public static final String SOURCES  = HOME + File.separator + "sources";
    public static final String DATA     = HOME + File.separator + "data";
    public static final String LANGUAGE = DATA + File.separator + "language";

    public static final String[] PATHS = new String[]{APP_DATA, HOME, SOURCES, DATA, LANGUAGE};

    public Paths(final Domain access) {
        super(access);
    }

    @Override
    public int priority() {
        return 0;
    }

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
