package org.obicere.cc.methods;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

public class IOUtils {

    private static final int APPLICATION_PORT_MIN = 55000;
    private static final int APPLICATION_PORT_MAX = 60000;

    private IOUtils() {
    }

    public static byte[] download(final URL url) throws IOException {
        Objects.requireNonNull(url);
        return readData(url.openStream());
    }

    public static byte[] readData(final File file) throws IOException {
        Objects.requireNonNull(file);
        return readData(new FileInputStream(file));
    }

    private static byte[] readData(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] data = new byte[1024];
        int read;
        while ((read = in.read(data, 0, 1024)) != -1) {
            out.write(data, 0, read);
        }
        in.close();
        return out.toByteArray();
    }

    public static void write(final File file, final byte[] data) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(data);
        final FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.close();
    }

    public static void readProperties(final Properties properties, final File file) throws IOException {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(file);
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Could not read from properties file: " + file);
        }
        final InputStream input = new FileInputStream(file);
        if (file.canRead()) {
            properties.load(input);
        }
    }

    public static void writeProperties(final Properties properties, final File file) throws IOException {
        if (file.exists() && !file.canWrite()) {
            throw new IOException("Could not write to properties file: " + file);
        }
        final FileOutputStream stream = new FileOutputStream(file);
        properties.store(stream, null);
        stream.flush();
        stream.close();
    }

    public static int findOpenPort() {
        return findOpenPort(APPLICATION_PORT_MIN, APPLICATION_PORT_MAX);
    }

    public static int findOpenPort(final int min, final int max) {
        if (min > max) {
            return findOpenPort(max, min);
        }
        for (int i = min; i <= max; i++) {
            try {
                final ServerSocket socket = new ServerSocket(i);
                socket.close();
                return i;
            } catch (final IOException ignored) {
            }
        }
        return -1;
    }

}
