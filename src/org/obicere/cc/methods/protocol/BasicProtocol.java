package org.obicere.cc.methods.protocol;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import org.obicere.cc.gui.settings.IntegerSetting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * @author Obicere
 */
public class BasicProtocol {

    public static void main(final String[] args) throws IOException {
        final ServerSocket socket = new ServerSocket(50066);
        final Socket clientSocket = socket.accept();
        final BasicProtocol protocol = new BasicProtocol();
        final ByteConsumer consumer = protocol.getConsumer();

        while (true) {
            try {
                try {
                    Thread.sleep(1000);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                final String send = "Ping";
                System.out.println(send);
                consumer.write(send);
                protocol.writeTo(clientSocket.getOutputStream());

                clientSocket.getOutputStream().flush();
                try {
                    final String receive = consumer.readString();
                    System.out.println(receive);
                } catch (final InvalidProtocolException e) {
                    System.out.println("No response...");
                }
            } catch (final Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    private final ByteConsumer consumer = new ByteConsumer();

    public ByteConsumer getConsumer() {
        return consumer;
    }

    public synchronized void readFrom(final InputStream stream) {
        try {
            final byte[] buffer = new byte[1024];
            while (stream.read(buffer) != -1) {
                consumer.streamInput(buffer);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeTo(final OutputStream stream) {
        try {
            stream.write(consumer.toOutputArray());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}
