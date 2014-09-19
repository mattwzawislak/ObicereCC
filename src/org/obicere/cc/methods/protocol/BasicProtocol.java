package org.obicere.cc.methods.protocol;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Obicere
 */
public class BasicProtocol {

    private final Consumer consumer = new Consumer();

    private final DataOutputStream output;
    private final DataInputStream  input;

    public BasicProtocol(final Socket socket) throws IOException {
        Objects.requireNonNull(socket);
        if (socket.isClosed() || !socket.isConnected()) {
            throw new SocketException("Socket is not open.");
        }
        this.output = new DataOutputStream(socket.getOutputStream());
        this.input = new DataInputStream(socket.getInputStream());
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

            final BasicProtocol protocol = new BasicProtocol(connection);

            System.out.println("Starting to write");
            int i = 0;
            while (connection.isConnected()) {
                System.out.println("Server: " + i);
                protocol.write(i++);
                Thread.sleep(500);
            }
        } else {
            final int socket = Integer.parseInt(args[0]);
            final Socket connection = new Socket("127.0.0.1", socket);

            final BasicProtocol protocol = new BasicProtocol(connection);

            System.out.println("Starting to read");
            while (connection.isConnected()) {
                System.out.println("Next int: " + protocol.readInt());
            }
            System.out.println("Done");
        }
    }

    public void write(final boolean b) {
        consumer.write(b);
    }

    public void write(final byte b) {
        consumer.write(b);
    }

    public void write(final short s) {
        consumer.write(s);
    }

    public void write(final char c) {
        consumer.write(c);
    }

    public void write(final int i) {
        consumer.write(i);
    }

    public void write(final float f) {
        consumer.write(f);
    }

    public void write(final long l) {
        consumer.write(l);
    }

    public void write(final double d) {
        consumer.write(d);
    }

    public void write(final String s) {
        consumer.write(s);
    }

    public int readInt() {
        while (!consumer.hasNext()) {
            read();
        }
        return consumer.readInt();
    }

    private void read() {
        try {
            consumer.readAvailable(input);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void flush() {
        try {
            consumer.writeAvailable(output);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
