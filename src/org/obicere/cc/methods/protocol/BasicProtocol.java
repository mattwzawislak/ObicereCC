package org.obicere.cc.methods.protocol;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Obicere
 */
public class BasicProtocol implements Flushable {

    private final Consumer consumer = new Consumer();

    private final DataOutputStream output;
    private final DataInputStream  input;

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
        this.autoFlush = autoFlush;
    }

    public static void main(final String... args) throws Exception {
        final boolean write = args.length == 0;

        if (write) {
            final ServerSocket socket = new ServerSocket(500);
            new Thread(() -> {
                try {
                    main("" + 500);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }).start();
            final Socket connection = socket.accept();

            final BasicProtocol protocol = new BasicProtocol(connection, true);

            System.out.println("Starting to write");
            protocol.write("Test");
            protocol.write(100);
            protocol.write(new Object[]{1, 2, 3, "Finished"});
        } else {
            final int socket = Integer.parseInt(args[0]);
            final Socket connection = new Socket("127.0.0.1", socket);

            final BasicProtocol protocol = new BasicProtocol(connection);

            System.out.println("Starting to read");
            System.out.println(protocol.readString());
            System.out.println(protocol.readInt());
            System.out.println(Arrays.toString(protocol.readArray(Object[].class)));
            System.out.println("Done");
        }
    }

    public void write(final boolean b) {
        consumer.write(b);
        doFlush();
    }

    public void write(final byte b) {
        consumer.write(b);
        doFlush();
    }

    public void write(final short s) {
        consumer.write(s);
        doFlush();
    }

    public void write(final char c) {
        consumer.write(c);
        doFlush();
    }

    public void write(final int i) {
        consumer.write(i);
        doFlush();
    }

    public void write(final float f) {
        consumer.write(f);
        doFlush();
    }

    public void write(final long l) {
        consumer.write(l);
        doFlush();
    }

    public void write(final double d) {
        consumer.write(d);
        doFlush();
    }

    public void write(final String s) {
        consumer.write(s);
        doFlush();
    }

    public void write(final Object[] data) {
        consumer.write(data);
        doFlush();
    }

    public void write(final boolean[] data) {
        consumer.write(data);
        doFlush();
    }

    public void write(final byte[] data) {
        consumer.write(data);
        doFlush();
    }

    public void write(final short[] data) {
        consumer.write(data);
        doFlush();
    }

    public void write(final char[] data) {
        consumer.write(data);
        doFlush();
    }

    public void write(final int[] data) {
        consumer.write(data);
        doFlush();
    }

    public void write(final float[] data) {
        consumer.write(data);
        doFlush();
    }


    public void write(final long[] data) {
        consumer.write(data);
        doFlush();
    }

    public void write(final double[] data) {
        consumer.write(data);
        doFlush();
    }

    private void waitForInput() {
        while (!consumer.hasNext()) {
            read();
        }
    }

    public boolean readBoolean() {
        waitForInput();
        return consumer.readBoolean();
    }

    public byte readByte() {
        waitForInput();
        return consumer.readByte();
    }

    public short readShort() {
        waitForInput();
        return consumer.readShort();
    }

    public char readChar() {
        waitForInput();
        return consumer.readChar();
    }

    public int readInt() {
        waitForInput();
        return consumer.readInt();
    }

    public float readFloat() {
        waitForInput();
        return consumer.readFloat();
    }

    public long readLong() {
        waitForInput();
        return consumer.readLong();
    }

    public double readDouble() {
        waitForInput();
        return consumer.readDouble();
    }

    public String readString() {
        waitForInput();
        return consumer.readString();
    }

    public <T> T readArray(final Class<T> cls) {
        waitForInput();
        return consumer.readArray(cls);
    }

    private void read() {
        try {
            consumer.readAvailable(input);
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
            consumer.writeAvailable(output);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
