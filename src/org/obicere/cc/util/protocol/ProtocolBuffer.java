package org.obicere.cc.util.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A simplistic buffer backed by a byte array. This has been created to
 * allow streaming by chunks of bytes. Once streamed, these bytes can be
 * recycled - if needed - by the user and free up memory without closing
 * the stream. The user can also specify the rate at which the buffer grows
 * and its initial size.
 * <p>
 * The maximum size for this buffer is the largest power of two available
 * as an index. Since <code>2<sup>31</sup></code> overflows, this value is
 * <code>2<sup>30</sup></code>. This will allocate <code>1,073,741,824
 * bytes</code> or <code>1GiB</code>.
 * <p>
 * The choice to use a power of two was decided in-case an extension wishes
 * to use modular indexing, allowing faster performance without
 * compromising integrity.
 * <p>
 * By default each buffer will have a default growth size of
 * <code>2.0</code> times. The initial size is <code>32 bytes</code>.
 * <p>
 * Therefore by default the size of the buffer will follow the powers of
 * two, until capping at the absolute maximum.
 * <p>
 * The largest difference between this buffer and the {@link
 * java.nio.ByteBuffer} is the capabilities with streams. The indices for
 * reading and writing to a stream are stored and update automatically, so
 * partial information can be written without error and partial deletion
 * can happen. This allows the caller to make the most efficient way of
 * streaming data dependent on their needs.
 *
 * @author Obicere
 */
public class ProtocolBuffer {

    /**
     * The maximum size for the buffer. This values corresponds to the
     * largest positive power of two available in indexing.
     * <p>
     * The maximum size corresponds to: <code>1,073,741,824 bytes
     * (1GiB)</code>.
     * <p>
     * This was chosen as certain buffers may want modular implementations,
     * so a consistent maximum size will reduce the potential for error.
     */
    protected static final int MAXIMUM_BUFFER_SIZE = 1 << 30;

    /**
     * Default growth value. This should ideally reacht the {@link
     * org.obicere.cc.util.protocol.ProtocolBuffer#MAXIMUM_BUFFER_SIZE}
     * when the initial size is a power of two. However with floating point
     * precision, this may not happen immediately.
     */

    protected static final float DEFAULT_GROWTH = 2.0f;

    /**
     * Default initial value. This will allocate 4 words to the buffer
     * automatically. This is also set to a power of two for modular
     * extensions.
     */

    protected static final int DEFAULT_SIZE = 32;

    private int lastWriteIndex = 0;

    private int lastReadIndex = 0;

    private int lastStreamWriteIndex = 0;

    private final float growth;

    private byte[] buffer;

    /**
     * Constructs a new protocol buffer using the default values. This will
     * therefore comply with modular extensions.
     */

    protected ProtocolBuffer() {
        this.buffer = new byte[DEFAULT_SIZE];
        this.growth = DEFAULT_GROWTH;
    }

    /**
     * Constructs a new protocol buffer with the given initial length.
     * Increasing this value can increase performance when a large amount
     * of data is being added. This value is the number of bytes to buffer
     * for. It is recommended to use a power of two.
     *
     * @param initialLength The initial size of the buffer, in bytes, to
     *                      allocate.
     * @throws java.lang.IllegalArgumentException if the initial buffer
     *                                            size is not positive.
     *                                            This means no sizing can
     *                                            be applied.
     */

    protected ProtocolBuffer(final int initialLength) {
        if (initialLength <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be > 0; given size: " + initialLength);
        }
        this.buffer = new byte[initialLength];
        this.growth = DEFAULT_GROWTH;
    }

    /**
     * Constructs a new protocol buffer with the given initial length and
     * growth ratio. Increasing the initial length will allow more,
     * smaller, packets to be added with increased performance. Increasing
     * the growth will allow larger packets to be added with increased
     * performance. If a very large amount of information is being added,
     * increasing both will therefore be very effective.
     * <p>
     * It is recommended to use a power of two for the initial length and
     * an integer growth value. However <code>float</code> values are
     * allowed, to allow growth rates of <code>50%</code>,
     * <code>25%</code>, and whatnot.
     *
     * @param initialLength The initial size of the buffer, in bytes, to
     *                      allocate.
     * @param growth        The rate at which the buffer grows.
     * @throws java.lang.IllegalArgumentException if the initial buffer
     *                                            size is not positive or
     *                                            the growth ratio is not
     *                                            greater than one. This
     *                                            means no sizing can be
     *                                            applied.
     */

    protected ProtocolBuffer(final int initialLength, final float growth) {
        if (initialLength <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be > 0; given size: " + initialLength);
        }
        if (growth <= 1.0) {
            throw new IllegalArgumentException("Growth ratio must be greater than 1; given growth: " + growth);
        }
        this.buffer = new byte[initialLength];
        this.growth = DEFAULT_GROWTH;
    }

    /**
     * Reads the next <code>byte</code> from the buffer. If the entire
     * stream is consumed, then an exception will be thrown. Otherwise the
     * value will be immediately returned. This will not check to see if
     * the current index has actually been written to yet.
     *
     * @return The next byte, if available in the buffer.
     * @throws java.util.NoSuchElementException if there is no next element
     *                                          available.
     */

    public byte read() {
        if (lastReadIndex >= size()) {
            throw new NoSuchElementException("No more data available.");
        }
        return buffer[lastReadIndex++];
    }

    /**
     * Peeks the next <code>byte</code> from the buffer. This will not
     * throw an error if the entire stream is consumed. However it will not
     * increment the read counter, so this cannot be to 'safely' read from
     * the buffer.
     * <p>
     * This by default will return <code>0</code> if there is no next
     * byte.
     *
     * @return The next byte, if available in the buffer. Otherwise,
     * <code>0</code>.
     */

    public byte peek() {
        if (lastReadIndex >= lastWriteIndex) {
            return 0; // No identifier should match this
        }
        return buffer[lastReadIndex];
    }

    /**
     * Writes the given <code>byte</code> to the buffer. This will ensure
     * the capacity of a single <code>byte</code> for the buffer. Note that
     * this is not efficient for a large set of data. For the primitive
     * values, this will work fine. However, with strings, arrays or other
     * data types of a long buffer: this may reduce performance. This is
     * because this will only check for the allocation of a single byte at
     * a time.
     * <p>
     * Should a large amount of data be available to write, then the
     * <code>byte</code> array write method is preferred.
     *
     * @param value The <code>byte</code> to write to the stream.
     * @throws java.lang.OutOfMemoryError if the current stream has no more
     *                                    room available.
     * @see org.obicere.cc.util.protocol.ProtocolBuffer#write(byte[], int,
     * int)
     */

    public void write(final byte value) {
        checkWriteSize(1);
        if (lastWriteIndex >= size()) {
            throw new OutOfMemoryError("No more buffer space available.");
        }
        buffer[lastWriteIndex++] = value;
    }

    /**
     * Writes the given <code>byte</code> array to the buffer. This will
     * only check the capacity once, opposed to the single-byte check
     * performed by {@link org.obicere.cc.util.protocol.ProtocolBuffer#write(byte)}.
     * However, there is a larger constant-time performance cost associated
     * to this method. This is because indices need to be checked before
     * any writing can be done. ensure that the
     * <p>
     * This particular method is meant for intermediate buffers, connecting
     * a stream to this buffer. Therefore the standard pattern of providing
     * the <code>offset</code> and the <code>length</code> of where the
     * content is within the buffer is specified.
     *
     * @param values The intermediate buffer to write to this buffer.
     * @param offset The offset for the content in the intermediate
     *               buffer.
     * @param length The length of the content in the intermediate buffer.
     * @throws java.lang.IndexOutOfBoundsException if <code>offset >=
     *                                             length</code> or <code>offset
     *                                             + length > values.length</code>.
     * @throws java.lang.IllegalArgumentException  if <code>offset <
     *                                             0</code> or <code>length
     *                                             < 0</code>.
     */

    public void write(final byte[] values, final int offset, final int length) {
        Objects.requireNonNull(values);
        if (length == 0 || values.length == 0) {
            return;
        }
        if (length < 0) {
            throw new IllegalArgumentException("Length to write cannot be negative.");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Offset for the writing index cannot be negative.");
        }
        if (offset >= values.length) {
            throw new IndexOutOfBoundsException("Offset out of range for bytes");
        }
        final int end = offset + length;
        if (end > values.length) {
            throw new IndexOutOfBoundsException("End index for range is out of bounds for bytes.");
        }
        checkWriteSize(length);
        if (lastWriteIndex + length >= size()) {
            throw new OutOfMemoryError("No more buffer space available.");
        }
        for (int i = offset; i < end; i++) {
            buffer[lastWriteIndex++] = values[i];
        }
    }

    /**
     * Retrieves the maximum size of the buffer. This is <b>not</b> the
     * length of the amount of data written to the stream. This is however,
     * the current amount of memory in the buffer - either available or
     * written to.
     *
     * @return The maximum size of the buffer.
     */

    public int size() {
        return buffer.length;
    }

    /**
     * Requests the buffer to grow in size. This will copy the entire
     * buffer over again, so as the size increases the cost of calling this
     * increases. Therefore if a large amount of data ie expected, either a
     * larger initial value or a larger growth value would reduce this.
     * That way
     */

    protected void expand() {
        if (buffer.length == MAXIMUM_BUFFER_SIZE) {
            return; // No possible way to expand
        }
        int newLength = (int) Math.ceil(growth * size());
        if (newLength > MAXIMUM_BUFFER_SIZE) {
            newLength = MAXIMUM_BUFFER_SIZE;
        }
        final byte[] newBuffer = new byte[newLength];
        System.arraycopy(buffer, 0, newBuffer, 0, lastWriteIndex);
        this.buffer = newBuffer;
    }

    /**
     * Clears all the bytes that have been read. This allows memory
     * management directed by the caller. This will not be called
     * automatically, yet helper functions have been provided to signal
     * when you <i>should</i> call it.
     * <p>
     * For example, say a <code>String</code> and an <code>int</code> have
     * been written:
     * <p>
     * <code> [15, 0, 0, 0, 8, 0, 97, 0, 121, 0, 121, 0, 32, 0, 108, 0,
     * 109, 0, 97, 0, 111, 7, 0, 0, -80, 11, 0, 0, 0, 0, 0, 0] </code>
     * <p>
     * Then, the string is read. The state of the buffer does not change.
     * <p>
     * Say however, that the caller does not require the buffer to still
     * store that string and therefore it can be recycled. Then the caller
     * can request the buffer gets cleared.
     * <p>
     * Upon doing so, the new buffer would look like:
     * <p>
     * <code>[7, 0, 0, -80, 11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
     * 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]</code>
     * <p>
     * Which preserves the <code>int</code> and shifts the counters. The
     * exact same state of the buffer would have been achieved here as if
     * the string was never written and therefore never read. This
     * therefore provides a pure recycling procedure and removes the
     * bottleneck of the maximum buffer size enforced by the calling
     * protocol - there will always be one.
     */

    protected void clearReadBuffer() {
        final int lastRead = lastReadIndex;
        final int lastWrite = lastWriteIndex;
        if (lastRead == 0) {
            // No content read yet.
            return;
        }
        final int newContent = lastWrite - lastRead;
        final byte[] newBuffer = new byte[buffer.length]; // create a new buffer to avoid continuous clear
        System.arraycopy(buffer, lastRead, newBuffer, 0, newContent);
        this.lastReadIndex = 0; // Reset last read
        this.lastWriteIndex = newContent;
        this.buffer = newBuffer;
    }

    /**
     * Reads all the available bytes from the given stream into the buffer.
     * This will increment the write counter. This will also comply with
     * the {@link java.io.InputStream#available()} contract. This method
     * should therefore be used for telling when more information is
     * available.
     * <p>
     * Due to the nature of streams, this will not wait for bytes to become
     * available. It is required that the caller specifies whether to wait
     * or to continue regular execution - regardless of the state of the
     * stream.
     *
     * @param stream The stream to read the data from.
     * @throws IOException Should an error occur in the reading of the
     *                     input stream.
     */

    protected void readAvailable(final InputStream stream) throws IOException {
        final int available = stream.available();
        if (available != 0) {
            final byte[] buffer = new byte[available];
            final int read = stream.read(buffer);
            write(buffer, 0, read);
        }
    }

    /**
     * Writes all the available data - that hasn't already been written -
     * to the stream. Once this has been called, all data that is written
     * is flagged for deletion. Upon calling deletion, these bytes will be
     * removed and the counters partially reset.
     * <p>
     * Using this combined with the proper handles for memory management
     * will boycott the bottlenecks surrounding the protocol. For example,
     * wishing to write exactly <code>256GiB</code>, the deletion must
     * occur at least once - since that would exceed the capabilities of
     * this stream. More importantly, why would you be writing
     * <code>256GiB</code> through this...
     *
     * @param stream The stream to delegate to.
     * @throws IOException Should an error occur in the writing to the
     *                     output stream.
     */

    protected void writeAvailable(final OutputStream stream) throws IOException {
        final int lastStream = lastStreamWriteIndex;
        final int lastWrite = lastWriteIndex;
        final int available = lastWrite - lastStream;

        final byte[] write = new byte[available];
        System.arraycopy(buffer, lastStreamWriteIndex, write, 0, available);
        stream.write(write);
        lastStreamWriteIndex += available;
    }

    /**
     * Checks the write size and buffers accordingly. Due to the nature of
     * the differentiating initial values and the groat factors, the growth
     * to accommodate cannot be asserted on a single operation.
     * <p>
     * This may also go over the maximum size of the buffer. If that
     * happens, an error is thrown. It is recommended to clear the buffer
     * commonly so this runs less and less - seeing as this can be a very
     * costly operation for large writes.
     *
     * @param newWrite The amount of bytes that will be written and
     *                 therefore needs to have space for.
     */

    protected void checkWriteSize(final int newWrite) {
        while (lastWriteIndex + newWrite > size()) {
            if (size() == MAXIMUM_BUFFER_SIZE) {
                throw new OutOfMemoryError("No more memory available in the buffer.");
            }
            expand();
        }
    }
}
