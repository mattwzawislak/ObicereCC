package org.obicere.cc.shutdown;

import org.obicere.cc.configuration.Global;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Obicere
 * @since 1.0
 */

public class SaveLayoutHook extends ShutDownHook {

    public static final String NAME = "save.layout";
    private static final File SAVE_FILE = new File(Global.Paths.LAYOUT_SAVE_FILE);


    private Properties properties;

    public SaveLayoutHook(final boolean conditional, final String purpose) {
        super(conditional, purpose, NAME, PRIORITY_WINDOW_CLOSING);
    }

    @Override
    public void run() {
        try {
            if (properties == null) {
                return;
            }
            if (!SAVE_FILE.exists() && !SAVE_FILE.createNewFile()) {
                return;
            }
            final FileOutputStream stream = new FileOutputStream(SAVE_FILE);
            properties.store(stream, null);
            stream.flush();
            stream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void provideProperties(final Properties properties) {
        this.properties = properties;
    }

}
