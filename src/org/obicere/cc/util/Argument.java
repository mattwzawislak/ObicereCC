package org.obicere.cc.util;

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

    private final boolean conditional;

    public Argument(final String name, final String preset) {
        Objects.requireNonNull(name);
        this.name = name;
        this.preset = preset;
        this.setTo = preset;
        this.conditional = false;
        this.aliases = new String[0];
    }

    public Argument(final String name, final String preset, final String... aliases) {
        Objects.requireNonNull(name);
        this.name = name;
        this.preset = preset;
        this.setTo = preset;
        this.conditional = false;
        if (aliases == null) {
            this.aliases = new java.lang.String[0];
        } else {
            this.aliases = aliases;
        }
    }

    public Argument(final String name, final boolean preset) {
        Objects.requireNonNull(name);
        this.name = name;
        this.preset = String.valueOf(preset);
        this.setTo = this.preset;
        this.conditional = true;
        this.aliases = new String[0];
    }

    public Argument(final String name, final boolean preset, final String... aliases) {
        Objects.requireNonNull(name);
        this.name = name;
        this.preset = String.valueOf(preset);
        this.setTo = this.preset;
        this.conditional = true;
        if (aliases == null) {
            this.aliases = new java.lang.String[0];
        } else {
            this.aliases = aliases;
        }
    }

    public boolean isDefault() {
        return Objects.equals(preset, setTo);
    }

    public String set(final String newValue) {
        final String oldValue = setTo;
        if (newValue == null) {
            this.setTo = null;
        } else {
            this.setTo = newValue.toLowerCase();
        }
        return oldValue;
    }

    public String set(final boolean value) {
        if (!isConditional()) {
            return null;
        }
        return set(String.valueOf(value));
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

    public boolean isConditional() {
        return conditional;
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
