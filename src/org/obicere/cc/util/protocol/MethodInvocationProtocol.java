package org.obicere.cc.util.protocol;

import org.obicere.cc.executor.Case;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

/**
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
 * @author Obicere
 */
public class MethodInvocationProtocol extends PrimitiveSocketProtocol {

    public MethodInvocationProtocol(final Socket socket, final boolean autoFlush) throws IOException {
        super(socket, autoFlush);
    }

    public MethodInvocationProtocol(final Socket socket) throws IOException {
        super(socket);
    }

    public void writeInvocation(final String methodName, final Class<?> returnClass, final Class<?>... paramClasses) {
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(returnClass);
        Objects.requireNonNull(paramClasses);
        protocol.write(methodName);
        writeClass(returnClass);
        for (final Class<?> param : paramClasses) {
            writeClass(param);
        }
    }

    private void writeClass(final Class<?> cls) {
        final int id = protocol.identifierFor(cls);
        if (id == -1) {
            throw new IllegalArgumentException("No valid identifier for class: " + cls + " found.");
        }
        protocol.writeIdentifier(id);
        protocol.writeIdentifier(dimension(cls));
    }

    public void writeCases(final Case[] cases) {
        Objects.requireNonNull(cases);
        final int length = cases.length;
        protocol.writeLength(length);
        for (final Case c : cases) {
            protocol.write(c.getParameters());
        }
    }

    private int dimension(final Class<?> cls) {
        int i = 0;
        Class<?> comp = cls;
        while ((comp = comp.getComponentType()) != null) {
            i++;
        }
        return i;
    }
}
