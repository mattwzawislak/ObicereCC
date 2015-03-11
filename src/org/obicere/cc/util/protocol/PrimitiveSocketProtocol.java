package org.obicere.cc.util.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class PrimitiveSocketProtocol implements Flushable, AutoCloseable {

    protected final PrimitiveProtocol protocol = new PrimitiveProtocol();

    private final DataOutputStream output;
    private final DataInputStream  input;

    private final Socket socket;

    private final boolean autoFlush;

    public PrimitiveSocketProtocol(final Socket socket) throws IOException {
        this(socket, false);
    }

    public PrimitiveSocketProtocol(final Socket socket, final boolean autoFlush) throws IOException {
        Objects.requireNonNull(socket);
        if (socket.isClosed() || !socket.isConnected()) {
            throw new SocketException("Socket is not open.");
        }
        this.output = new DataOutputStream(socket.getOutputStream());
        this.input = new DataInputStream(socket.getInputStream());
        this.socket = socket;
        this.autoFlush = autoFlush;
    }

    public void write(final boolean b) {
        protocol.write(b);
        doFlush();
    }

    public void write(final byte b) {
        protocol.write(b);
        doFlush();
    }

    public void write(final short s) {
        protocol.write(s);
        doFlush();
    }

    public void write(final char c) {
        protocol.write(c);
        doFlush();
    }

    public void write(final int i) {
        protocol.write(i);
        doFlush();
    }

    public void write(final float f) {
        protocol.write(f);
        doFlush();
    }

    public void write(final long l) {
        protocol.write(l);
        doFlush();
    }

    public void write(final double d) {
        protocol.write(d);
        doFlush();
    }

    public void write(final String s) {
        protocol.write(s);
        doFlush();
    }

    public void write(final Object[] data) {
        protocol.write(data);
        doFlush();
    }

    public void write(final boolean[] data) {
        protocol.write(data);
        doFlush();
    }

    public void write(final byte[] data) {
        protocol.write(data);
        doFlush();
    }

    public void write(final short[] data) {
        protocol.write(data);
        doFlush();
    }

    public void write(final char[] data) {
        protocol.write(data);
        doFlush();
    }

    public void write(final int[] data) {
        protocol.write(data);
        doFlush();
    }

    public void write(final float[] data) {
        protocol.write(data);
        doFlush();
    }


    public void write(final long[] data) {
        protocol.write(data);
        doFlush();
    }

    public void write(final double[] data) {
        protocol.write(data);
        doFlush();
    }

    private void waitForInput() {
        while (!protocol.hasNext()) {
            read();
        }
    }

    public boolean readBoolean() {
        waitForInput();
        return protocol.readBoolean();
    }

    public byte readByte() {
        waitForInput();
        return protocol.readByte();
    }

    public short readShort() {
        waitForInput();
        return protocol.readShort();
    }

    public char readChar() {
        waitForInput();
        return protocol.readChar();
    }

    public int readInt() {
        waitForInput();
        return protocol.readInt();
    }

    public float readFloat() {
        waitForInput();
        return protocol.readFloat();
    }

    public long readLong() {
        waitForInput();
        return protocol.readLong();
    }

    public double readDouble() {
        waitForInput();
        return protocol.readDouble();
    }

    public String readString() {
        waitForInput();
        return protocol.readString();
    }

    public <T> T readArray(final Class<T> cls) {
        waitForInput();
        return protocol.readArray(cls);
    }

    public boolean hasBoolean() {
        return protocol.hasBoolean();
    }

    public boolean hasByte() {
        return protocol.hasByte();
    }

    public boolean hasShort() {
        return protocol.hasShort();
    }

    public boolean hasChar() {
        return protocol.hasChar();
    }

    public boolean hasInt() {
        return protocol.hasInt();
    }

    public boolean hasFloat() {
        return protocol.hasFloat();
    }

    public boolean hasLong() {
        return protocol.hasLong();
    }

    public boolean hasDouble() {
        return protocol.hasDouble();
    }

    public boolean hasString() {
        return protocol.hasString();
    }

    public boolean hasArray() {
        return protocol.hasArray();
    }

    public boolean hasNext() {
        return protocol.hasNext();
    }

    private void read() {
        try {
            protocol.readAvailable(input);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void doFlush() {
        if (autoFlush) {
            flush();
        }
    }

    @Override
    public void flush() {
        try {
            protocol.writeAvailable(output);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
        output.close();
        socket.close();
    }
}
