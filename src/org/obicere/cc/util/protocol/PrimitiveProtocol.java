package org.obicere.cc.util.protocol;

import org.obicere.cc.util.protocol.consumers.AbstractConsumer;
import org.obicere.cc.util.protocol.consumers.BooleanConsumer;
import org.obicere.cc.util.protocol.consumers.ByteConsumer;
import org.obicere.cc.util.protocol.consumers.CharConsumer;
import org.obicere.cc.util.protocol.consumers.DoubleConsumer;
import org.obicere.cc.util.protocol.consumers.FloatConsumer;
import org.obicere.cc.util.protocol.consumers.IntConsumer;
import org.obicere.cc.util.protocol.consumers.LongConsumer;
import org.obicere.cc.util.protocol.consumers.ShortConsumer;
import org.obicere.cc.util.protocol.consumers.StringConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.InputMismatchException;
import java.util.Objects;

/**
 * A binary-based, persistent reader and writer. This will function only on
 * the following types:
 * <pre>
 * <ul>
 * <li> boolean
 * <li> byte
 * <li> char
 * <li> short
 * <li> int
 * <li> float
 * <li> long
 * <li> double
 * <li> String
 * <li> Array of any of the types
 * </ul>
 * </pre>
 * <p>
 * Each of the corresponding types, except for the array type has a
 * corresponding {@link org.obicere.cc.util.protocol.consumers.AbstractConsumer}.
 * This will handle the individual implementation of how to handle reading
 * and writing.
 * <p>
 * Each consumer will also delegate to a single {@link ProtocolBuffer}
 * type, which handles the actual storage and management of data.
 * <p>
 * As of result, checking the state of individual bytes in the buffer can
 * be complicated. Since the buffer also has a limited size of 1GiB
 * (1,073,741,824 bytes), then memory management might be of concern. To
 * handle this, corresponding methods have been added to free memory:
 * {@link PrimitiveProtocol#shouldClear()} and {@link
 * PrimitiveProtocol#clearRead()}. Which check to see the size of unused
 * bytes and to clear the unused bytes respectively.
 * <p>
 * Due to the limited set of data types, the header for each type only has
 * to be 1 byte in size. This helps reduce packet size compared to other
 * serializing data types, where the header might contain more data than
 * the actual type.
 * <p>
 * Unfortunately, the <code>boolean</code> types have not been optimized,
 * so each instance will take a full <code>byte</code> of information.
 * <p>
 * However, the <code>String</code> type has been optimized. So opposed to
 * storing the header for each <code>char</code>, this will treat each
 * element of having the same type. However, one limitation is the
 * encoding. So far, only <code>UTF-16</code> is supported on the default
 * Java charset.
 * <p>
 * All information will be stored in big-endian order. The stream is also
 * synchronized, however synchronizing is not necessarily useful. Since the
 * read/write operations hold data type and order, then reading the data in
 * a potentially mixed-order environment could lead to improper parsing.
 * <p>
 * For example, with reading and writing, the following code example can be
 * formed: <code>
 * <pre>
 * final StreamConsumer consumer = new StreamConsumer();
 * consumer.write(100);                 // write int
 * consumer.write("test");              // write String
 * consumer.write(new byte[]{1, 2, 3}); // write byte array
 * </pre>
 * </code>
 * <p>
 * After this, the byte contents will be (in hexadecimal):
 * <p>
 * <code>
 * <pre>
 * 07               // int header
 * 00 00 00 64      // int data
 * 0F               // String header
 * 00 00 00 04      // String length
 * 00 74            // 't'
 * 00 65            // 'e'
 * 00 73            // 's'
 * 00 74            // 't'
 * 10               // array header
 * 00 00 00 03      // array size
 * 02               // byte header
 * 01               // 1
 * 02               // 2
 * 03               // 3
 * </pre>
 * </code>
 * <p>
 * Which can then be retrieved as such:
 * <p>
 * <code>
 * <pre>
 * int a = consumer.readInt();
 * String str = consumer.readString();
 * byte[] b = consumer.readByteArray();
 * </pre>
 * </code>
 * <p>
 * As noted, the type of the individual components must be checked. This is
 * because the headers may be present in the contents elsewhere, so
 * automatically parsing such information might result in error. Also, it
 * would require explicit casting for each type regardless. Due to this,
 * avoiding the casting and instead providing methods for each
 * corresponding type is required.
 * <p>
 * Dear Future Self,
 * <p>
 * You probably don't like me right now, but I had to do this. Chances are
 * you are reading this because the protocol finally broke. Well, it ain't
 * getting fixed. Better start rewriting it now, because there is nothing
 * here salvageable.
 * <p>
 * Sincerely, Past Self.
 * <p>
 * P.S: While I have you, I'm also sorry I probably made you fat. Go hit
 * the gym you loser.
 *
 * @author Obicere
 */
public class PrimitiveProtocol {

    /**
     * Used to define the buffer size of an identifier flag. Revision 1.0
     * of the protocol dictates that the default identifier flag is an
     * 8-bit integer, occupying 1 <tt>byte</tt>.
     */

    private static final int IDENTIFIER_SIZE = 1;

    /**
     * The default buffer size required to store an 8-bit value, or a
     * <tt>boolean</tt>, in the protocol. This size includes the identifier
     * size.
     */
    private static final int BUFFER_SIZE_8_BIT = (1 << 0) + IDENTIFIER_SIZE;

    /**
     * The default buffer size required to store a 16-bit value, or a
     * <tt>character</tt>, in the protocol. This size includes the
     * identifier size.
     */
    private static final int BUFFER_SIZE_16_BIT = (1 << 1) + IDENTIFIER_SIZE;

    /**
     * The default buffer size required to store a 32-bit value, length of
     * an array, or a length of a <tt>String</tt>, in the protocol. This
     * size includes the identifier size.
     */
    private static final int BUFFER_SIZE_32_BIT = (1 << 2) + IDENTIFIER_SIZE;

    /**
     * The default buffer size required to store a 64-bit value in the
     * protocol. This size includes the identifier size.
     */
    private static final int BUFFER_SIZE_64_BIT = (1 << 3) + IDENTIFIER_SIZE;

    /**
     * {@link org.obicere.cc.util.protocol.consumers.BooleanConsumer}
     * instance for printing all boolean-related methods.
     */

    private final BooleanConsumer booleanC;

    /**
     * {@link org.obicere.cc.util.protocol.consumers.ByteConsumer} instance
     * for printing all byte-related methods.
     */

    private final ByteConsumer byteC;

    /**
     * {@link org.obicere.cc.util.protocol.consumers.ShortConsumer}
     * instance for printing all short-related methods.
     */
    private final ShortConsumer shortC;

    /**
     * {@link org.obicere.cc.util.protocol.consumers.ShortConsumer}
     * instance for printing all short-related methods.
     */
    private final CharConsumer charC;

    /**
     * {@link org.obicere.cc.util.protocol.consumers.IntConsumer} instance
     * for printing all int-related methods.
     */
    private final IntConsumer intC;

    /**
     * {@link org.obicere.cc.util.protocol.consumers.FloatConsumer}
     * instance for printing all float-related methods.
     */
    private final FloatConsumer floatC;

    /**
     * {@link org.obicere.cc.util.protocol.consumers.LongConsumer} instance
     * for printing all long-related methods.
     */
    private final LongConsumer longC;

    /**
     * {@link org.obicere.cc.util.protocol.consumers.DoubleConsumer}
     * instance for printing all double-related methods.
     */
    private final DoubleConsumer doubleC;

    /**
     * {@link org.obicere.cc.util.protocol.consumers.StringConsumer}
     * instance for printing all {@link java.lang.String}-related methods.
     */
    private final StringConsumer stringC;

    /**
     * The storage of all objects written to the stream. All reading and
     * writing will be done through the appropriate {@link
     * org.obicere.cc.util.protocol.consumers.AbstractConsumer} and the
     * methods.provided in the {@link ProtocolBuffer} instance.
     */
    private final ProtocolBuffer buffer;

    /**
     * Maximum size of a string available for input. If the length of the
     * string exceeds this value, either another more suitable data
     * structure, a buffered system or a shorter string is in order. This
     * is defined as a 'magical constant', where this is the maximum size
     * of the buffer (1GiB) with room for an identifier.
     * <p>
     * Therefore, this value is then equal to the amount of bytes
     * available:
     * <p>
     * <code>1024 * 1024 * 1024 = 1 <<<< 30</code>
     * <p>
     * Divided by the bytes per character:
     * <p>
     * <code>(1 <<<< 30) / 2 = (1 <<<< 29)</code>
     * <p>
     * With room for an identifier and 1 extra byte left over.
     */

    private static final int MAX_STRING_LENGTH = (1 << 29) - 1;

    /**
     * Constructs a new <tt>StreamConsumer</tt> with the default parameters
     * assigned to the basis {@link ProtocolBuffer}.
     *
     * @see ProtocolBuffer#DEFAULT_SIZE
     * @see ProtocolBuffer#DEFAULT_GROWTH
     */
    public PrimitiveProtocol() {
        this(ProtocolBuffer.DEFAULT_SIZE, ProtocolBuffer.DEFAULT_GROWTH);
    }

    /**
     * Constructs a new <tt>StreamConsumer</tt> with the allocated length.
     * This can help reduce the running time of the writing it a large
     * amount of items are being written to the {@link ProtocolBuffer}
     * instance. The growth will remain default.
     *
     * @param initialLength The initial length of the buffer. <tt>0 <
     *                      initialLength</tt>
     * @see ProtocolBuffer#DEFAULT_GROWTH
     */

    public PrimitiveProtocol(final int initialLength) {
        this(initialLength, ProtocolBuffer.DEFAULT_GROWTH);
    }

    /**
     * Constructs a new <tt>StreamConsumer</tt> with the allocated length
     * and the specified growth ratio. Setting higher values can help
     * allocate memory more effectively, resulting in a faster write speed
     * to the buffer.
     *
     * @param initialLength The initial length of the buffer. <tt>0 <
     *                      initialLength</tt>
     * @param growth        The growth ratio of the buffer. <tt>0 <
     *                      growth</tt>
     */

    public PrimitiveProtocol(final int initialLength, final float growth) {
        this.buffer = new ProtocolBuffer(initialLength, growth);

        this.booleanC = new BooleanConsumer(buffer);
        this.byteC = new ByteConsumer(buffer);
        this.shortC = new ShortConsumer(buffer);
        this.charC = new CharConsumer(buffer);
        this.intC = new IntConsumer(buffer);
        this.floatC = new FloatConsumer(buffer);
        this.longC = new LongConsumer(buffer);
        this.doubleC = new DoubleConsumer(buffer);
        this.stringC = new StringConsumer(buffer);
    }

    /**
     * Frees up the bytes in the {@link ProtocolBuffer} that have already
     * been read by the consumer. This will not happen automatically, so
     * the buffer may overflow if not cleared. Note that for the buffer to
     * fill, 1GiB of data must be written to it, so this is unlikely.
     * <p>
     * This method will also not resize the array, so the growth is
     * effectively finalized. An example is this as follows:
     * <pre>
     * <tt>Given a buffer M of length n, we have:
     *
     * M = {m<sub>1</sub>, m<sub>2</sub>,... m<sub>n</sub>}
     *
     * Let r be the last index read by the consumer. Given that 0 <= r <
     * n,
     * we have the
     * following buffer:
     *
     * M = {m<sub>1</sub>, m<sub>2</sub>,... m<sub>r</sub>,...
     * m<sub>n</sub>}
     *
     * Upon clearing the read, M will equal the following:
     *
     * M = {m<sub>r</sub>,... m<sub>n</sub>, 0, 0,... 0}
     *
     * With length n.
     *
     * Given M = {a, b, c, d}, r = 1; after clearing:
     *
     * M = {b, c, d, 0}
     * </tt>
     * </pre>
     *
     * @see #shouldClear()
     */
    public synchronized void clearRead() {
        buffer.clearReadBuffer();
    }

    /**
     * Checks whether or not the {@link ProtocolBuffer} should be cleared
     * to free up memory. This is done by checking to see if the buffer's
     * current length is equal to the {@link ProtocolBuffer#MAXIMUM_BUFFER_SIZE
     * maximum buffer size}. This method does not check if the amount of
     * items written requires a clear. So with a single item written, with
     * a minimal initial size and a very large growth factor: this method
     * may flag the buffer as eligible for a clear.
     * <p>
     * This method merely works as just a recommendation and is perfectly
     * suited for the default arguments for the buffer.
     *
     * @return <code>true</code> if the buffer should clear.
     * @see ProtocolBuffer#MAXIMUM_BUFFER_SIZE
     * @see ProtocolBuffer#size()
     */

    public synchronized boolean shouldClear() {
        return buffer.size() == ProtocolBuffer.MAXIMUM_BUFFER_SIZE;
    }

    /**
     * Writes all the data, sequentially, to the given {@link
     * java.io.OutputStream}; given that the data has not yet been written
     * to the stream. This does not clash with default reading from the
     * consumer. Reading a value from the consumer means it will still
     * remain eligible for the writing here, and vice-versa.
     *
     * @param stream The stream to write all available data to the given
     *               <tt>stream</tt>.
     * @throws IOException If the stream has been closed, is full or the
     *                     {@link ProtocolBuffer} failed to write to the
     *                     stream for any reason. The full specifications
     *                     for what can throw this error is dependent on
     *                     the {@link java.io.OutputStream}'s
     *                     implementation
     * @see java.io.OutputStream#write(byte[])
     * @see ProtocolBuffer#writeAvailable(java.io.OutputStream)
     */

    public synchronized void writeAvailable(final OutputStream stream) throws IOException {
        buffer.writeAvailable(stream);
    }

    /**
     * Reads all the data from the given {@link java.io.InputStream} to the
     * {@link ProtocolBuffer}. This is particularly useful when creating a
     * pipe-system, as the data values read are not checked for their
     * validity. So even though any IO system can connect to the
     * <tt>StreamConsumer</tt>, it is recommended to only connect another
     * <tt>StreamConsumer</tt> for this reason.
     *
     * @param stream The stream to read all data - even if not part of the
     *               valid protocol.
     * @throws IOException If the stream has been closed. The best
     *                     specification on what can throw this error is
     *                     dependent on the {@link java.io.InputStream}'s
     *                     implementation.
     * @see java.io.InputStream#read(byte[])
     * @see ProtocolBuffer#readAvailable(java.io.InputStream)
     */

    public synchronized void readAvailable(final InputStream stream) throws IOException {
        buffer.readAvailable(stream);
    }

    /**
     * Writes the given identifier to the stream - to signal the start of a
     * new object. This method is effectively the same as {@link
     * #write(byte)}  at this time, but has been implemented in case the
     * identifier specifications change.
     *
     * @param identifier The identifier to write to signal a new object.
     */

    protected void writeIdentifier(final int identifier) {
        buffer.write((byte) identifier);
    }

    protected void writeLength(final int length) {
        intC.writeRaw(length);
    }

    /**
     * Writes a <code>boolean</code> to the {@link ProtocolBuffer} signaled
     * first by the identifier, then the value.
     *
     * @param value The value to write.
     * @see org.obicere.cc.util.protocol.consumers.BooleanConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_BOOLEAN
     */

    public synchronized void write(final boolean value) {
        booleanC.write(value);
    }

    /**
     * Writes a <code>byte</code> to the {@link ProtocolBuffer} signaled
     * first by the identifier, then the value.
     *
     * @param value The value to write.
     * @see org.obicere.cc.util.protocol.consumers.ByteConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_BYTE
     */

    public synchronized void write(final byte value) {
        byteC.write(value);
    }

    /**
     * Writes a <code>char</code> to the {@link ProtocolBuffer} signaled
     * first by the identifier, then the value spread across 2 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.util.protocol.consumers.CharConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_CHAR
     */

    public synchronized void write(final char value) {
        charC.write(value);
    }

    /**
     * Writes a <code>short</code> to the {@link ProtocolBuffer} signaled
     * first by the identifier, then the value spread across 2 bytes. This
     * is effectively equal to the {@link #write(char)} method, but is
     * merely provided to avoid casting.
     *
     * @param value The value to write.
     * @see org.obicere.cc.util.protocol.consumers.ShortConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_SHORT
     */

    public synchronized void write(final short value) {
        shortC.write(value);
    }

    /**
     * Writes a <code>int</code> to the {@link ProtocolBuffer} signaled
     * first by the identifier, then the value spread across 4 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.util.protocol.consumers.IntConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_INT
     */

    public synchronized void write(final int value) {
        intC.write(value);
    }

    /**
     * Writes a <code>long</code> to the {@link ProtocolBuffer} signaled
     * first by the identifier, then the value spread across 8 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.util.protocol.consumers.LongConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_LONG
     */

    public synchronized void write(final long value) {
        longC.write(value);
    }

    /**
     * Writes a <code>float</code> to the {@link ProtocolBuffer} signaled
     * first by the identifier, then the value spread across 4 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.util.protocol.consumers.FloatConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_FLOAT
     */

    public synchronized void write(final float value) {
        floatC.write(value);
    }

    /**
     * Writes a <code>double</code> to the {@link ProtocolBuffer} signaled
     * first by the identifier, then the value spread across 8 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.util.protocol.consumers.DoubleConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_DOUBLE
     */

    public synchronized void write(final double value) {
        doubleC.write(value);
    }

    /**
     * Writes a <code>java.lang.String</code> of length <tt>n</tt> to the
     * {@link ProtocolBuffer} signaled first by the identifier, then the
     * length (4 bytes), followed by <tt>n</tt> chars each with a
     * byte-length of 2 bytes - as the identifiers are excluded. This
     * results in <tt>2n + 5</tt> bytes written to the stream.
     * <p>
     * Support for writing <code>null</code> strings is not supported.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException if the value of the string is
     *                                        <code>null</code>. Null
     *                                        values are not supported, and
     *                                        instead should be replaced
     *                                        with the string representation
     *                                        or the empty string.
     * @throws java.lang.OutOfMemoryError     if the given string's length
     *                                        exceeds that of {@link
     *                                        org.obicere.cc.util.protocol.PrimitiveProtocol#MAX_STRING_LENGTH}
     * @see org.obicere.cc.util.protocol.consumers.StringConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_STRING
     */

    public synchronized void write(final String value) {
        Objects.requireNonNull(value);
        final int length = value.length();
        if (length > MAX_STRING_LENGTH) {
            throw new OutOfMemoryError("Not enough available memory for this string of length: " + length);
        }
        stringC.write(value);
    }

    /**
     * Writes a <code>boolean</code> array of length <code>n</code> to the
     * {@link ProtocolBuffer} signaled first by the identifier, then the
     * length (4 bytes), followed by the <code>n</code> elements. Headers
     * are excluded.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException if the given array is
     *                                        <code>null</code>.
     * @see org.obicere.cc.util.protocol.consumers.BooleanConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_BOOLEAN
     */

    public synchronized void write(final boolean[] value) {
        booleanC.write(value);
    }

    /**
     * Writes a <code>byte</code> array of length <code>n</code> to the
     * {@link ProtocolBuffer} signaled first by the identifier, then the
     * length (4 bytes), followed by the <code>n</code> elements. Headers
     * are excluded.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException if the given array is
     *                                        <code>null</code>.
     * @see org.obicere.cc.util.protocol.consumers.ByteConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_BYTE
     */

    public synchronized void write(final byte[] value) {
        byteC.write(value);
    }

    /**
     * Writes a <code>char</code> array of length <code>n</code> to the
     * {@link ProtocolBuffer} signaled first by the identifier, then the
     * length (4 bytes), followed by the <code>n</code> elements. Headers
     * are excluded.
     * <p>
     * This will only write characters in the UTF-16 encoding.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException if the given array is
     *                                        <code>null</code>.
     * @see org.obicere.cc.util.protocol.consumers.CharConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_CHAR
     */

    public synchronized void write(final char[] value) {
        charC.write(value);
    }

    /**
     * Writes a <code>short</code> array of length <code>n</code> to the
     * {@link ProtocolBuffer} signaled first by the identifier, then the
     * length (4 bytes), followed by the <code>n</code> elements. Headers
     * are excluded.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException if the given array is
     *                                        <code>null</code>.
     * @see org.obicere.cc.util.protocol.consumers.ShortConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_SHORT
     */

    public synchronized void write(final short[] value) {
        shortC.write(value);
    }

    /**
     * Writes a <code>int</code> array of length <code>n</code> to the
     * {@link ProtocolBuffer} signaled first by the identifier, then the
     * length (4 bytes), followed by the <code>n</code> elements. Headers
     * are excluded.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException if the given array is
     *                                        <code>null</code>.
     * @see org.obicere.cc.util.protocol.consumers.IntConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_INT
     */

    public synchronized void write(final int[] value) {
        intC.write(value);
    }

    /**
     * Writes a <code>long</code> array of length <code>n</code> to the
     * {@link ProtocolBuffer} signaled first by the identifier, then the
     * length (4 bytes), followed by the <code>n</code> elements. Headers
     * are excluded.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException if the given array is
     *                                        <code>null</code>.
     * @see org.obicere.cc.util.protocol.consumers.LongConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_LONG
     */

    public synchronized void write(final long[] value) {
        longC.write(value);
    }

    /**
     * Writes a <code>float</code> array of length <code>n</code> to the
     * {@link ProtocolBuffer} signaled first by the identifier, then the
     * length (4 bytes), followed by the <code>n</code> elements. Headers
     * are excluded.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException if the given array is
     *                                        <code>null</code>.
     * @see org.obicere.cc.util.protocol.consumers.FloatConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_FLOAT
     */

    public synchronized void write(final float[] value) {
        floatC.write(value);
    }

    /**
     * Writes a <code>double</code> array of length <code>n</code> to the
     * {@link ProtocolBuffer} signaled first by the identifier, then the
     * length (4 bytes), followed by the <code>n</code> elements. Headers
     * are excluded.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException if the given array is
     *                                        <code>null</code>.
     * @see org.obicere.cc.util.protocol.consumers.DoubleConsumer
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_DOUBLE
     */

    public synchronized void write(final double[] value) {
        doubleC.write(value);
    }

    /**
     * Reads the next byte as an identifier. This retrieve an unsigned
     * integer as the identifier, so equality will remain between the
     * identifiers read from there and the identifiers constants.
     *
     * @return The corresponding identifier.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */

    private synchronized int nextIdentifier() {
        return Byte.toUnsignedInt(buffer.read());
    }

    /**
     * Reads a single <code>boolean</code> from the buffer. This is not
     * identified, so a booleans will not be packed together, so a whole
     * <code>byte</code> is needed.
     *
     * @return The corresponding boolean.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */

    public synchronized boolean readBoolean() {
        return booleanC.read();
    }

    /**
     * Reads a single signed <code>byte</code> from the buffer.
     *
     * @return The corresponding byte.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */

    public synchronized byte readByte() {
        return byteC.read();
    }

    /**
     * Reads a single signed <code>short</code> from the buffer.
     *
     * @return The corresponding short.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */

    public synchronized short readShort() {
        return shortC.read();
    }

    /**
     * Reads a single signed <code>char</code> from the buffer.
     *
     * @return The corresponding short.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */
    public synchronized char readChar() {
        return charC.read();
    }

    /**
     * Reads a single signed <code>int</code> from the buffer.
     *
     * @return The corresponding int.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */

    public synchronized int readInt() {
        return intC.read();
    }

    /**
     * Reads a single signed <code>long</code> from the buffer.
     *
     * @return The corresponding long.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */
    public synchronized long readLong() {
        return longC.read();
    }

    /**
     * Reads a single signed <code>float</code> from the buffer.
     *
     * @return The corresponding float.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */
    public synchronized float readFloat() {
        return floatC.read();
    }

    /**
     * Reads a single signed <code>double</code> from the buffer.
     *
     * @return The corresponding double.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */
    public synchronized double readDouble() {
        return doubleC.read();
    }

    /**
     * Reads a single {@link java.lang.String} from the buffer.
     *
     * @return The corresponding <code>String</code>.
     * @throws java.lang.IndexOutOfBoundsException if the stream has been
     *                                             fully read.
     */
    public synchronized String readString() {
        return stringC.read();
    }

    /**
     * Reads a primitive <code>boolean</code> array from the buffer. This
     * is effectively the same as calling {@link PrimitiveProtocol#readArray(Class)}
     * with the <code>boolean[].class</code> argument passed.
     *
     * @return The read boolean array.
     */

    public synchronized boolean[] readBooleanArray() {
        return booleanC.readArray();
    }

    /**
     * Reads a primitive <code>byte</code> array from the buffer. This is
     * effectively the same as calling {@link PrimitiveProtocol#readArray(Class)}
     * with the <code>byte[].class</code> argument passed.
     *
     * @return The read byte array.
     */

    public synchronized byte[] readByteArray() {
        return byteC.readArray();
    }

    /**
     * Reads a primitive <code>short</code> array from the buffer. This is
     * effectively the same as calling {@link PrimitiveProtocol#readArray(Class)}
     * with the <code>short[].class</code> argument passed.
     *
     * @return The read short array.
     */

    public synchronized short[] readShortArray() {
        return shortC.readArray();
    }

    /**
     * Reads a primitive <code>char</code> array from the buffer. This is
     * effectively the same as calling {@link PrimitiveProtocol#readArray(Class)}
     * with the <code>char[].class</code> argument passed.
     *
     * @return The read char array.
     */

    public synchronized char[] readCharArray() {
        return charC.readArray();
    }

    /**
     * Reads a primitive <code>int</code> array from the buffer. This is
     * effectively the same as calling {@link PrimitiveProtocol#readArray(Class)}
     * with the <code>int[].class</code> argument passed.
     *
     * @return The read int array.
     */

    public synchronized int[] readIntArray() {
        return intC.readArray();
    }

    /**
     * Reads a primitive <code>float</code> array from the buffer. This is
     * effectively the same as calling {@link PrimitiveProtocol#readArray(Class)}
     * with the <code>float[].class</code> argument passed.
     *
     * @return The read float array.
     */

    public synchronized float[] readFloatArray() {
        return floatC.readArray();
    }

    /**
     * Reads a primitive <code>long</code> array from the buffer. This is
     * effectively the same as calling {@link PrimitiveProtocol#readArray(Class)}
     * with the <code>long[].class</code> argument passed.
     *
     * @return The read long array.
     */

    public synchronized long[] readLongArray() {
        return longC.readArray();
    }

    /**
     * Reads a primitive <code>boolean</code> array from the buffer. This
     * is effectively the same as calling {@link PrimitiveProtocol#readArray(Class)}
     * with the <code>boolean[].class</code> argument passed.
     *
     * @return The read boolean array.
     */

    public synchronized double[] readDoubleArray() {
        return doubleC.readArray();
    }

    /**
     * Checks to see if there is a next available identifier in the buffer.
     * This can be used when buffering from a stream to ensure that errors
     * are not thrown when data is not yet available. Data should only be
     * written in whole chunks, so a single identifier <i>should</i> mark
     * the presence of a following object. This checks the next available
     * identifier to accomplish this task. The way this is done is by
     * ensuring the next identifier, if there is one available is not equal
     * to <code>0</code>. No identifier should match this preset value.
     *
     * @return <code>true</code> if and only if there is an identifier
     * ready in the buffer.
     * @see PrimitiveSocketProtocol
     */

    public synchronized boolean hasNext() {
        return buffer.peek() != 0;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>boolean</code>. This does not guarantee the existence of the
     * <code>boolean</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>boolean</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_BOOLEAN
     */

    public boolean hasBoolean() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_BOOLEAN;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>byte</code>. This does not guarantee the existence of the
     * <code>byte</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>byte</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_BYTE
     */

    public boolean hasByte() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_BYTE;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>short</code>. This does not guarantee the existence of the
     * <code>short</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>short</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_SHORT
     */

    public boolean hasShort() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_SHORT;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>char</code>. This does not guarantee the existence of the
     * <code>char</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>char</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_CHAR
     */

    public boolean hasChar() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_CHAR;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>int</code>. This does not guarantee the existence of the
     * <code>int</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>int</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_INT
     */

    public boolean hasInt() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_INT;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>float</code>. This does not guarantee the existence of the
     * <code>float</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>float</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_FLOAT
     */

    public boolean hasFloat() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_FLOAT;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>long</code>. This does not guarantee the existence of the
     * <code>long</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>long</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_LONG
     */

    public boolean hasLong() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_LONG;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>double</code>. This does not guarantee the existence of the
     * <code>double</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>double</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_DOUBLE
     */

    public boolean hasDouble() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_DOUBLE;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>String</code>. This does not guarantee the existence of the
     * <code>String</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>String</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_STRING
     */

    public boolean hasString() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_STRING;
    }

    /**
     * Checks to see if the next identifier signifies the presence of a
     * <code>array</code>. This does not guarantee the existence of the
     * <code>array</code> data. This will not move forward the reading
     * index.
     *
     * @return <code>true</code> if and only if the next identifier is a
     * <code>array</code> identifier.
     * @see org.obicere.cc.util.protocol.consumers.AbstractConsumer#IDENTIFIER_ARRAY
     */

    public boolean hasArray() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_ARRAY;
    }

    /**
     * Allows writing of {@link Object} data types, including arrays,
     * {@link java.lang.String}s and wrapper classes. Should the type be
     * generic, an {@link java.lang.Object} type can be used, even though
     * it is not supported by this protocol. As long as each component
     * contained in the array is supported.
     * <p>
     * For example, say we have a class <code>Foo</code>:
     * <p>
     * <code>
     * <pre>
     * public class Foo {
     *     int num;
     *     String value;
     *
     *     public int getNum(){
     *         return num;
     *     }
     *
     *     public String getValue(){
     *         return value;
     *     }
     * }
     * </pre>
     * </code>
     * <p>
     * This protocol will <b>not</b> support the writing of this object,
     * however it will support the fields within <code>Foo</code>.
     * Therefore those individual fields can be passed representing
     * <code>Foo</code>:
     * <p>
     * <code>
     * <pre>
     * Foo foo = ...;
     * PrimitiveProtocol protocol = ...;
     *
     * protocol.write(new Object[]{foo.getNum(), foo.getValue()});
     * </pre>
     * </code>
     * <p>
     * This allows persistence of objects through this protocol, so long as
     * they can easily be represented through the supported data types.
     * However, all serializing must be handled by the caller and the
     * receiver.
     * <p>
     * That being said, the format for writing an array in this case
     * deviates from the standard primitive approach. Whereas in the
     * primitive arrays the type is asserted, there is no assertion about
     * the type here. Therefore each element must specify its own type,
     * increasing buffer size.
     * <p>
     * Therefore, if the size of a buffer is an issue, usage of the
     * primitive types is preferred. Since using the wrapper classes will
     * use this method and therefore pack extra data.
     *
     * @param value The array to write.
     * @param <T>   The component type of the array to write. This can be
     *              arrays, {@link String}s or a wrapper class. All
     *              primitive array writing should be done through the
     *              respective methods.
     * @throws java.lang.NullPointerException if the given array is
     *                                        <code>null</code> or elements
     *                                        within the array are
     *                                        <code>null</code>. This
     *                                        protocol does not support
     *                                        <code>null</code> types.
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#write(boolean[])
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#write(byte[])
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#write(short[])
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#write(char[])
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#write(int[])
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#write(float[])
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#write(long[])
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#write(double[])
     */

    public synchronized <T> void write(final T[] value) {
        Objects.requireNonNull(value);
        final int length = value.length;
        writeIdentifier(AbstractConsumer.IDENTIFIER_ARRAY);
        intC.writeRaw(length);
        for (final Object t : value) {
            final Class<?> cls = t.getClass();
            final int identifier = identifierFor(t.getClass());
            final int size = sizeFor(cls);
            if (identifier == -1 || size == -1) {
                throw new IllegalArgumentException("Non-supported class: " + cls);
            }
            writeIdentifier(identifier);
            writeFor(cls, t);
        }
    }

    /**
     * Attempts to read an array from the buffer by the specified type. The
     * specification of the type is useful for two purposes:
     * <pre>
     * 1) Allows the primitive array types to handle the optimized arrays.
     * 2) Casting by the caller will already be specified, this handles
     *    the cast instead and can assert type safety.
     * </pre>
     * Point <code>1)</code> also highlights a key component here about the
     * type safety of primitive arrays. Since processing is handled off to
     * them for this operation, the casting to the array wrapper classes
     * will not work. For example, <code>int[]</code> is not directly
     * assignable to <code>Integer[]</code>. And also due to the nature of
     * Java generics, the array type can't just be used for the primitive
     * types, since the component type needs to also be checked for nested
     * arrays. This would also lead to an issue with the primitive cases.
     * <p>
     * Lastly, point <code>2)</code> is just for the sake of simplicity. It
     * is the same reason the Java {@link java.util.List} allows specifying
     * the type of the array to remove the cast. Passing the type checking
     * here ensures that type checking need not be present in every usage.
     * <p>
     * Say we have <code>int[][]</code> array that was written to the
     * protocol. To access this array, one must call this method - since
     * <code>int[].class</code> is directly assignable from
     * <code>Object</code>. <code>
     * <pre>
     * PrimitiveProtocol protocol = ...;
     *
     * int[][] array = protocol.readArray(int[][].class);
     * </pre>
     * </code>
     * <p>
     * Note that the type is required, so that type safety can be assured
     * and to avoid the cast: <code>
     * <pre>
     * PrimitiveProtocol protocol = ...;
     *
     * {@literal @}SuppressWarnings("unchecked")
     * int[][] array = (int[][]) protocol.readArray();
     * </pre>
     * </code>
     * <p>
     * For the sake of simplicity, the first example was the chosen path.
     *
     * @param cls The class instance of the array type to read. This will
     *            be used for checking the component type and performing
     *            proper casting.
     * @param <T> The array type to read. This must be of type array. This
     *            will support the primitive array types as well as the
     *            wrapper, <code>String</code> and array types.
     * @param <S> The component class of type <code>T</code>.
     * @return The read array of type <code>T</code>.
     * @throws java.lang.IllegalArgumentException if <code>T</code> is not
     *                                            an array type.
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#readBooleanArray()
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#readByteArray()
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#readShortArray()
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#readCharArray()
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#readIntArray()
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#readFloatArray()
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#readLongArray()
     * @see org.obicere.cc.util.protocol.PrimitiveProtocol#readDoubleArray()
     */

    @SuppressWarnings("unchecked")
    // This is all checked - not really though
    public synchronized <T, S> T readArray(final Class<T> cls) {
        if (!hasArray()) {
            throw new InputMismatchException("The next identifier is not for an array.");
        }
        nextIdentifier(); // array identifier.
        final Class<S> component = (Class<S>) cls.getComponentType();
        if (component == null) {
            throw new IllegalArgumentException("Type class must be an array.");
        }
        if (component.isPrimitive()) {
            switch (component.getCanonicalName()) {
                case "boolean":
                    return (T) booleanC.readRawArray();
                case "byte":
                    return (T) byteC.readRawArray();
                case "short":
                    return (T) shortC.readRawArray();
                case "char":
                    return (T) charC.readRawArray();
                case "int":
                    return (T) intC.readRawArray();
                case "float":
                    return (T) floatC.readRawArray();
                case "long":
                    return (T) longC.readRawArray();
                case "double":
                    return (T) doubleC.readRawArray();
            }
        }
        final int length = intC.readRaw();
        final S[] array = (S[]) Array.newInstance(component, length);
        for (int i = 0; i < length; i++) {
            array[i] = (S) readMethodFor(component, nextIdentifier()); // Peek the next identifier
        }
        // T <-> S[]
        return (T) array;
    }

    /**
     * Provides the read method for the given class and the given
     * identifier. The class is only needed during the operation of nested
     * arrays. This is because the type of array needs to be referenced for
     * a recursive load.
     * <p>
     * This assumes the identifier has already been read, since it is
     * present in the method parameters. Therefore the raw methods will be
     * used for parsing an array. This includes the array and type
     * methods.
     *
     * @param component The component of the object - used only during an
     *                  array read.
     * @param id        The identifier of the object to read. This should
     *                  have already been read.
     * @param <S>       The type of the object to read. Should
     *                  <code>S</code> be an array, then a recursive load
     *                  is needed and therefore the type is needed.
     * @return The read object based off of the identifier.
     */

    private <S> Object readMethodFor(final Class<S> component, final int id) {
        switch (id) {
            case AbstractConsumer.IDENTIFIER_BOOLEAN:
                return booleanC.readRaw();
            case AbstractConsumer.IDENTIFIER_BYTE:
                return byteC.readRaw();
            case AbstractConsumer.IDENTIFIER_SHORT:
                return shortC.readRaw();
            case AbstractConsumer.IDENTIFIER_CHAR:
                return charC.readRaw();
            case AbstractConsumer.IDENTIFIER_INT:
                return intC.readRaw();
            case AbstractConsumer.IDENTIFIER_FLOAT:
                return floatC.readRaw();
            case AbstractConsumer.IDENTIFIER_LONG:
                return longC.readRaw();
            case AbstractConsumer.IDENTIFIER_DOUBLE:
                return doubleC.readRaw();
            case AbstractConsumer.IDENTIFIER_STRING:
                return stringC.readRaw();
            case AbstractConsumer.IDENTIFIER_ARRAY:
                return readArray(component);
            default:
                throw new IllegalArgumentException("Unsupported identifier: " + id);
        }
    }

    /**
     * Writes the specific object to the protocol. This should ideally only
     * be used on arrays and as of <code>v1.0</code> is. But, support for
     * the primitive types has already been added due to simplicity.
     * <p>
     * This will handle both arrays and standard types. This will delegate
     * to the according write method. Either for the primitive types or the
     * array types, as both can be accessed during the recursive writing of
     * an array.
     * <p>
     * The actual definition of the class is not needed, but just examined.
     * This is used to decide whether or not additional array calls are
     * needed or we are just writing an object. Ideally the non-array
     * methods will not be accessed here, but are added in the case of
     * future changes or an unexpected fall-through.
     *
     * @param cls The class of the object to write.
     * @param obj The object to write.
     * @throws java.lang.AssertionError       if the given primitive array
     *                                        type or primitive was
     *                                        incorrectly parsed or failed
     *                                        to write.
     * @throws java.lang.NullPointerException if <code>obj</code> is
     *                                        <code>null</code> or the
     *                                        given <code>cls</code> is
     *                                        <code>null</code>.
     */

    private void writeFor(final Class<?> cls, final Object obj) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(obj);
        if (cls.isArray()) {
            final Class<?> component = cls.getComponentType();
            if (component.isPrimitive()) {
                switch (component.getCanonicalName()) {
                    case "boolean":
                        write((boolean[]) obj);
                        return;
                    case "byte":
                        write((byte[]) obj);
                        return;
                    case "short":
                        write((short[]) obj);
                        return;
                    case "char":
                        write((char[]) obj);
                        return;
                    case "int":
                        write((int[]) obj);
                        return;
                    case "float":
                        write((float[]) obj);
                        return;
                    case "long":
                        write((long[]) obj);
                        return;
                    case "double":
                        write((double[]) obj);
                        return;
                    default:
                        throw new AssertionError("Failed to properly write the primitive array.");
                }
            } else {
                write((Object[]) obj);
            }
        } else {
            switch (cls.getCanonicalName()) {
                case "boolean":
                case "java.lang.Boolean":
                    booleanC.write((boolean) obj);
                    return;

                case "byte":
                case "java.lang.Byte":
                    byteC.write((byte) obj);
                    return;

                case "short":
                case "java.lang.Short":
                    shortC.write((short) obj);

                case "java.lang.Character":
                case "char":
                    charC.write((char) obj);
                    return;

                case "int":
                case "java.lang.Integer":
                    intC.write((int) obj);
                    return;

                case "long":
                case "java.lang.Long":
                    longC.write((long) obj);
                    return;

                case "float":
                case "java.lang.Float":
                    floatC.write((float) obj);
                    return;

                case "double":
                case "java.lang.Double":
                    doubleC.write((double) obj);
                    return;

                case "java.lang.String":
                    stringC.write((String) obj);
                    return;
                default:
                    throw new AssertionError("Could not write object for class: " + cls);
            }
        }
    }

    protected int componentIdentifier(final Class<?> cls) {
        Class<?> comp = cls;
        Class<?> tmp;
        while ((tmp = comp.getComponentType()) != null) {
            comp = tmp;
        }
        return identifierFor(comp);
    }

    protected int identifierFor(final Class<?> cls) {
        Objects.requireNonNull(cls);
        if (cls.isArray()) {
            return AbstractConsumer.IDENTIFIER_ARRAY;
        }
        switch (cls.getCanonicalName()) {
            case "boolean":
            case "java.lang.Boolean":
                return AbstractConsumer.IDENTIFIER_BOOLEAN;

            case "byte":
            case "java.lang.Byte":
                return AbstractConsumer.IDENTIFIER_BYTE;

            case "short":
            case "java.lang.Short":
                return AbstractConsumer.IDENTIFIER_SHORT;

            case "char":
            case "java.lang.Character":
                return AbstractConsumer.IDENTIFIER_CHAR;

            case "int":
            case "java.lang.Integer":
                return AbstractConsumer.IDENTIFIER_INT;

            case "long":
            case "java.lang.Long":
                return AbstractConsumer.IDENTIFIER_LONG;

            case "float":
            case "java.lang.Float":
                return AbstractConsumer.IDENTIFIER_FLOAT;

            case "double":
            case "java.lang.Double":
                return AbstractConsumer.IDENTIFIER_DOUBLE;

            case "java.lang.String":
                return AbstractConsumer.IDENTIFIER_STRING;
            default:
                return -1; // Unsupported type
        }
    }

    // This will only return header size. Additional memory may need to be allocated
    private int sizeFor(final Class<?> cls) {
        Objects.requireNonNull(cls);
        if (cls.isArray()) {
            return BUFFER_SIZE_32_BIT + 1; // Allocate length (32-bit int) and array type (8-bit int)
        }
        switch (cls.getCanonicalName()) {
            case "boolean":
            case "byte":
            case "java.lang.Boolean": // Sorry, no optimizations yet
            case "java.lang.Byte":
                return BUFFER_SIZE_8_BIT;

            case "short":
            case "char":
            case "java.lang.Short":
            case "java.lang.Character":
                return BUFFER_SIZE_16_BIT;

            case "int":
            case "float":
            case "java.lang.String": // Technically an array - record the length
            case "java.lang.Integer":
            case "java.lang.Float":
                return BUFFER_SIZE_32_BIT;

            case "long":
            case "double":
            case "java.lang.Long":
            case "java.lang.Double":
                return BUFFER_SIZE_64_BIT;
            default:
                return -1; // Unsupported type
        }
    }

}