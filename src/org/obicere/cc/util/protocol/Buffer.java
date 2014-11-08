package org.obicere.cc.util.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Obicere
 */
public class Buffer {

    /**
     * The maximum size for the buffer. This values corresponds to the largest positive power of two
     * available in indexing.
     * <p>
     * The maximum size corresponds to: <tt>134,217,728 bytes (128MB)</tt>.
     */
    protected static final int MAXIMUM_BUFFER_SIZE = 1 << 30;

    protected static final float DEFAULT_GROWTH = 2.0f;

    protected static final int DEFAULT_SIZE = 32;

    private int lastWriteIndex = 0;

    private int lastReadIndex = 0;

    private int lastStreamWriteIndex = 0;

    private final float growth;

    private byte[] buffer;

    protected Buffer() {
        this.buffer = new byte[DEFAULT_SIZE];
        this.growth = DEFAULT_GROWTH;
    }

    protected Buffer(final int initialLength) {
        if (initialLength <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be > 0; given size: " + initialLength);
        }
        this.buffer = new byte[initialLength];
        this.growth = DEFAULT_GROWTH;
    }

    protected Buffer(final int initialLength, final float growth) {
        if (initialLength <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be > 0; given size: " + initialLength);
        }
        if (growth <= 1.0) {
            throw new IllegalArgumentException("Growth ratio must be greater than 1; given growth: " + growth);
        }
        this.buffer = new byte[initialLength];
        this.growth = DEFAULT_GROWTH;
    }

    public byte read() {
        if (lastReadIndex >= length()) {
            throw new IndexOutOfBoundsException("No more data available.");
        }
        return buffer[lastReadIndex++];
    }

    public byte peek() {
        if (lastReadIndex >= lastWriteIndex) {
            return 0; // No identifier should match this
        }
        return buffer[lastReadIndex];
    }

    public void write(final byte value) {
        checkWriteSize(1);
        if (lastWriteIndex >= length()) {
            throw new OutOfMemoryError("No more buffer space available.");
        }
        buffer[lastWriteIndex++] = value;
    }

    public int length() {
        return buffer.length;
    }

    protected void expand() {
        int newLength = (int) (growth * length());
        if (newLength > MAXIMUM_BUFFER_SIZE) {
            newLength = MAXIMUM_BUFFER_SIZE;
        }
        final byte[] newBuffer = new byte[newLength];
        System.arraycopy(buffer, 0, newBuffer, 0, lastWriteIndex);
        this.buffer = newBuffer;
    }

    protected void clearReadBuffer() {
        final int lastRead = lastReadIndex;
        final int lastWrite = lastWriteIndex;
        if (lastRead == 0) {
            // No content read yet.
            return;
        }
        final int newContent = lastWrite - lastRead;
        final byte[] newBuffer = new byte[newContent * 2]; // create a new buffer to avoid continuous clear
        System.arraycopy(buffer, lastRead, newBuffer, 0, newContent);
        this.lastReadIndex = 0; // Reset last read
        this.lastWriteIndex = newContent;
        this.buffer = newBuffer;
    }

    protected void readAvailable(final InputStream stream) throws IOException {
        final int available = stream.available();
        if (available != 0) {
            final byte[] buffer = new byte[available];
            stream.read(buffer);
            for (final byte b : buffer) {
                write(b);
            }
        }
    }

    protected void writeAvailable(final OutputStream stream) throws IOException {
        final int lastStream = lastStreamWriteIndex;
        final int lastWrite = lastWriteIndex;
        final int available = lastWrite - lastStream;

        final byte[] write = new byte[available];
        System.arraycopy(buffer, lastStreamWriteIndex, write, 0, available);
        stream.write(write);
        lastStreamWriteIndex += available;
    }

    protected void checkWriteSize(final int newWrite) {
        while (lastWriteIndex + newWrite > length()) {
            expand();
        }
    }
}
