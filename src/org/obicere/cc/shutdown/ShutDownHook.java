package org.obicere.cc.shutdown;

import org.obicere.cc.configuration.Global;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

public abstract class ShutDownHook extends Thread {

    private static final String DYNAMIC_MATCHER_BOOLEAN     = "true|false";
    private static final String DYNAMIC_MATCHER_OCTAL       = "[+-]?0[0-7]{1,11}";
    private static final String DYNAMIC_MATCHER_DECIMAL     = "[+-]?[0-9]{1,10}";
    private static final String DYNAMIC_MATCHER_HEXADECIMAL = "[+-]?0x[0-9a-fA-F]{1,9}";
    private static final String DYNAMIC_MATCHER_DOUBLE      = "[+-]?([0-9]{1,10})((\\.)?[0-9]{0,10}?)([eE][+-]?[0-9]{1,10})?";
    private static final String DYNAMIC_MATCHER_COLOR       = "java\\.awt\\.Color\\[r=([0-9]{1,3}),g=([0-9]{1,3}),b=([0-9]{1,3})\\]";

    public static final int PRIORITY_WINDOW_CLOSING   = 0x0;
    public static final int PRIORITY_RUNTIME_SHUTDOWN = 0x1;

    private final int priority;

    private final File propertiesFile;
    private final Properties properties = new Properties();

    public ShutDownHook(final String name, final int priority) {
        super(name);
        this.priority = priority;
        this.propertiesFile = new File(Global.Paths.DATA, name.concat(".properties"));
        loadProperties();
    }

    protected void loadProperties() {
        try {
            Global.readProperties(properties, propertiesFile);
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
        try {
            Global.writeProperties(properties, propertiesFile);
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
        return String.valueOf(properties.getProperty(name));
    }

    public boolean getPropertyAsBoolean(final String name) {
        return Boolean.valueOf(properties.getProperty(name));
    }

    public Color getPropertyAsColor(final String name) {
        final String content = getPropertyAsString(name);
        return parseColor(content);
    }

    public Object getDynamicObject(final String name) {
        final String value = getPropertyAsString(name);
        if (value.matches(DYNAMIC_MATCHER_BOOLEAN)) {
            return Boolean.valueOf(value);
        }
        if (value.matches(DYNAMIC_MATCHER_OCTAL)) {
            return Integer.parseInt(value, 8);
        }
        if (value.matches(DYNAMIC_MATCHER_DECIMAL)) {
            return Integer.parseInt(value, 10);
        }
        if (value.matches(DYNAMIC_MATCHER_HEXADECIMAL)) {
            return Integer.parseInt(value, 16);
        }
        if (value.matches(DYNAMIC_MATCHER_DOUBLE)) {
            return Double.parseDouble(value);
        }
        if (value.matches(DYNAMIC_MATCHER_COLOR)) {
            return parseColor(value);
        }
        // By default, just return a string
        return value;
    }

    private Color parseColor(final String value) {
        final String parse = value.replaceAll(DYNAMIC_MATCHER_COLOR, "$1;$2;$3");
        final String[] tokens = parse.split(";");

        final int r = Integer.parseInt(tokens[0]);
        final int g = Integer.parseInt(tokens[1]);
        final int b = Integer.parseInt(tokens[2]);
        return new Color(r, g, b);
    }

}
