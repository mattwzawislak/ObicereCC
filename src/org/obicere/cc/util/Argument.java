package org.obicere.cc.util;

import com.sun.org.apache.xpath.internal.Arg;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Obicere
 */
public class Argument {

    private final String   name;
    private final String[] aliases;

    private final String preset;
    private       String setTo;

    public Argument(final String name, final String preset) {
        Objects.requireNonNull(name);
        this.name = name;
        this.preset = preset;
        this.setTo = preset;
        this.aliases = new java.lang.String[0];
    }

    public Argument(final String name, final String preset, final String... aliases) {
        Objects.requireNonNull(name);
        this.name = name;
        this.preset = preset;
        this.setTo = preset;
        if (aliases == null) {
            this.aliases = new java.lang.String[0];
        } else {
            this.aliases = aliases;
        }
    }

    public Argument(final String name, final boolean preset) {

    }

    public Argument(final String name, final boolean preset, final String... aliases) {

    }

    public boolean isDefault() {
        return Objects.equals(preset, setTo);
    }

    public String set(final String newValue) {
        final String oldValue = setTo;
        this.setTo = newValue;
        return oldValue;
    }

    public String get() {
        return setTo;
    }

    public String getDefault() {
        return preset;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(45);
        builder.append("Argument[name=");
        builder.append(name);
        builder.append(",aliases=");
        builder.append(Arrays.toString(aliases));
        builder.append(",default=");
        builder.append(preset);
        builder.append(",value=");
        builder.append(setTo);
        builder.append("]");
        return builder.toString();
    }

}
