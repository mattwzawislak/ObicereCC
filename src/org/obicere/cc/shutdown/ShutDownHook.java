package org.obicere.cc.shutdown;

import org.obicere.cc.configuration.Global;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ShutDownHook extends Thread {

    private static final Logger log = Logger.getLogger(ShutDownHook.class.getCanonicalName());

    private static final String DYNAMIC_MATCHER_BOOLEAN     = "(?i)true|false";
    private static final String DYNAMIC_MATCHER_OCTAL       = "[+-]?0[0-7]{1,11}";
    private static final String DYNAMIC_MATCHER_DECIMAL     = "[+-]?[0-9]{1,10}";
    private static final String DYNAMIC_MATCHER_HEXADECIMAL = "[+-]?((0x)|#)([0-9a-fA-F]{1,6})"; // 3rd match group is actual hex value
    private static final String DYNAMIC_MATCHER_DOUBLE      = "[+-]?([0-9]{1,10})((\\.)?[0-9]{0,10}?)([eE][+-]?[0-9]{1,10})?";
    private static final String DYNAMIC_MATCHER_COLOR       = "([0-9]{1,3}),\\s*([0-9]{1,3}),\\s*([0-9]{1,3})";

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
            if (!propertiesFile.exists() && !propertiesFile.createNewFile()) {
                throw new AssertionError("Could not create properties file.");
            }
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

    public String getDefaultValue(final String key) {
        Objects.requireNonNull(key);
        try {
            final Field[] fields = getClass().getDeclaredFields();
            for (final Field field : fields) {
                try {
                    if (field.isAnnotationPresent(HookValue.class)) {
                        final HookValue annotation = field.getAnnotation(HookValue.class);
                        final String name = (String) field.get(this);
                        if (name.equals(key)) {
                            return annotation.value();
                        }
                    }
                } catch (final IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        throw new HookNotFoundException("No hook value found for key: " + key);
    }

    public int getHookPriority() {
        return priority;
    }

    public void setProperty(final String key, final Object value) {
        if (value instanceof Color) {
            final Color color = (Color) value;
            properties.setProperty(key, String.format("%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue()));
            return;
        }
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

    private Color parseColor(final String value) {
        final int rgb;
        if (value.matches(DYNAMIC_MATCHER_HEXADECIMAL)) {
            final String actualContent = value.replaceAll(DYNAMIC_MATCHER_HEXADECIMAL, "$3"); // 3rd match group is hex value
            rgb = Integer.parseInt(actualContent, 16);
        } else if (value.matches(DYNAMIC_MATCHER_COLOR)) {
            final String parse = value.replaceAll(DYNAMIC_MATCHER_COLOR, "$1 $2 $3");
            final String[] tokens = parse.split("\\s+");

            final int r = Integer.parseInt(tokens[0]);
            final int g = Integer.parseInt(tokens[1]);
            final int b = Integer.parseInt(tokens[2]);
            rgb = (r << 16) | (g << 8) | (b << 0);
        } else {
            log.log(Level.WARNING, "Failed to parse color code: {0}. Set to default rgb=black.", value);
            rgb = 0;
        }
        return new Color(rgb);
    }

}
