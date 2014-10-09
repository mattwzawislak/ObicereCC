package org.obicere.cc.executor.language.impl;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.obicere.cc.executor.Case;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.Command;
import org.obicere.cc.executor.language.Casing;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.executor.language.LanguageIdentifier;
import org.obicere.cc.methods.IOUtils;
import org.obicere.cc.methods.StringSubstitute;
import org.obicere.cc.methods.protocol.BasicProtocol;
import org.obicere.cc.projects.Project;
import org.obicere.cc.projects.Runner;

/**
 * @author Obicere
 */
@LanguageIdentifier
public class PythonLanguage extends Language {

    public static final String[] KEYWORDS = new String[]{
            "and", "as", "assert", "break", "class", "continue", "def",
            "del", "elif", "else", "except", "exec", "finally", "for",
            "from", "global", "if", "import", "in", "is", "lambda", "not",
            "or", "pass", "print", "raise", "return", "try", "while", "with",
            "yield"
    };

    public static final String SKELETON = "def ${method}(${parameter}):\n\t";

    public static final String[] LITERALS = new String[]{
            "(?s)(\"){3}(?:[^\"\\\\\r\\u2028\\u2029]|\\\\(?:[^\\n\\rxu0-9]|0(?![0-9])|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|\\n|\\r\\n?))*(\"){3}",
            "(?s)'{3}(?:[^\'\\\\\r\\u2028\\u2029]|\\\\(?:[^\\n\\rxu0-9]|0(?![0-9])|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|\\n|\\r\\n?))*'{3}",
            "'(?:[^'\\\\\\n\\r\\u2028\\u2029]|\\\\(?:[^\\n\\rxu0-9]|0(?![0-9])|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|\\n|\\r\\n?))*'",
            "\"(?:[^\"\\\\\\n\\r\\u2028\\u2029]|\\\\(?:[^\\n\\rxu0-9]|0(?![0-9])|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|\\n|\\r\\n?))*\"",
            "(#.*+)"
    };

    public static final String COMPILED_EXTENSION = ".pyc";

    public static final String SOURCE_EXTENSION = ".py";

    public static final Command[] COMMANDS = new Command[]{
            new Command("python", "${exec} \"${file}\" ${varargs}"),
            new Command("python2", "${exec} \"${file}\" ${varargs}"),
            new Command("python3", "${exec} \"${file}\" ${varargs}")
    };

    public static final Casing METHOD_CASING = Casing.LOWERCASE_UNDERSCORE;

    public static final Casing FIELD_CASING = Casing.LOWERCASE_UNDERSCORE;

    public static final Casing CLASS_CASING = Casing.CAMEL_CASE;

    public static final boolean INCLUDE_PARAMETERS = false;

    public PythonLanguage() {
        super("Python");
    }

    @Override
    public String[] getLiteralMatches() {
        return LITERALS;
    }

    @Override
    public String getSkeleton(final Project project) {
        try {
            final String methodName = project.getRunner().getMethodName();

            final String skeleton = SKELETON;
            final StringSubstitute substitute = new StringSubstitute();

            substitute.put("parameter", buildParameters(project));
            substitute.put("method", methodName);

            return substitute.apply(skeleton);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected Casing getParameterCasing() {
        return FIELD_CASING;
    }

    @Override
    protected boolean shouldDisplayParameterTypes() {
        return INCLUDE_PARAMETERS;
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
    protected String[] getKeyWords() {
        return KEYWORDS;
    }

    @Override
    protected Command[] getCommands() {
        return COMMANDS;
    }

    @Override
    public Result[] compileAndRun(final Project project) {
        final int openPort = IOUtils.findOpenPort();
        if (openPort == -1) {
            displayError(project, "Could not open connection to remote process.");
            return null;
        }
        final Runner runner;
        final ServerSocket host;

        try {
            runner = project.getRunner();
            host = new ServerSocket(openPort);

        } catch (final IOException e) {
            displayError(project, "Failed to open server socket.");
            e.printStackTrace();
            return null;
        }

        final File file = project.getFile(this);
        final String[] message = getProcessExecutor().process(file, "-port", openPort);
        if (message.length != 0) {
            try {
                final Socket accept = host.accept();
                final BasicProtocol protocol = new BasicProtocol(accept);

                final Case[] cases = runner.getCases();
                final int length = cases.length;
                final Object[] container = new Object[length];
                for (int i = 0; i < length; i++) {
                    container[i] = cases[i].getParameters();
                }
                protocol.write(container);

                if (protocol.hasString()) {
                    // Oh oh, error message
                    final List<String> error = new LinkedList<>();
                    do {
                        error.add(protocol.readString());
                    } while (protocol.hasString());
                    displayError(project, error.toArray(new String[1]));
                }

                final Object[] result = protocol.readArray(Object[][].class);
                final Result[] results = new Result[length];
                for (int i = 0; i < length; i++) {
                    results[i] = new Result(result[i], cases[i].getExpectedResult(), container[i]);
                }
                return results;
            } catch (final Exception e) {
                e.printStackTrace();
                displayError(project, "System connection error.");
                return null;
            }
        }
        displayError(project, message);
        return null;
    }
}
