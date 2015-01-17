package org.obicere.cc.executor.language.impl.java;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.executor.Case;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.Command;
import org.obicere.cc.executor.language.Casing;
import org.obicere.cc.executor.language.CodeFormatter;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.executor.language.LanguageExecutorService;
import org.obicere.cc.executor.language.LanguageIdentifier;
import org.obicere.cc.projects.Parameter;
import org.obicere.cc.projects.Project;
import org.obicere.cc.projects.Runner;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

/**
 * {@inheritDoc}
 */
@LanguageIdentifier
public class JavaLanguage extends Language {

    private static final String[] KEYWORDS = new String[]{
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "double", "do", "else",
            "enum", "extends", "false", "final", "finally",
            "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long",
            "native", "new", "null", "package", "private",
            "protected", "public", "return", "short", "static",
            "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "true", "try",
            "void", "volatile", "while"
    };

    //skeleton
    private static final String SKELETON = "public class ${name} {\n\t\n\tpublic ${return} ${method}(${parameter}){\n\t\t\n\t}\n}";

    //literal
    private static final String[] LITERALS = new String[]{
            "\"(?:[^\"\\\\\\n\\r\\u2028\\u2029]|\\\\(?:[^\\n\\rxu0-9]|0(?![0-9])|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|\\n|\\r\\n?))*\"", // Match Strings
            "'(?:[^\"\\\\\\n\\r\\u2028\\u2029]|\\\\(?:[^\\n\\rxu0-9]|0(?![0-9])|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|\\n|\\r\\n?))'", // Match chars
            "(//.*+)|(?s)(/[*].*?[*]/)" // Match comments
    };

    //compiledExtension
    private static final String COMPILED_EXTENSION = ".class";
    //sourceExtension
    private static final String SOURCE_EXTENSION   = ".java";

    private static final Command[] COMPILER_COMMANDS = new Command[]{
            new Command("javac", "${exec} -g -nowarn \"${file}\"")
    };

    private static final Casing METHOD_CASING = Casing.LOWER_CAMEL_CASE;

    private static final Casing FIELD_CASING = Casing.LOWER_CAMEL_CASE;

    private static final Casing CLASS_CASING = Casing.CAMEL_CASE;

    private static final boolean INCLUDE_PARAMETER_TYPES = true;

    //string
    private static final String STRING    = "String";
    //character
    private static final String CHARACTER = "char";
    //integer
    private static final String INTEGER   = "int";
    //float
    private static final String FLOAT     = "double";
    // boolean
    private static final String BOOLEAN   = "boolean";

    private static final String OPEN_ARRAY = "[";

    private static final String CLOSE_ARRAY = "]";

    private static final CodeFormatter FORMATTER = new JavaCodeFormatter();

    public JavaLanguage(final Domain access) {
        super(access, "Java");
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String[] getLiteralMatches() {
        return LITERALS;
    }

    /**
     * Builds the parameters and includes the names for a specific
     * project's method. This was overridden to utilize the Java class
     * naming features, although the default implementation will be just as
     * proficient.
     * <p>
     * The largest difference is not having to generate the correct
     * dimension of array, as that is already packed into the class name.
     *
     * @param project The project to build the parameters off of.
     * @return the built parameters.
     */

    @Override
    public String buildParameters(final Project project) {
        final StringBuilder builder = new StringBuilder();
        final Parameter[] params = project.getRunner().getParameters();
        final Casing casing = getParameterCasing();

        for (int i = 0; i < params.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(params[i].getType().getSimpleName());
            builder.append(' ');
            builder.append(casing.performCase(params[i].getName()));
        }
        return builder.toString();
    }

    /**
     * Builds the parameters with no name utilizing the simple class name.
     * This is used to simulate the Java method referencing convention:
     * <p>
     * <code>Foo#bar(String)</code>
     * <p>
     * For a method <code>bar</code> found in a class named
     * <code>Foo</code> that takes a single {@link java.lang.String}
     * argument.
     *
     * @param project The project to build the parameters off of.
     * @return The built nameless parameters.
     */

    private String buildNamelessParameters(final Project project) {
        final StringBuilder builder = new StringBuilder();
        final Parameter[] params = project.getRunner().getParameters();

        for (int i = 0; i < params.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(params[i].getType().getSimpleName());
        }
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     * <p>
     * For the Java language, this will utilize the {@link
     * java.net.URLClassLoader} to load the user's file. Then, reflection
     * is utilized to load the method and invoke it.
     * <p>
     * In the case of an exception in the invocation of the user's code,
     * the exception is parsed. The part of the error generated by
     * reflection is thrown out and the actual error is parsed and
     * displayed. Should the error fail to be parsed, then a generic error
     * message for the error message is thrown.
     */

    @Override
    public Result[] compileAndRun(final Project project) {
        final File file = project.getFile(this);
        final String[] message = getCommandExecutor().process(file);
        if (message.length == 0) {
            final Runner runner = project.getRunner();
            try {
                final Parameter[] parameters = runner.getParameters();
                Case[] cases;
                try {
                    cases = runner.getCases();
                } catch (final Throwable e) {
                    e.printStackTrace();
                    return null;
                }
                final Class<?>[] searchClasses = new Class<?>[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    searchClasses[i] = parameters[i].getType();
                }

                final URL[] urls = new URL[]{getDirectory().toURI().toURL()};
                final ClassLoader cl = new URLClassLoader(urls);
                final Class<?> invoke = cl.loadClass(project.getName());
                final Method method = invoke.getDeclaredMethod(runner.getMethodName(), searchClasses);

                final Function<Case, FutureTask<Object>> supplier = c -> new FutureTask<>(() -> method.invoke(invoke.newInstance(), c.getParameters()));
                final LanguageExecutorService service = new LanguageExecutorService(cases, supplier);
                return service.requestResults();
            } catch (final NoSuchMethodException m) {
                displayError(project, "Method not found.\n" +
                                      "Your code must have a method " + runner.getMethodName() + "(" + buildNamelessParameters(project) + ") defined.");
                return null;
            } catch (final Exception e) {
                // java.lang.AssertionError blah blah blah
                // ...
                // Caused by: <- We need this
                final StringWriter writer = new StringWriter();
                final PrintWriter printer = new PrintWriter(writer);
                e.printStackTrace(printer);

                final String fullError = writer.toString();
                final int index = fullError.indexOf("Caused by:");
                if (index > 0) {
                    displayError(project, fullError.substring(index));
                } else {
                    displayError(project, "Failed to parse error.");
                }
                return null;
            }
        }
        displayError(project, message);
        return null;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected Casing getParameterCasing() {
        return FIELD_CASING;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected Casing getMethodCasing() {
        return METHOD_CASING;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected Casing getClassCasing() {
        return CLASS_CASING;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected boolean shouldDisplayParameterTypes() {
        return INCLUDE_PARAMETER_TYPES;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getStringType() {
        return STRING;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getCharacterType() {
        return CHARACTER;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getIntegerType() {
        return INTEGER;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getFloatType() {
        return FLOAT;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getBooleanType() {
        return BOOLEAN;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getArrayOpen() {
        return OPEN_ARRAY;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getArrayClose() {
        return CLOSE_ARRAY;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getSourceExtension() {
        return SOURCE_EXTENSION;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String getCompiledExtension() {
        return COMPILED_EXTENSION;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getRawSkeleton() {
        return SKELETON;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String[] getKeyWords() {
        return KEYWORDS;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public Command[] getCommands() {
        return COMPILER_COMMANDS;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public CodeFormatter getCodeFormatter() {
        return FORMATTER;
    }
}
