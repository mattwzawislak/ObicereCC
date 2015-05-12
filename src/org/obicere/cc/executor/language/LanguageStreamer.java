package org.obicere.cc.executor.language;

import org.obicere.cc.executor.Case;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.CommandExecutor;
import org.obicere.cc.projects.Parameter;
import org.obicere.cc.projects.Project;
import org.obicere.cc.projects.Runner;
import org.obicere.cc.util.MethodInvocationProtocol;
import org.obicere.utility.io.IOUtils;
import org.obicere.utility.protocol.PrimitiveSocketProtocol;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;

/**
 * Sets up the process for connecting the {@link org.obicere.cc.executor.language.Language}'s
 * Java implementation to its respective language implementation. This will
 * utilize the {@link org.obicere.utility.protocol.PrimitiveSocketProtocol}
 * to transfer data from the program to the invoker for the language.
 * <p>
 * The invoker is a program that language-specific and usually written in
 * the target language. It receives the server information through its
 * program arguments and connects to the server from there. With this, it
 * will receive the case information and then invoke the project file in
 * the target language.
 * <p>
 * This works primarily through the localhost. Therefore this can see a
 * performance increase on Window's machines by utilizing the Fast TCP
 * Loopback.
 * <p>
 * Each connection will survive for 10 seconds. After that amount, the
 * invocation gets interrupted. Should this happen, the call should adhere
 * to the call.
 *
 * @author Obicere
 * @version 1.0
 */
public class LanguageStreamer {

    private static final long RESPONSE_WAIT = 10000;

    private final Language language;

    /**
     * Constructs a new streamer to function on a language.
     *
     * @param language The language to stream.
     */

    protected LanguageStreamer(final Language language) {
        this.language = language;
    }

    /**
     * Creates a new host socket for the invoker program. It does this by
     * finding the first open port available. From there, if successful in
     * creating the port, then the created server is returned. If it fails
     * to be created, then <code>null</code> is returned.
     *
     * @param project The project to open a server for.
     * @return The newly created server if available.
     */

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

    /**
     * Creates a new {@link org.obicere.cc.util.MethodInvocationProtocol}
     * for the given server. This waits for a connection to be established
     * from the invoker, then establishes the protocol.
     *
     * @param server The server to stream through.
     * @return The new protocol for streaming to the server.
     */

    public MethodInvocationProtocol createProtocol(final ServerSocket server) {
        try {
            final Socket accept = server.accept();
            return new MethodInvocationProtocol(accept, true);
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Waits for a response from the invoker. This is usually called after
     * the cases have been streamed. Once there is a next element, it can
     * be assumed that the rest of the elements are - or will be -
     * available for reading.
     *
     * @param protocol The protocol streaming to the invoker.
     * @throws IOException If the connection timed out.
     * @see #RESPONSE_WAIT
     */

    public void waitForResponse(final PrimitiveSocketProtocol protocol) throws IOException {
        final long start = System.currentTimeMillis();
        while (!protocol.hasNext()) {
            if (System.currentTimeMillis() - start > RESPONSE_WAIT) {
                throw new IOException("No response from console before response expiration.");
            }
        }
    }

    /**
     * Writes the parameters needed for invoking the method from the
     * project. This will follow a basic protocol system:
     * <pre>
     * string : method_name
     * int : return_id
     * int : return_dimension
     * int : param_count $n
     * int : param_1_id
     * int : param_1_dimension
     * int : param_2_id
     * int : param_2_dimension
     * ...
     * int : param_n_id
     * int : param_n_dimension
     * int : case_count $k
     * Object[] : case_1
     * Object[] : case_2
     * ...
     * Object[] : case_k
     * </pre>
     *
     * @param project  The project to write the parameters for invocation
     *                 for.
     * @param protocol The protocol to write the invocation instructions
     *                 for.
     * @see org.obicere.cc.util.MethodInvocationProtocol#writeInvocation(String,
     * String, Class, Class[])
     * @see org.obicere.cc.util.MethodInvocationProtocol#writeCases(org.obicere.cc.executor.Case[])
     */

    public void writeInvocationParameters(final Project project, final MethodInvocationProtocol protocol) {
        final Runner runner = project.getRunner();
        final Parameter[] params = runner.getParameters();
        final int paramLength = params.length;

        final String className = project.getName();
        final String methodName = runner.getMethodName();
        final Class<?> returnClass = runner.getReturnType();
        final Class<?>[] paramClasses = new Class<?>[paramLength];
        for (int i = 0; i < paramLength; i++) {
            paramClasses[i] = params[i].getType();
        }

        protocol.writeInvocation(className, methodName, returnClass, paramClasses);
        protocol.writeCases(runner.getCases());
    }

    /**
     * Retrieve the results from the protocol. If this fails, then an
     * {@link java.io.IOException} is thrown. When this happens, it is
     * suggested to attempt to read for errors. If any are present, then
     * such errors may lead to resolving the issue.
     * <p>
     * This will ensure that the results are indeed of correct length. In
     * case they are not, it is assumed there is an issue with the stream.
     * This is since any invoker should at minimum return an array of just
     * <code>null</code> values.
     *
     * @param cases    The cases of the current invocation.
     * @param protocol The protocol streaming to the invoker.
     * @return The result from the invocation.
     * @throws IOException
     */

    public Result[] getResults(final Case[] cases, final PrimitiveSocketProtocol protocol) throws IOException {
        final int length = cases.length;

        try {
            final Object[] response = protocol.readArray(Object[].class);

            if (response == null || response.length != length) {
                throw new IOException("Invalid results returned from console.");
            }

            final Result[] results = new Result[length];
            for (int i = 0; i < length; i++) {
                results[i] = new Result(response[i], cases[i]);
            }
            return results;
        } catch (final InputMismatchException e) {
            e.printStackTrace();
            throw new IOException("Unable to parse results.");
        }
    }

    /**
     * Attempts to read an error from the protocol. If this fails, well...
     * yeah. Basically this is a last-ditch effort to figure out why
     * everything is failing.
     *
     * @param protocol The protocol that attempted to stream to the
     *                 invoker.
     * @return The error if present, otherwise an empty array.
     */

    public String[] readError(final PrimitiveSocketProtocol protocol) {
        final List<String> error = new LinkedList<>();
        while (protocol.hasString()) {
            error.add(protocol.readString());
        }
        return error.toArray(new String[error.size()]);
    }

    /**
     * Runs the invoker in a specific file. This specifies the port of the
     * open server for the invoker to connect to. The format for arguments
     * is: <code>exec -port n</code>.
     * <p>
     * Where <code>exec</code> is the file the executor is attempting to
     * run, and <code>n</code> is the port number of the open server to
     * stream between.
     *
     * @param executor The executor from the language specification.
     * @param exec     The file of the invoker.
     * @param port     The port of the server.
     * @return The result of running the invocation program.
     */

    public String[] runProcess(final CommandExecutor executor, final File exec, final int port) {

        // The format for args should be like so:
        // C:/Test/script.java -port 550000
        final String[] args = new String[2];
        args[0] = "-port";
        args[1] = String.valueOf(port);
        return executor.process(exec, (Object[]) args);
    }
}
