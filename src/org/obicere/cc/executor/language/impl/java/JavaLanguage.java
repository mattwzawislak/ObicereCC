package org.obicere.cc.executor.language.impl.java;

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

    public JavaLanguage() {
        super("Java");
    }

    @Override
    public String[] getLiteralMatches() {
        return LITERALS;
    }

    @Override
    public String buildParameters(final Project project) {
        final StringBuilder builder = new StringBuilder();
        final Parameter[] params = project.getRunner().getParameters();

        for (int i = 0; i < params.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(params[i].getType().getSimpleName());
            builder.append(' ');
            builder.append(params[i].getName());
        }
        return builder.toString();
    }

    @Override
    public Result[] compileAndRun(final Project project) {
        final File file = project.getFile(this);
        final String[] message = getProcessExecutor().process(file);
        if (message.length == 0) {
            try {
                final Runner runner = project.getRunner();
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
                }
                return null;
            }
        }
        displayError(project, message);
        return null;
    }

    @Override
    protected Casing getParameterCasing() {
        return FIELD_CASING;
    }

    @Override
    protected Casing getMethodCasing() {
        return METHOD_CASING;
    }

    @Override
    protected Casing getClassCasing() {
        return CLASS_CASING;
    }

    @Override
    protected boolean shouldDisplayParameterTypes() {
        return INCLUDE_PARAMETER_TYPES;
    }

    @Override
    protected String getStringType() {
        return STRING;
    }

    @Override
    protected String getCharacterType() {
        return CHARACTER;
    }

    @Override
    protected String getIntegerType() {
        return INTEGER;
    }

    @Override
    protected String getFloatType() {
        return FLOAT;
    }

    @Override
    protected String getBooleanType() {
        return BOOLEAN;
    }

    @Override
    protected String getArrayOpen() {
        return OPEN_ARRAY;
    }

    @Override
    protected String getArrayClose() {
        return CLOSE_ARRAY;
    }

    @Override
    public String getSourceExtension() {
        return SOURCE_EXTENSION;
    }

    @Override
    public String getCompiledExtension() {
        return COMPILED_EXTENSION;
    }

    @Override
    protected String getRawSkeleton() {
        return SKELETON;
    }

    @Override
    public String[] getKeyWords() {
        return KEYWORDS;
    }

    @Override
    public Command[] getCommands() {
        return COMPILER_COMMANDS;
    }

    @Override
    public CodeFormatter getCodeFormatter() {
        return FORMATTER;
    }
}
