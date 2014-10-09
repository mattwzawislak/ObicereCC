package org.obicere.cc.executor.language.impl;

import org.obicere.cc.executor.Case;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.Command;
import org.obicere.cc.executor.language.Casing;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.executor.language.LanguageIdentifier;
import org.obicere.cc.methods.StringSubstitute;
import org.obicere.cc.projects.Parameter;
import org.obicere.cc.projects.Project;
import org.obicere.cc.projects.Runner;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@LanguageIdentifier
public class JavaLanguage extends Language {

    public static final String[] KEYWORDS = new String[]{
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
    public static final String SKELETON = "public class ${name} {\n\t\n\tpublic ${return} ${method}(${parameter}){\n\t\t\n\t}\n}";

    //literal
    public static final String[] LITERALS = new String[]{
            "\"(?:[^\"\\\\\\n\\r\\u2028\\u2029]|\\\\(?:[^\\n\\rxu0-9]|0(?![0-9])|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|\\n|\\r\\n?))*\"", // Match Strings
            "'(?:[^\"\\\\\\n\\r\\u2028\\u2029]|\\\\(?:[^\\n\\rxu0-9]|0(?![0-9])|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|\\n|\\r\\n?))'", // Match chars
            "(//.*+)|(?s)(/[*].*?[*]/)" // Match comments
    };

    //compiledExtension
    public static final String COMPILED_EXTENSION = ".class";
    //sourceExtension
    public static final String SOURCE_EXTENSION   = ".java";

    public static final Command[] COMPILER_COMMANDS = new Command[]{
            new Command("javac", "${exec} -g -nowarn \"${file}\"")
    };

    public static final Casing METHOD_CASING = Casing.LOWER_CAMEL_CASE;

    public static final Casing FIELD_CASING = Casing.LOWER_CAMEL_CASE;

    public static final Casing CLASS_CASING = Casing.CAMEL_CASE;

    public static final boolean INCLUDE_PARAMETER_TYPES = true;

    //string
    public static final String STRING    = "String";
    //character
    public static final String CHARACTER = "char";
    //integer
    public static final String INTEGER   = "int";
    //float
    public static final String FLOAT     = "double";

    public static final String OPEN_ARRAY = "[";

    public static final String CLOSE_ARRAY = "]";

    public JavaLanguage() {
        super("Java");
    }

    @Override
    public String[] getLiteralMatches() {
        return LITERALS;
    }

    public String getSkeleton(final Project project) {
        try {
            final Runner runner = project.getRunner();

            final String returnType = runner.getReturnType().getCanonicalName();
            final String methodName = runner.getMethodName();

            final String skeleton = SKELETON;
            final StringSubstitute substitute = new StringSubstitute();

            substitute.put("parameter", buildParameters(project));
            substitute.put("name", project.getName());
            substitute.put("return", returnType);
            substitute.put("method", methodName);

            return substitute.apply(skeleton);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return "";
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
        final String[] message = getProcessExecutor().compile(file);
        if (message.length == 0) {
            try {
                final Runner runner = project.getRunner();
                final Parameter[] parameters = runner.getParameters();
                final Case[] cases = runner.getCases();
                final Class<?>[] searchClasses = new Class<?>[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    searchClasses[i] = parameters[i].getType();
                }

                final URL[] urls = new URL[]{getDirectory().toURI().toURL()};
                final ClassLoader cl = new URLClassLoader(urls);
                final Class<?> invoke = cl.loadClass(project.getName());
                final Method method = invoke.getDeclaredMethod(runner.getMethodName(), searchClasses);

                final Result[] results = new Result[cases.length];
                for (int i = 0; i < results.length; i++) {
                    final Case thisCase = cases[i];
                    final Object result = method.invoke(invoke.newInstance(), thisCase.getParameters());
                    results[i] = new Result(result, thisCase.getExpectedResult(), thisCase.getParameters());
                }
                return results;
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
                return new Result[0];
            }
        }
        displayError(project, message);
        return new Result[0];
    }

    @Override
    public Casing getParameterCasing() {
        return FIELD_CASING;
    }

    @Override
    public boolean shouldDisplayParameterTypes() {
        return INCLUDE_PARAMETER_TYPES;
    }

    @Override
    public String getStringType() {
        return STRING;
    }

    @Override
    public String getCharacterType() {
        return CHARACTER;
    }

    @Override
    public String getIntegerType() {
        return INTEGER;
    }

    @Override
    public String getFloatType() {
        return FLOAT;
    }

    @Override
    public String getArrayOpen() {
        return OPEN_ARRAY;
    }

    @Override
    public String getArrayClose() {
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
    public String[] getKeyWords() {
        return KEYWORDS;
    }

    @Override
    public Command[] getCommands() {
        return COMPILER_COMMANDS;
    }
}
