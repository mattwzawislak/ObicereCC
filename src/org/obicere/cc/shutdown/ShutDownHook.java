package org.obicere.cc.shutdown;

import org.obicere.cc.configuration.Global;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Properties;

public abstract class ShutDownHook extends Thread {

    public static final int PRIORITY_WINDOW_CLOSING = 0x0;
    public static final int PRIORITY_RUNTIME_SHUTDOWN = 0x1;

    private final int priority;

    private final Properties properties = new Properties();

    public ShutDownHook(final String name, final int priority) {
        super(name);
        this.priority = priority;
        loadProperties();
    }

    protected void loadProperties() {
        final File file = new File(Global.Paths.DATA, getName() + ".properties");
        try {
            if (!file.exists() && !file.createNewFile()) {
                return;
            }
            final InputStream input = new FileInputStream(file);
            if (file.canRead()) {
                properties.load(input);
            }
            final Field[] fields = getClass().getDeclaredFields();
            for (final Field field : fields) {
                try {
                    if (field.isAnnotationPresent(HookValue.class)) {
                        final HookValue annotation = field.getAnnotation(HookValue.class);
                        final String name = (String) field.get(this);
                        final String value = properties.getProperty(name);
                        if (value == null) {
                            properties.setProperty(name, annotation.value());
                        }
                    }
                } catch (final IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public int getHookPriority() {
        return priority;
    }

    public void setProperty(final String key, final Object value) {
        properties.setProperty(key, String.valueOf(value));
    }

    @Override
    public void run() {
        final File file = new File(Global.Paths.DATA, getName() + ".properties");
        if (file.exists() && !file.canWrite()) {
            return;
        }
        try {
            final FileOutputStream stream = new FileOutputStream(file);
            properties.store(stream, null);
            stream.flush();
            stream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public int getPropertyAsInt(final String name) {
        return Integer.parseInt(properties.getProperty(name));
    }

    public double getPropertyAsDouble(final String name) {
        return Double.parseDouble(properties.getProperty(name));
    }

    public String getPropertyAsString(final String name) {
        return String.valueOf(properties.get(name));
    }

    public boolean getPropertyAsBoolean(final String name) {
        return Boolean.valueOf(properties.getProperty(name));
    }

}
