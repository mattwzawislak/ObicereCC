package org.obicere.cc.util;

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

/**
 * Basic I/O tools for easily handling reading and writing from files or
 * streams. This also provides the functionality of reading in properties
 * from a file, or exporting properties to a file.
 * <p>
 * The tools themselves are used primarily to simplify reading and writing.
 * Common patterns are implemented here that relate to I/O. This, opposed
 * to having these methods defined across the application with the chance
 * of variance.
 */

public class IOUtils {

    private static final int APPLICATION_PORT_MIN = 55000;
    private static final int APPLICATION_PORT_MAX = 60000;

    private static final int KILOBYTE = 1024;

    private IOUtils() {
        throw new AssertionError("u avin' a giggle m8");
    }

    /**
     * Retrieves the <code>byte[]</code> contents at the given {@link
     * java.net.URL}.
     * <p>
     * This is done in chunks of 1,024 bytes (1 kilobyte). The bytes will
     * be collected and then returned at the end, as opposed to streaming.
     *
     * @param url The source to download from.
     * @return The bytes downloaded from the source.
     * @throws java.io.IOException  if opening the source, streaming from
     *                              the source or closing the source
     *                              results in an error.
     * @throws NullPointerException if the given <code>source</code> is
     *                              <code>null</code>.
     */

    public static byte[] download(final URL url) throws IOException {
        Objects.requireNonNull(url);
        return readData(url.openStream());
    }

    /**
     * Reads the <code>byte[]</code> contents of the given {@link
     * java.io.File}.
     * <p>
     * This is done in chunks of 1,024 bytes (1 kilobyte). The bytes will
     * be collected and then returned at the end, as opposed ot streaming.
     *
     * @param file The file to read.
     * @return The bytes read from the file.
     * @throws java.io.IOException  if opening the file, streaming from the
     *                              file or closing the file results in an
     *                              error.
     * @throws NullPointerException if the given <code>file</code> is
     *                              <code>null</code>.
     */

    public static byte[] readData(final File file) throws IOException {
        Objects.requireNonNull(file);
        return readData(new FileInputStream(file));
    }

    /**
     * Streams the data to a collector from the given stream. This is done
     * in chunks of 1,024 bytes.
     * <p>
     * Should an error occur, partial bytes will not be returned. This is
     * to avoid potentially corrupt streams.
     * <p>
     * The buffer is initially set to 1,024 bytes, so this method will
     * perform best with amounts of bytes close to multiples of 1,024
     * bytes. If the stream needs to be throttled, this should be used to
     * maintain efficiency and prevent possible errors.
     *
     * @param in The stream to read from.
     * @return The read bytes from the stream.
     * @throws java.io.IOException Should reading from the stream or
     *                             closing the stream result in an error.
     */

    private static byte[] readData(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(KILOBYTE);
        final byte[] data = new byte[KILOBYTE];
        int read;
        while ((read = in.read(data, 0, KILOBYTE)) != -1) {
            out.write(data, 0, read);
        }
        try {
            return out.toByteArray();
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * Writes the given content to a file. No checks are made to ensure
     * that the file write is not redundant.
     * <p>
     * Appending to the file is not possible, due to the nature of the
     * data. In the case of an image for example, appending a second image
     * to it may cause collisions with the tags.
     *
     * @param file The file to write to.
     * @param data The contents of the file to write.
     * @throws java.io.IOException
     * @throws NullPointerException if <code>file</code> or <code>data</code>
     *                              is <code>null</code>.
     */

    public static void write(final File file, final byte[] data) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(data);
        final FileOutputStream out = new FileOutputStream(file);
        out.write(data);
        out.close();
    }

    /**
     * Reads the properties configuration from the given file into the
     * {@link java.util.Properties} instance.
     * <p>
     * This allows for configuring the properties beforehand, or performing
     * multiple loads onto the same properties map. As of result, a new
     * properties map is not created each time, it is up to the caller to
     * create the properties instance.
     *
     * @param properties The map of the current properties.
     * @param file       The properties file.
     * @return The <code>properties</code> map with the data added to it.
     * @throws java.io.IOException When the <code>file</code> does not
     *                             exist, or it does exist but cannot be
     *                             read from.
     */

    public static Properties readProperties(final Properties properties, final File file) throws IOException {
        Objects.requireNonNull(properties);
        Objects.requireNonNull(file);
        if (!file.exists() || !file.canRead()) {
            throw new IOException("Could not read from properties file: " + file);
        }
        final InputStream input = new FileInputStream(file);
        if (file.canRead()) {
            properties.load(input);
        }
        return properties;
    }

    /**
     * Writes the properties to the given <code>file</code>. This will not
     * append to the file, so any content in the file pre-write will be
     * lost.
     * <p>
     * No comments will be left in terms of the properties with this
     * method.
     *
     * @param properties The properties to write.
     * @param file       The file to write the properties to.
     * @throws java.io.IOException If the <code>file</code> does not exist,
     *                             or if the <code>file</code> exists but
     *                             cannot be written to.
     */

    public static void writeProperties(final Properties properties, final File file) throws IOException {
        if (!file.exists() || !file.canWrite()) {
            throw new IOException("Could not write to properties file: " + file);
        }
        final FileOutputStream stream = new FileOutputStream(file);
        properties.store(stream, null);
        stream.flush();
        stream.close();
    }

    /**
     * Attempts to find an open port. The range for the ports to check will
     * be <code>[55000, 60000]</code>, based on {@link
     * org.obicere.cc.util.IOUtils#APPLICATION_PORT_MIN} and {@link
     * org.obicere.cc.util.IOUtils#APPLICATION_PORT_MAX}.
     *
     * @return the first open port found. If none was found,
     * <code>-1</code> is returned instead.
     * @see org.obicere.cc.util.IOUtils#APPLICATION_PORT_MIN
     * @see org.obicere.cc.util.IOUtils#APPLICATION_PORT_MAX
     * @see org.obicere.cc.util.IOUtils#findOpenPort(int, int)
     */

    public static int findOpenPort() {
        return findOpenPort(APPLICATION_PORT_MIN, APPLICATION_PORT_MAX);
    }

    /**
     * Attempts to find an open port within the given range. If a port is
     * found, it will not be reserved and its vacancy is subject to
     * change.
     * <p>
     * In the case that <code>min > max</code>, then the <code>min</code>
     * and <code>max</code> values are swapped.
     * <p>
     * Effectively:
     * <p>
     * <code>findOpenPort(max, min);</code>
     * <p>
     * This way, assertions don't need to be made about calculated port
     * ranges, and also to avoid potential error where none should occur.
     * <p>
     * Otherwise, if <code>min <= max</code>, normal operation is
     * continued.
     * <p>
     * If at any port an error occurs, the error is ignored and the port is
     * therefore also ignored. Since if a standard {@link java.net.Socket}
     * connection can't be established, a heavyweight channel won't as
     * well.
     * <p>
     * Once a port is found, the socket is closed again and not reserved.
     * This means that in a rare case, although possible, the port may be
     * reserved by a separate thread or process in between when the port is
     * marked 'open' and when you try to lock it. As of <code>v1.0</code>,
     * no fail safes have been issued to counter such a case.
     *
     * @param min The minimum port to start searching at. Bounded by
     *            <code>[0, 65535]</code>.
     * @param max The maximum port to stop searching at. Bounded by
     *            <code>[0, 65535]</code>.
     * @return The first open port, if any. If none was found:
     * <code>-1</code>.
     */

    public static int findOpenPort(final int min, final int max) {
        if (min > max) {
            return findOpenPort(max, min);
        }
        if (min < 0 || min > 65535) {
            throw new IllegalArgumentException("Minimum port value out of range for standard ports [0, 65535]: " + min);
        }
        if (min < 0 || min > 65535) {
            throw new IllegalArgumentException("Maximum port value out of range for standard ports [0, 65535]: " + max);
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
