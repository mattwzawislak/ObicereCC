package org.obicere.cc.executor.language.impl.python;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.Command;
import org.obicere.cc.executor.language.Casing;
import org.obicere.cc.executor.language.CodeFormatter;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.executor.language.LanguageIdentifier;
import org.obicere.cc.executor.language.LanguageStreamer;
import org.obicere.cc.util.protocol.MethodInvocationProtocol;
import org.obicere.cc.projects.Project;
import org.obicere.cc.projects.Runner;

import java.io.File;
import java.net.ServerSocket;

/**
 * @author Obicere
 */
@LanguageIdentifier()
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

    public PythonLanguage(final Domain access) {
        super(access, "Python");
        requestStreamer();
    }

    @Override
    public String[] getLiteralMatches() {
        return LITERALS;
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
    protected String getRawSkeleton() {
        return SKELETON;
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
        final LanguageStreamer streamer = streamer();
        final Runner runner = project.getRunner();
        final ServerSocket socket = streamer.createServerSocket(project);

        final File file = project.getFile(this);
        final String[] message = streamer.runProcess(getProcessExecutor(), file, socket.getLocalPort(), project);
        if (message.length != 0) {
            try {
                final MethodInvocationProtocol protocol = streamer.createProtocol(socket);
                streamer.writeInvocationParameters(project, protocol); // We wrote the data
                streamer.waitForResponse(protocol); // lets wait for a reply

                if (protocol.hasString()) {
                    displayError(project, streamer.readError(protocol));
                }
                return streamer.getResultsAndClose(runner.getCases(), protocol);
            } catch (final Exception e) {
                e.printStackTrace();
                displayError(project, "System connection error.");
                return null;
            }
        }
        displayError(project, message);
        return null;
    }

    @Override
    public CodeFormatter getCodeFormatter() {
        return null;
    }
}
