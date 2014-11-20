package org.obicere.cc.configuration;

import java.io.File;
import java.util.logging.Level;

/**
 * @author Obicere
 */
public class Paths extends DomainAccess {

    public static final String APP_DATA = Global.getAppData();
    public static final String HOME     = APP_DATA + File.separator + "ObicereCC";
    public static final String SOURCES  = HOME + File.separator + "sources";
    public static final String DATA     = HOME + File.separator + "data";
    public static final String LANGUAGE = DATA + File.separator + "language";

    public static final String[] PATHS = new String[]{APP_DATA, HOME, SOURCES, DATA, LANGUAGE};

    public Paths(final Domain access) {
        super(access);
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
