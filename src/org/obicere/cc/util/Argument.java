package org.obicere.cc.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * Creates a new argument handler for parsing program arguments. This
 * contains the information relevant to the given argument, such as: the
 * official name, aliases, initial value, current value and whether or not
 * the value is conditional.
 * <p>
 * The system functions off of model similar to a Java {@link
 * java.util.Properties} system, where the name is the key and the value is
 * the current attribute.
 * <p>
 * So even with conditional arguments, comparison must be made by checking
 * to see if the current attribute is equal to <code>"true"</code>.
 * <p>
 * The standard argument will work off of basic {@link java.lang.String}s,
 * and each argument will be set to lowercase before storing it.
 * <p>
 * The purpose of this system is to provide a type of entry for a
 * properties map. However, functionality has been added by providing the
 * usage of aliases, initial values and whether or not the argument is
 * conditional.
 * <p>
 * This may be split into separate implementations in future revisions, so
 * that conditional arguments can be parsed through <code>boolean</code>
 * values opposed to <code>String</code> values.
 * <p>
 * This comes in use for when handling arguments. For example, we can then
 * define a new argument with the name <code>"foo"</code> and with the
 * alias <code>"f"</code>. <code>foo</code>'s initial value will be
 * <code>"bar"</code>.
 * <p>
 * Initially: <code>
 * <pre>
 * name = "foo"
 * aliases = ["f"]
 * preset = "bar"
 * setTo = "bar"
 * conditional = false
 * </pre>
 * </code>
 * <p>
 * After parsing the following argument:
 * <p>
 * <code>f=test
 * <pre>
 * name = "foo"
 * aliases = ["f"]
 * preset = "bar"
 * setTo = "test"
 * conditional = false
 * </pre>
 * </code>
 * <p>
 * Say if we create a new conditional argument, <code>bar123</code> with no
 * aliases and a default value of <code>false</code>: <code>
 * <pre>
 * name = "bar123"
 * aliases = []
 * preset = "false"
 * setTo = "false"
 * conditional = true
 * </pre>
 * </code>
 * <p>
 * After parsing the following argument:
 * <p>
 * <code>-bar123
 * <pre>
 * name = "bar123"
 * aliases = []
 * preset = "false"
 * setTo = "true"
 * conditional = true
 * </pre>
 * </code>
 *
 * @author Obicere
 * @version 1.0
 */
public class Argument {

    private final String   name;
    private final String[] aliases;

    private final String preset;
    private       String setTo;

    private final boolean conditional;

    /**
     * Constructs a new argument handle for the given <code>name</code> and
     * with the initial value <code>preset</code>. This will have no
     * aliases, will not be a conditional argument and therefore accessible
     * through assignment access:
     * <p>
     * <code>name=newValue</code>
     *
     * @param name   The name of the argument.
     * @param preset The preset value.
     */

    public Argument(final String name, final String preset) {
        checkName(name);
        this.name = name;
        this.preset = preset;
        this.setTo = preset;
        this.conditional = false;
        this.aliases = new String[0];
    }

    /**
     * Constructs a new argument with the given <code>name</code> and with
     * the initial value <code>preset</code>. This argument will also be
     * accessible by any of its <code>aliases</code>. This will not be a
     * conditional argument and therefore accessible through assignment
     * access:
     * <p>
     * <code>name=newValue</code>
     *
     * @param name    The name of the argument.
     * @param preset  The preset value.
     * @param aliases The aliases that can reference this argument.
     */

    public Argument(final String name, final String preset, final String... aliases) {
        checkName(name);
        this.name = name;
        this.preset = preset;
        this.setTo = preset;
        this.conditional = false;
        if (aliases == null) {
            this.aliases = new String[0];
        } else {
            this.aliases = aliases;
        }
    }

    /**
     * Constructs a new argument with the given <code>name</code> and with
     * the initial value <code>preset</code>. This argument will have no
     * aliases. However, this argument will be conditional and therefore
     * modifiable through conditional access:
     * <p>
     * <code>-name</code>
     *
     * @param name   The name of the argument.
     * @param preset The preset value.
     */

    public Argument(final String name, final boolean preset) {
        checkName(name);
        this.name = name;
        this.preset = String.valueOf(preset);
        this.setTo = this.preset;
        this.conditional = true;
        this.aliases = new String[0];
    }

    /**
     * Constructs a new argument with the given <code>name</code> and with
     * the initial value <code>preset</code>. This argument will also be
     * accessible by any of its <code>aliases</code>. This argument will be
     * conditional therefore be accessible through conditional access:
     * <p>
     * <code>-name</code>
     *
     * @param name    The name of the argument.
     * @param preset  The preset value.
     * @param aliases The aliases that can reference this argument.
     */

    public Argument(final String name, final boolean preset, final String... aliases) {
        checkName(name);
        this.name = name;
        this.preset = String.valueOf(preset);
        this.setTo = this.preset;
        this.conditional = true;
        if (aliases == null) {
            this.aliases = new String[0];
        } else {
            this.aliases = aliases;
        }
    }

    /**
     * Checks to see if the name is a valid argument name. If not, a
     * runtime exception is thrown signifying an error with the name.
     * <p>
     * A valid argument name is not <code>null</code> and is not empty.
     *
     * @param name The name of the argument to check.
     * @throws java.lang.IllegalArgumentException if the given argument
     *                                            name is invalid. Either
     *                                            by being <code>null</code>
     *                                            or by being empty.
     */

    private void checkName(final String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Illegal argument name: " + name);
        }
    }

    /**
     * Checks to see if a change has occurred on the value of the argument.
     * If reassignment happens, but with the same initial value, then this
     * will still return <code>true</code>.
     * <p>
     * This is checked through object equality, granted by {@link
     * java.util.Objects#equals(Object, Object)}.
     *
     * @return <code>true</code> if and only if <code>preset =
     * setTo</code>.
     */

    public boolean isDefault() {
        return Objects.equals(preset, setTo);
    }

    /**
     * Assigns a new value to the argument and performs lowercase
     * operations on the value. After the assignment has happened, the old
     * value of this argument will be returned. This allows easy handling
     * of filtering arguments.
     *
     * @param newValue The new value assigned to the argument.
     * @return The previous value of the argument.
     */

    public String set(final String newValue) {
        final String oldValue = setTo;
        if (newValue == null) {
            this.setTo = null;
        } else {
            this.setTo = newValue.toLowerCase();
        }
        return oldValue;
    }

    /**
     * Assigns a new conditional value to this argument if and only if this
     * argument is conditional. If this argument is not conditional, no
     * value will be changed and the <code>null</code> value will be
     * returned.
     * <p>
     * Otherwise, the previous value will be returned.
     *
     * @param value The new value for this conditional argument.
     * @return <code>null</code> if this argument is not conditional,
     * otherwise: the previous value.
     * @see org.obicere.cc.util.Argument#set(String)
     */

    public String set(final boolean value) {
        if (!isConditional()) {
            return null;
        }
        return set(String.valueOf(value));
    }

    /**
     * Retrieves the current value this argument is set to. Comparison with
     * conditional arguments should be done such as:
     * <p>
     * <code> boolean value = "true".equals(argument.get()); </code>
     * <p>
     * This because an error during the parsing mechanics may assign the
     * value to be <code>null</code>, or the argument is not conditional
     * and has a default value of <code>null</code>.
     * <p>
     * Specific handling of how this is interpreted is up to the
     * developer.
     *
     * @return The current value of the argument.
     */

    public String get() {
        return setTo;
    }

    /**
     * Retrieves the default value of this argument. This is set by the
     * calling code and is required by every constructor. This should be
     * set by a manageable value by the developer, so that in the case a
     * user enters an invalid value, then the default value can be used
     * instead.
     * <p>
     * Initially, the argument will also be assigned this value, such that
     * if the user does not specify its new value, this will returned -
     * comparable by <code>==</code>.
     *
     * @return The default value of the argument.
     */

    public String getDefault() {
        return preset;
    }

    /**
     * Retrieves the full scoped name of the argument. This should be used
     * as the key if the argument is put into an argument map. Every
     * argument requires a valid, non-empty name that is not
     * <code>null<</code>.
     *
     * @return The valid name of the argument.
     */

    public String getName() {
        return name;
    }

    /**
     * Retrieves the list of aliases usable by this argument. This does not
     * include the qualified name of the argument, as by {@link
     * Argument#getName()}. This list may be empty, but will never be
     * <code>null</code>. However, the available aliases may not be valid
     * and may collide with other argument names/aliases.
     *
     * @return The non-<code>null</code> list of valid aliases.
     */

    public String[] getAliases() {
        return aliases;
    }

    /**
     * Returns <code>true</code> if and only if this argument is a
     * conditional argument. This means that the argument should be handled
     * through conditional notation:
     * <p>
     * <code>-name</code>
     * <p>
     * And that comparison should be done in a manner similar to:
     * <p>
     * <code>boolean value = "true".equals(argument.get());</code>
     * <p>
     * Arguments are flagged as conditional if their initial value is set
     * to a <code>boolean</code> opposed to a <code>String</code>.
     *
     * @return <code>true</code> if and only if this argument is
     * conditional.
     */

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
