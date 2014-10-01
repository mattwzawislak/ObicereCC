package org.obicere.cc.methods.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class BasicProtocol implements Flushable, AutoCloseable {

    private final StreamConsumer streamConsumer = new StreamConsumer();

    private final DataOutputStream output;
    private final DataInputStream  input;

    private final Socket socket;

    private final boolean autoFlush;

    public BasicProtocol(final Socket socket) throws IOException {
        this(socket, false);
    }

    public BasicProtocol(final Socket socket, final boolean autoFlush) throws IOException {
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
        streamConsumer.write(b);
        doFlush();
    }

    public void write(final byte b) {
        streamConsumer.write(b);
        doFlush();
    }

    public void write(final short s) {
        streamConsumer.write(s);
        doFlush();
    }

    public void write(final char c) {
        streamConsumer.write(c);
        doFlush();
    }

    public void write(final int i) {
        streamConsumer.write(i);
        doFlush();
    }

    public void write(final float f) {
        streamConsumer.write(f);
        doFlush();
    }

    public void write(final long l) {
        streamConsumer.write(l);
        doFlush();
    }

    public void write(final double d) {
        streamConsumer.write(d);
        doFlush();
    }

    public void write(final String s) {
        streamConsumer.write(s);
        doFlush();
    }

    public void write(final Object[] data) {
        streamConsumer.write(data);
        doFlush();
    }

    public void write(final boolean[] data) {
        streamConsumer.write(data);
        doFlush();
    }

    public void write(final byte[] data) {
        streamConsumer.write(data);
        doFlush();
    }

    public void write(final short[] data) {
        streamConsumer.write(data);
        doFlush();
    }

    public void write(final char[] data) {
        streamConsumer.write(data);
        doFlush();
    }

    public void write(final int[] data) {
        streamConsumer.write(data);
        doFlush();
    }

    public void write(final float[] data) {
        streamConsumer.write(data);
        doFlush();
    }


    public void write(final long[] data) {
        streamConsumer.write(data);
        doFlush();
    }

    public void write(final double[] data) {
        streamConsumer.write(data);
        doFlush();
    }

    private void waitForInput() {
        while (!streamConsumer.hasNext()) {
            read();
        }
    }

    public boolean readBoolean() {
        waitForInput();
        return streamConsumer.readBoolean();
    }

    public byte readByte() {
        waitForInput();
        return streamConsumer.readByte();
    }

    public short readShort() {
        waitForInput();
        return streamConsumer.readShort();
    }

    public char readChar() {
        waitForInput();
        return streamConsumer.readChar();
    }

    public int readInt() {
        waitForInput();
        return streamConsumer.readInt();
    }

    public float readFloat() {
        waitForInput();
        return streamConsumer.readFloat();
    }

    public long readLong() {
        waitForInput();
        return streamConsumer.readLong();
    }

    public double readDouble() {
        waitForInput();
        return streamConsumer.readDouble();
    }

    public String readString() {
        waitForInput();
        return streamConsumer.readString();
    }

    public <T> T readArray(final Class<T> cls) {
        waitForInput();
        return streamConsumer.readArray(cls);
    }

    private void read() {
        try {
            streamConsumer.readAvailable(input);
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
            streamConsumer.writeAvailable(output);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        input.close();
        output.close();
        socket.close();
    }
}
