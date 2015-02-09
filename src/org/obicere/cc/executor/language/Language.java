package org.obicere.cc.executor.language;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.configuration.DomainAccess;
import org.obicere.cc.configuration.Paths;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.Command;
import org.obicere.cc.executor.compiler.CommandExecutor;
import org.obicere.cc.gui.swing.projects.Editor;
import org.obicere.cc.projects.Parameter;
import org.obicere.cc.projects.Project;
import org.obicere.cc.util.StringSubstitute;

import javax.swing.JOptionPane;
import java.io.File;
import java.util.logging.Level;

/**
 * The fully inclusive language specification. This should include the
 * basic information for handling any generic systems required to add
 * support. Such information would be:
 * <pre><ul>
 * <li> Casing:
 *      the correct casing specification for handling code generation when
 *      creating classes, methods and variables. This is required to be
 *      specified by the implementation.
 * <li> Basic Types:
 *      the names of the types that correspond to the Java
 *      <code>String</code>, <code>char</code>, <code>int</code>,
 *      <code>float</code> and <code>boolean</code> types.
 * <li> Keywords:
 *      the list of all keywords that are implemented by the language.
 *      This should be in sorted order, since the lookup utilizes binary
 *      search.
 * <li> Literals:
 *      the set of regexs that match the literal types in a language, such
 *      as the strings, comments and characters.
 * <li> Code Formatter:
 *      the implementation for handling default code formatting on-the-go.
 * <li> Code Skeleton:
 *      the basic code skeleton for creating a new runner implementation.
 *      The elements to add are: <code>parameter</code>,
 *      <code>method</code>, <code>name</code> and <code>return</code>.
 * <li> Array creation:
 *      the opening and closing characters for array specification. This
 *      will be used generically to create n-dimensional arrays as needed.
 * <li> Compilation and invocation:
 *      the implementation on how the project should be ran for a specific
 *      language. This can be handled either by another language or
 *      through Java. This can and may also be OS-specific.
 * </ul>
 * </pre>
 * <p>
 * The specification can also request a {@link org.obicere.cc.executor.language.LanguageStreamer}.
 * This will provide the specification default access to the protocol
 * system for communicating between languages. This is not a default
 * feature, as the specification may either wish to implement its own form
 * of communication. This was to avoid allocating unnecessary resources
 * that may not be needed.
 * <p>
 * For a language to be detected by the {@link org.obicere.cc.executor.language.LanguageManager},
 * it must simply just inherit this class. The manager will automatically
 * find it and create it.
 * <p>
 * The manager looks for a <code>Language(Domain)</code> constructor. The
 * domain will be provided through reflection and then added to the
 * manager's pool.
 *
 * @author Obicere
 * @version 1.0
 */

public abstract class Language extends DomainAccess {

    private final String name;

    private final File            directory;
    private final CommandExecutor commandExecutor;

    private LanguageStreamer streamer;

    /**
     * Constructs a new language instance with full {@link
     * org.obicere.cc.configuration.Domain} access under the specific name.
     * The may wish to implement a version number if the language
     * specification changed between versions.
     *
     * @param access The domain access.
     * @param name   The name of the language.
     */

    protected Language(final Domain access, final String name) {
        super(access);
        try {
            this.name = name;
            this.directory = new File(Paths.FOLDER_DATA, name);
            if (!directory.exists() && !directory.mkdir()) {
                log.log(Level.WARNING, "Failed to create directory for " + name);
            }
            final String src = getSourceExtension();
            final String cmp = getCompiledExtension();
            final Command[] commands = getCommands();

            this.commandExecutor = new CommandExecutor(commands, src, cmp);

        } catch (final Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Failed to load language: " + name);
            throw new IllegalArgumentException();
        }
    }

    /**
     * Requests a {@link org.obicere.cc.executor.language.LanguageStreamer}
     * be implemented for this language. This is not added by default, so
     * the request must be made if the usage of a streamer is needed.
     *
     * @see #streamer()
     */

    public void requestStreamer() {
        this.streamer = new LanguageStreamer(this);
    }

    /**
     * Retrieves the {@link org.obicere.cc.executor.language.LanguageStreamer}
     * that was requested. If no such request was made, this method will
     * return <code>null</code>.
     * <p>
     * This may be modified in future release to automatically request a
     * streamer if this was called and no such request was made.
     *
     * @return The requested streamer, if it exists. Otherwise
     * <code>null</code>.
     * @see #requestStreamer()
     */

    public LanguageStreamer streamer() {
        return streamer;
    }

    /**
     * Checks to see if through binary search that the given string is a
     * keyword for this language specification. The list of keywords
     * returned by {@link #getKeywords()} should be sorted. This is a
     * performance improvement, since thousands of searches could be called
     * per second. To counter this, a binary search algorithm is used to
     * reduce time.
     *
     * @param word The word to check if it is a keyword.
     * @return <code>true</code> if and only if the word is a keyword.
     */

    public boolean isKeyword(final String word) {
        final String[] keywords = getKeywords();

        int low = 0;
        int high = keywords.length - 1;

        while (low <= high) {
            final int mid = (low + high) / 2;
            final int comp = keywords[mid].compareTo(word);

            if (comp < 0) {
                low = mid + 1;
            } else if (comp > 0) {
                high = mid - 1;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the name of the language as specified in the constructor
     * of the implementation.
     *
     * @return The language's name.
     */

    public String getName() {
        return name;
    }

    /**
     * The working directory for the source and compiled files for this
     * language. Note that this may cross over with other languages of the
     * same name. To counter this of course, names could be changes in
     * accordance.
     * <p>
     * This is defined as: <code>{@link Paths#FOLDER_DATA} + {@link
     * File#separator} + {@link #getName()}</code>. Where <code>+</code> is
     * concatenation.
     *
     * @return The directory of the files for this language.
     * @see #getName()
     */

    public File getDirectory() {
        return directory;
    }

    /**
     * Retrieves the command executor used to compile and invoke the
     * project files.
     *
     * @return The instance of the executor.
     */

    protected CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    /**
     * Generates a new code skeleton for the given project file. The
     * required fields are:
     * <p>
     * <pre>
     * <ul>
     * <li> parameter:
     *      The method parameters.
     * <li> method:
     *      The method name.
     * <li> name:
     *      The name of the class - usually the project name.
     * <li> return:
     *      The return type of the method.
     * </ul>
     * </pre>
     * <p>
     * For example, the following Java code could be generated from the
     * skeleton:
     * <pre>
     * {@code
     *
     * public class Foo {
     *      public String foo(int[] nums){
     *          //...
     *      }
     * }
     * }
     * As:
     * {@code
     *
     * public class {$name} {
     *      public {$return} {$method}({$parameter}){
     *          //...
     *      }
     * }
     * }
     * </pre>
     * <p>
     * When the parameters are equal to their respective parts.
     *
     * @param project The project to base the skeleton off of.
     * @return The new skeleton with the correct parameters.
     * @see #buildParameters(org.obicere.cc.projects.Project)
     * @see #getMethodString(org.obicere.cc.projects.Project)
     * @see #getClassString(org.obicere.cc.projects.Project)
     * @see #getStringForClass(Class)
     */

    public String getSkeleton(final Project project) {
        try {

            final Class<?> returnType = project.getRunner().getReturnType();

            final String skeleton = getRawSkeleton();
            final StringSubstitute substitute = new StringSubstitute();

            substitute.put("parameter", buildParameters(project));
            substitute.put("method", getMethodString(project));
            substitute.put("name", getClassString(project));
            substitute.put("return", getStringForClass(returnType));

            return substitute.apply(skeleton);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Properly cases the method name for the given project. This is used
     * when generating the skeleton.
     *
     * @param project The project to base the skeleton off of.
     * @return The properly cased method name.
     */

    private String getMethodString(final Project project) {
        final Casing methodCasing = getMethodCasing();
        final String methodName = project.getRunner().getMethodName();
        if (methodCasing != null) {
            return methodCasing.performCase(methodName);
        } else {
            return methodName;
        }
    }

    /**
     * Properly cases the class name for the given project. This is used
     * when generating the skeleton.
     *
     * @param project The project to base the skeleton off of.
     * @return The properly cased class name.
     */

    private String getClassString(final Project project) {
        final Casing classCasing = getClassCasing();
        final String className = project.getName();
        if (classCasing != null) {
            return classCasing.performCase(className);
        } else {
            return className;
        }
    }

    /**
     * The casing type to use for parameters. The most common type is the
     * {@link Casing#LOWER_CAMEL_CASE}, but is still required by contract
     * so no default can be set accurately.
     *
     * @return The casing to use for parameters.
     * @see org.obicere.cc.executor.language.Casing
     */

    protected abstract Casing getParameterCasing();

    /**
     * The casing type to use for methods. The most common type is the
     * {@link Casing#LOWER_CAMEL_CASE}, but is still required by contract
     * so no default can be set accurately.
     *
     * @return The casing to use for methods.
     * @see org.obicere.cc.executor.language.Casing
     */

    protected abstract Casing getMethodCasing();

    /**
     * The casing type to use for classes. The most common type is the
     * {@link Casing#CAMEL_CASE}, but is still required by contract so no
     * default can be set accurately.
     *
     * @return The casing to use for classes.
     * @see org.obicere.cc.executor.language.Casing
     */
    protected abstract Casing getClassCasing();

    /**
     * Specifies the tab size for given language. This is used to properly
     * size the tabs in the code pane. This does change font-by-font so a
     * more generic implementation is set here. Therefore this is
     * represented by the number of spaces is equivalent to a tab. In most
     * languages, such as Java, C++ or ASM: the tab size is 4. Therefore a
     * default value of <code>4</code> was set.
     *
     * @return The amount of spaces per tab for the language.
     */

    public int getTabSize() {
        return 4;
    }

    /**
     * Whether or not to display parameter types when generating a
     * skeleton. An example of where this would be set to <code>true</code>
     * would be in Java, where the types must be specified. An example
     * where this would be set to <code>false</code> would be in Python.
     * <p>
     * In general strong-typed languages will have this set to
     * <code>true</code> and weak-typed languages will have this set to
     * <code>false</code>.
     *
     * @return Whether or not to display parameter types.
     */

    protected abstract boolean shouldDisplayParameterTypes();

    /**
     * The string representation of the {@link java.lang.String} type in
     * the specific language.
     * <p>
     * Set to default as the empty string to reduce verbosity on weak-typed
     * languages.
     *
     * @return The string representation of the string data type.
     */

    protected String getStringType() {
        return "";
    }

    /**
     * The string representation of the <code>char</code> type in the
     * specific language.
     * <p>
     * Set to default as the empty string to reduce verbosity on weak-typed
     * languages.
     *
     * @return The string representation of the char data type.
     */

    protected String getCharacterType() {
        return "";
    }

    /**
     * The string representation of the <code>int</code> type in the
     * specific language.
     * <p>
     * Set to default as the empty string to reduce verbosity on weak-typed
     * languages.
     *
     * @return The string representation of the int data type.
     */

    protected String getIntegerType() {
        return "";
    }

    /**
     * The string representation of the <code>float</code> type in the
     * specific language.
     * <p>
     * Set to default as the empty string to reduce verbosity on weak-typed
     * languages.
     *
     * @return The string representation of the float data type.
     */

    protected String getFloatType() {
        return "";
    }

    /**
     * The string representation of the <code>boolean</code> type in the
     * specific language.
     * <p>
     * Set to default as the empty string to reduce verbosity on weak-typed
     * languages.
     *
     * @return The string representation of the boolean data type.
     */

    protected String getBooleanType() {
        return "";
    }

    /**
     * The opening string to specify an array type. Different languages may
     * handle this differently, so the opening and closing sequences are
     * split.
     *
     * @return The opening sequence for the array specification.
     */

    protected String getArrayOpen() {
        return "";
    }

    /**
     * The closing string to specify an array type. Different languages may
     * handle this differently, so the opening and closing sequences are
     * split.
     *
     * @return The closing sequence for the array specification.
     */

    protected String getArrayClose() {
        return "";
    }

    /**
     * The file extensions for the source files. This should include the
     * dot. Such that for the Java language, this value would be defined as
     * <code>.java</code>
     *
     * @return The source extension including the dot.
     */

    public abstract String getSourceExtension();

    /**
     * The file extensions for the compiled files. This should include the
     * dot. Such that for the Java language, this value would be defined as
     * <code>.class</code>
     *
     * @return The compiled extension including the dot.
     */

    public abstract String getCompiledExtension();

    /**
     * The raw skeleton to be used as the framework for generating
     * skeletons for projects.
     * <p>
     * This may include the following terms for formatting the skeleton:
     * <pre>
     * <ul>
     * <li> parameter:
     *      The method parameters.
     * <li> method:
     *      The method name.
     * <li> name:
     *      The name of the class - usually the project name.
     * <li> return:
     *      The return type of the method.
     * </ul>
     * </pre>
     *
     * @return The format to base the skeleton off of.
     */

    protected abstract String getRawSkeleton();

    /**
     * Retrieves the list of keywords defined in the language. This should
     * be sorted by binary search for it to work effectively. This was
     * chosen for a performance reason.
     *
     * @return The sorted list of keywords.
     */

    protected abstract String[] getKeywords();

    /**
     * The list of literal matchers. In order of their precedence. This is
     * in case two matchers may intersect and one is considered the more
     * formal matcher.
     *
     * @return The list of matchers for the language.
     */

    public abstract String[] getLiteralMatchers();

    /**
     * The running commands for the compiler. These can be ordered in terms
     * of likelihood to work. This is a small change, but the order can
     * matter if there are issues with a certain compiler.
     *
     * @return The list of commands to test for a working command.
     */

    protected abstract Command[] getCommands();

    /**
     * Compiles and runs the set project based off of the current code
     * saved in the file. This can be done in a number of ways, so this is
     * purely language specific. This is where the {@link
     * org.obicere.cc.executor.language.LanguageStreamer} would mostly be
     * used, once requested through {@link #requestStreamer()}.
     * <p>
     * The returned results may be <code>null</code>. This will signify an
     * error has occurred to the {@link org.obicere.cc.gui.swing.projects.Editor}
     * for this specific project.
     * <p>
     * It is also recommended that if the operation is lengthy, that a new
     * {@link java.lang.Thread} be spawned to avoid blocking the current
     * thread. In most cases, it should be fine to run it on the current
     * thread without hitch.
     *
     * @param project The project to base the skeleton off of.
     * @return The results from the project's attempt to solve the problem.
     */

    public abstract Result[] compileAndRun(final Project project);

    /**
     * Retrieves the {@link org.obicere.cc.executor.language.CodeFormatter}
     * for handling format operations in the code pane.
     * <p>
     * In reality, this should be implemented as a listener. But then again
     * there is enough ambiguity in the actual implementation that it could
     * pass as a utility. Even then, the entire implementation is not
     * finished as of build v1.0. In future releases however, this may
     * become a listener.
     *
     * @return The code formatter for handling format operations.
     */

    public abstract CodeFormatter getCodeFormatter();

    /**
     * Builds an n-dimensional array. This can be overridden by the
     * implementation if required. The default operation will create a
     * string of size <code>n * 2</code> where the first <code>n</code>
     * characters are the {@link #getArrayOpen()} and the last
     * <code>n</code> characters are {@link #getArrayClose()}.
     *
     * @param size The dimension value for the array, <code>n</code>.
     * @return The built n-dimensional array header.
     */

    protected String getArray(final int size) {
        if (size <= 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(size * 2);
        for (int i = 0; i < size; i++) {
            builder.append(getArrayOpen());
            builder.append(getArrayClose());
        }
        return builder.toString();
    }

    /**
     * Attempts to retrieve the dimension from the class. This is done by
     * continuously looping on the predicate that the class is an array.
     *
     * @param cls The class to retrieve the array dimension for.
     * @return The array dimension, with a default value of <code>0</code>.
     */

    private int getArrayDimension(final Class<?> cls) {

        int count = 0;
        Class<?> subCls = cls;
        while (subCls.isArray()) {
            subCls = subCls.getComponentType();
            count++;
        }
        return count;
    }

    /**
     * Builds the parameters by switching the class in Java out for the
     * string representation. These representations are specified in the
     * implementation.
     * <p>
     * If the specification has a better way to handle this, the method can
     * be overridden as to utilize this.
     *
     * @param project The project to build the parameters of the method
     *                for.
     * @return The built parameters.
     */

    public String buildParameters(final Project project) {
        final StringBuilder builder = new StringBuilder();
        final Parameter[] params = project.getRunner().getParameters();

        final boolean displayTypes = shouldDisplayParameterTypes();

        final Casing param = getParameterCasing();

        for (int i = 0; i < params.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            if (displayTypes) {
                builder.append(getStringForClass(params[i].getType()));
            }
            if (param != null) {
                builder.append(param.performCase(params[i].getName()));
            } else {
                builder.append(params[i].getName());
            }
        }
        return builder.toString();
    }

    /**
     * Retrieves the appropriate type for the given Java type. This will
     * ignore array dimensions and generate the new dimension based off of
     * the language specification.
     *
     * @param cls The class to retrieve the language-specific code for.
     * @return The language-specific code for the parameter.
     */

    private String getStringForClass(final Class<?> cls) {
        final StringBuilder builder = new StringBuilder();
        final String clsName = cls.getSimpleName().replaceAll("(\\[|\\])+", "");
        switch (clsName) {
            case "int":
            case "Integer":
                builder.append(getIntegerType());
                break;

            case "char":
            case "Character":
                builder.append(getCharacterType());
                break;

            case "float":
            case "Float":
            case "double":
            case "Double":
                builder.append(getFloatType());
                break;

            case "boolean":
            case "Boolean":
                builder.append(getBooleanType());
                break;

            case "String":
                builder.append(getStringType());
                break;
        }
        final int count = getArrayDimension(cls);
        if (count >= 1) {
            builder.append(getArray(count));
        }
        return builder.toString();
    }

    /**
     * Publicly displays an error regarding the compilation and execution
     * of the project. This will display in the editor panel if possible,
     * otherwise it will show a {@link javax.swing.JOptionPane}.
     * <p>
     * This will also attempt to clean up the error by removing the working
     * directory where applicable.
     *
     * @param project The project currently being processed.
     * @param error   The error that occurred, that will be displayed to
     *                the user.
     */

    public void displayError(final Project project, final String... error) {
        final Editor editor = access.getFrameManager().getTab(project.getName(), this);
        final StringBuilder builder = new StringBuilder();
        final String path = project.getFile(this).getAbsolutePath();
        for (final String str : error) {
            builder.append(str.replace(path, "line"));
            builder.append(System.lineSeparator());
        }
        if (editor != null) {
            editor.setInstructionsText(builder.toString(), true);
        } else {
            JOptionPane.showMessageDialog(null, builder.toString());
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Language && ((Language) obj).getName().equals(getName());
    }

    /**
     * {@inheritDoc}
     * <p>
     * The name of the language is the default string representation.
     */

    @Override
    public String toString() {
        return name;
    }

}
