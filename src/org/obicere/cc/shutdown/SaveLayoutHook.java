package org.obicere.cc.shutdown;

import org.obicere.cc.configuration.Global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SaveLayoutHook extends ShutDownHook {

    public static final String NAME = "save.layout";
    public static final String PROPERTY_FRAME_WIDTH = "frame.width";
    public static final String PROPERTY_FRAME_HEIGHT = "frame.height";
    public static final String PROPERTY_FRAME_STATE = "frame.state";
    public static final String PROPERTY_MAINSPLIT_DIVIDER_LOCATION = "mainsplit.divider.location";
    public static final String PROPERTY_TEXTSPLIT_DIVIDER_LOCATION = "textsplit.divider.location";

    private static final File SAVE_FILE = new File(Global.Paths.LAYOUT_SAVE_FILE);
    private final Properties properties = new Properties();

    public SaveLayoutHook() {
        super(true, "Save Layout", NAME, PRIORITY_WINDOW_CLOSING);
        final File file = new File(Global.Paths.LAYOUT_SAVE_FILE);

        try {
            if (file.exists()) {
                properties.load(new FileInputStream(file));
            } else if(!file.createNewFile()){
                System.err.println("Failed to create settings file");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
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

    public void saveProperty(final String property, final Object value){
        properties.setProperty(property, value.toString());
    }

    public Object getProperty(final String property, final String defaultValue){
        return properties.getProperty(property, defaultValue);
    }

}
