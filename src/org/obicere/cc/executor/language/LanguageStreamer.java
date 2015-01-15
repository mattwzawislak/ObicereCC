package org.obicere.cc.executor.language;

import org.obicere.cc.executor.Case;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.CommandExecutor;
import org.obicere.cc.util.IOUtils;
import org.obicere.cc.util.protocol.BasicProtocol;
import org.obicere.cc.util.protocol.MethodInvocationProtocol;
import org.obicere.cc.projects.Parameter;
import org.obicere.cc.projects.Project;
import org.obicere.cc.projects.Runner;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Obicere
 */
public class LanguageStreamer {

    private static final long RESPONSE_WAIT = 10000;

    private final Language language;

    protected LanguageStreamer(final Language language) {
        this.language = language;
    }

    public ServerSocket createServerSocket(final Project project) {
        final int openPort = IOUtils.findOpenPort();
        if (openPort == -1) {
            language.displayError(project, "Could not open connection to remote process.");
            return null;
        }

        try {
            return new ServerSocket(openPort);
        } catch (final IOException e) {
            language.displayError(project, "Failed to open server socket.");
            e.printStackTrace();
            return null;
        }
    }

    public MethodInvocationProtocol createProtocol(final ServerSocket server) {
        try {
            final Socket accept = server.accept();
            return new MethodInvocationProtocol(accept, true);
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void waitForResponse(final BasicProtocol protocol) throws IOException {
        final long start = System.currentTimeMillis();
        while (!protocol.hasNext()) {
            if (System.currentTimeMillis() - start > RESPONSE_WAIT) {
                throw new IOException("No response from console before response expiration.");
            }
        }
    }

    public void writeInvocationParameters(final Project project, final MethodInvocationProtocol protocol) {
        final Runner runner = project.getRunner();
        final Parameter[] params = runner.getParameters();
        final int paramLength = params.length;

        final String methodName = runner.getMethodName();
        final Class<?> returnClass = runner.getReturnType();
        final Class<?>[] paramClasses = new Class<?>[paramLength];
        for (int i = 0; i < paramLength; i++) {
            paramClasses[i] = params[i].getType();
        }

        protocol.writeInvocation(methodName, returnClass, paramClasses);
        protocol.writeCases(runner.getCases());
    }

    public Result[] getResults(final Case[] cases, final BasicProtocol protocol) throws IOException {
        final int length = cases.length;

        final Object[] response = protocol.readArray(Object[][].class);

        if (response == null || response.length != length) {
            throw new IOException("Invalid results returned from console.");
        }

        final Result[] results = new Result[length];
        for (int i = 0; i < length; i++) {
            final Case current = cases[i];
            results[i] = new Result(response[i], current.getExpectedResult(), current.getParameters());
        }
        return results;
    }

    public Result[] getResultsAndClose(final Case[] cases, final BasicProtocol protocol) throws IOException {
        final Result[] results = getResults(cases, protocol);
        protocol.close();
        return results;
    }

    public String[] readError(final BasicProtocol protocol) {
        final List<String> error = new LinkedList<>();
        do {
            error.add(protocol.readString());
        } while (protocol.hasString());
        return error.toArray(new String[error.size()]);
    }

    public String[] runProcess(final CommandExecutor executor, final File exec, final int port, final Project project) {
        final Parameter[] params = project.getRunner().getParameters();

        // The format for args should be like so:
        // C:/Test/script.java -port 550000 String[] String[] int
        final String[] args = new String[params.length + 2];
        args[0] = "-port";
        args[1] = String.valueOf(port);
        for (int i = 0; i < params.length; i++) {
            args[i] = params[i].getType().getSimpleName();
        }
        return executor.process(exec, (Object[]) args);
    }
}
