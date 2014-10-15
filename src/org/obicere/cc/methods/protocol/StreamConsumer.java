package org.obicere.cc.methods.protocol;

import org.obicere.cc.methods.protocol.consumers.AbstractConsumer;
import org.obicere.cc.methods.protocol.consumers.BooleanConsumer;
import org.obicere.cc.methods.protocol.consumers.ByteConsumer;
import org.obicere.cc.methods.protocol.consumers.CharConsumer;
import org.obicere.cc.methods.protocol.consumers.DoubleConsumer;
import org.obicere.cc.methods.protocol.consumers.FloatConsumer;
import org.obicere.cc.methods.protocol.consumers.IntConsumer;
import org.obicere.cc.methods.protocol.consumers.LongConsumer;
import org.obicere.cc.methods.protocol.consumers.ShortConsumer;
import org.obicere.cc.methods.protocol.consumers.StringConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Objects;

/**
 * Dear Future Self,
 * <p>
 * You probably don't like me right now, but I had to do this. Chances are you are reading this
 * because the protocol finally broke. Well, it ain't getting fixed. Better start rewriting it now,
 * because there is nothing here salvageable.
 * <p>
 * Sincerely, Past Self.
 * <p>
 * P.S: While I have you, I'm also sorry I probably made you fat. Go hit the gym you loser.
 *
 * @author Obicere
 */
public class StreamConsumer {

    /**
     * Used to define the buffer size of an identifier flag. Revision 1.0 of the protocol dictates
     * that the default identifier flag is an 8-bit integer, occupying 1 <tt>byte</tt>.
     */

    private static final int IDENTIFIER_SIZE = 1;

    /**
     * The default buffer size required to store an 8-bit value, or a <tt>boolean</tt>, in the
     * protocol. This size includes the identifier size.
     */
    private static final int BUFFER_SIZE_8_BIT = (1 << 0) + IDENTIFIER_SIZE;

    /**
     * The default buffer size required to store a 16-bit value, or a <tt>character</tt>, in the
     * protocol. This size includes the identifier size.
     */
    private static final int BUFFER_SIZE_16_BIT = (1 << 1) + IDENTIFIER_SIZE;

    /**
     * The default buffer size required to store a 32-bit value, length of an array, or a length
     * of a <tt>String</tt>, in the protocol. This size includes the identifier size.
     */
    private static final int BUFFER_SIZE_32_BIT = (1 << 2) + IDENTIFIER_SIZE;

    /**
     * The default buffer size required to store a 64-bit value in the protocol. This size includes
     * the identifier size.
     */
    private static final int BUFFER_SIZE_64_BIT = (1 << 3) + IDENTIFIER_SIZE;

    /**
     * {@link org.obicere.cc.methods.protocol.consumers.BooleanConsumer} instance for printing
     * all boolean-related methods.
     */

    private final BooleanConsumer booleanC;

    /**
     * {@link org.obicere.cc.methods.protocol.consumers.ByteConsumer} instance for printing all
     * byte-related methods.
     */

    private final ByteConsumer byteC;

    /**
     * {@link org.obicere.cc.methods.protocol.consumers.ShortConsumer} instance for printing all
     * short-related methods.
     */
    private final ShortConsumer shortC;

    /**
     * {@link org.obicere.cc.methods.protocol.consumers.ShortConsumer} instance for printing all
     * short-related methods.
     */
    private final CharConsumer charC;

    /**
     * {@link org.obicere.cc.methods.protocol.consumers.IntConsumer} instance for printing all
     * int-related methods.
     */
    private final IntConsumer intC;

    /**
     * {@link org.obicere.cc.methods.protocol.consumers.FloatConsumer} instance for printing all
     * float-related methods.
     */
    private final FloatConsumer floatC;

    /**
     * {@link org.obicere.cc.methods.protocol.consumers.LongConsumer} instance for printing all
     * long-related methods.
     */
    private final LongConsumer longC;

    /**
     * {@link org.obicere.cc.methods.protocol.consumers.DoubleConsumer} instance for printing all
     * double-related methods.
     */
    private final DoubleConsumer doubleC;

    /**
     * {@link org.obicere.cc.methods.protocol.consumers.StringConsumer} instance for printing all
     * {@link java.lang.String}-related methods.
     */
    private final StringConsumer stringC;

    /**
     * The storage of all objects written to the stream. All reading and writing will be done
     * through the appropriate {@link org.obicere.cc.methods.protocol.consumers.AbstractConsumer}
     * and the methods provided in the {@link Buffer} instance.
     */
    private final Buffer buffer;

    /**
     * Constructs a new <tt>StreamConsumer</tt> with the default parameters assigned to the basis
     * {@link Buffer}.
     *
     * @see Buffer#DEFAULT_SIZE
     * @see Buffer#DEFAULT_GROWTH
     */
    protected StreamConsumer() {
        this(Buffer.DEFAULT_SIZE, Buffer.DEFAULT_GROWTH);
    }

    /**
     * Constructs a new <tt>StreamConsumer</tt> with the allocated length. This can help reduce
     * the running time of the writing it a large amount of items are being written to the
     * {@link Buffer} instance. The growth will remain default.
     *
     * @param initialLength The initial length of the buffer. <tt>0 < initialLength</tt>
     * @see Buffer#DEFAULT_GROWTH
     */

    protected StreamConsumer(final int initialLength) {
        this(initialLength, Buffer.DEFAULT_GROWTH);
    }

    /**
     * Constructs a new <tt>StreamConsumer</tt> with the allocated length and the specified growth
     * ratio. Setting higher values can help allocate memory more effectively, resulting in a faster
     * write speed to the buffer.
     *
     * @param initialLength The initial length of the buffer. <tt>0 < initialLength</tt>
     * @param growth        The growth ratio of the buffer. <tt>0 < growth</tt>
     */

    protected StreamConsumer(final int initialLength, final float growth) {
        this.buffer = new Buffer(initialLength, growth);

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
     * Frees up the bytes in the {@link Buffer} that have already been read by the consumer. This
     * will not happen automatically, so the buffer may overflow if not cleared. Note that for the
     * buffer to fill, 128MB of data must be written to it, so this is unlikely.
     * <p>
     * This method will also not resize the array, so the growth is effectively finalized. An
     * example is this as follows:
     * <pre>
     * <tt>Given a buffer M of length n, we have:
     *
     * M = {m<sub>1</sub>, m<sub>2</sub>,... m<sub>n</sub>}
     *
     * Let r be the last index read by the consumer. Given that 0 <= r < n, we have the
     * following buffer:
     *
     * M = {m<sub>1</sub>, m<sub>2</sub>,... m<sub>r</sub>,... m<sub>n</sub>}
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
     * Checks whether or not the {@link Buffer} should be cleared to free up memory. This is done by
     * checking to see if the buffer's current length is equal to the
     * {@link org.obicere.cc.methods.protocol.Buffer#MAXIMUM_BUFFER_SIZE maximum buffer size}. This
     * method does not check if the amount of items written requires a clear. So with a single item
     * written, with a minimal initial size and a very large growth factor: this method may flag the
     * buffer as eligible for a clear.
     * <p>
     * This method merely works as just a recommendation and is perfectly suited for the default
     * arguments for the buffer.
     *
     * @return <code>true</code> if the buffer should clear.
     * @see Buffer#MAXIMUM_BUFFER_SIZE
     * @see org.obicere.cc.methods.protocol.Buffer#length()
     */

    public synchronized boolean shouldClear() {
        return buffer.length() == Buffer.MAXIMUM_BUFFER_SIZE;
    }

    /**
     * Writes all the data, sequentially, to the given {@link java.io.OutputStream}; given that the
     * data has not yet been written to the stream. This does not clash with default reading from
     * the consumer. Reading a value from the consumer means it will still remain eligible for the
     * writing here, and vice-versa.
     *
     * @param stream The stream to write all available data to the given <tt>stream</tt>.
     * @throws IOException If the stream has been closed, is full or the {@link Buffer} failed to
     *                     write to the stream for any reason. The full specifications for what can
     *                     throw this error is dependent on the {@link java.io.OutputStream}'s
     *                     implementation
     * @see java.io.OutputStream#write(byte[])
     * @see Buffer#writeAvailable(java.io.OutputStream)
     */

    public synchronized void writeAvailable(final OutputStream stream) throws IOException {
        buffer.writeAvailable(stream);
    }

    /**
     * Reads all the data from the given {@link java.io.InputStream} to the {@link Buffer}. This is
     * particularly useful when creating a pipe-system, as the data values read are not checked for
     * their validity. So even though any IO system can connect to the <tt>StreamConsumer</tt>, it
     * is recommended to only connect another <tt>StreamConsumer</tt> for this reason.
     *
     * @param stream The stream to read all data - even if not part of the valid protocol.
     * @throws IOException If the stream has been closed. The best specification on what can throw
     *                     this error is dependent on the {@link java.io.InputStream}'s
     *                     implementation.
     * @see java.io.InputStream#read(byte[])
     * @see Buffer#readAvailable(java.io.InputStream)
     */

    public synchronized void readAvailable(final InputStream stream) throws IOException {
        buffer.readAvailable(stream);
    }

    /**
     * Writes the given identifier to the stream - to signal the start of a new object. This method
     * is effectively the same as {@link #write(byte)}  at this time, but has been implemented in
     * case the identifier specifications change.
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
     * Writes a <code>boolean</code> to the {@link Buffer} signaled first by the identifier, then
     * the value.
     *
     * @param value The value to write.
     * @see org.obicere.cc.methods.protocol.consumers.BooleanConsumer
     * @see org.obicere.cc.methods.protocol.consumers.AbstractConsumer#IDENTIFIER_BOOLEAN
     */

    public synchronized void write(final boolean value) {
        booleanC.write(value);
    }

    /**
     * Writes a <code>byte</code> to the {@link Buffer} signaled first by the identifier, then the
     * value.
     *
     * @param value The value to write.
     * @see org.obicere.cc.methods.protocol.consumers.ByteConsumer
     * @see org.obicere.cc.methods.protocol.consumers.AbstractConsumer#IDENTIFIER_BYTE
     */

    public synchronized void write(final byte value) {
        byteC.write(value);
    }

    /**
     * Writes a <code>char</code> to the {@link Buffer} signaled first by the identifier, then the
     * value spread across 2 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.methods.protocol.consumers.CharConsumer
     * @see org.obicere.cc.methods.protocol.consumers.AbstractConsumer#IDENTIFIER_CHAR
     */

    public synchronized void write(final char value) {
        charC.write(value);
    }

    /**
     * Writes a <code>short</code> to the {@link Buffer} signaled first by the identifier, then the
     * value spread across 2 bytes. This is effectively equal to the {@link #write(char)} method,
     * but is merely provided to avoid casting.
     *
     * @param value The value to write.
     * @see org.obicere.cc.methods.protocol.consumers.ShortConsumer
     * @see org.obicere.cc.methods.protocol.consumers.AbstractConsumer#IDENTIFIER_SHORT
     */

    public synchronized void write(final short value) {
        shortC.write(value);
    }

    /**
     * Writes a <code>int</code> to the {@link Buffer} signaled first by the identifier, then the
     * value spread across 4 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.methods.protocol.consumers.IntConsumer
     * @see org.obicere.cc.methods.protocol.consumers.AbstractConsumer#IDENTIFIER_INT
     */

    public synchronized void write(final int value) {
        intC.write(value);
    }

    /**
     * Writes a <code>long</code> to the {@link Buffer} signaled first by the identifier, then the
     * value spread across 8 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.methods.protocol.consumers.LongConsumer
     * @see org.obicere.cc.methods.protocol.consumers.AbstractConsumer#IDENTIFIER_LONG
     */

    public synchronized void write(final long value) {
        longC.write(value);
    }

    /**
     * Writes a <code>float</code> to the {@link Buffer} signaled first by the identifier, then the
     * value spread across 4 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.methods.protocol.consumers.FloatConsumer
     * @see org.obicere.cc.methods.protocol.consumers.AbstractConsumer#IDENTIFIER_FLOAT
     */

    public synchronized void write(final float value) {
        floatC.write(value);
    }

    /**
     * Writes a <code>double</code> to the {@link Buffer} signaled first by the identifier, then the
     * value spread across 8 bytes.
     *
     * @param value The value to write.
     * @see org.obicere.cc.methods.protocol.consumers.DoubleConsumer
     * @see org.obicere.cc.methods.protocol.consumers.AbstractConsumer#IDENTIFIER_DOUBLE
     */

    public synchronized void write(final double value) {
        doubleC.write(value);
    }

    /**
     * Writes a <code>java.lang.String</code> of length <tt>n</tt> to the {@link Buffer} signaled
     * first by the identifier, then the length (4 bytes), followed by <tt>n</tt> chars each with a
     * byte-length of 2 bytes - as the identifiers are excluded. This results in <tt>2n + 5</tt>
     * bytes written to the stream.
     * <p>
     * Support for writing <code>null</code> strings is not supported.
     *
     * @param value The value to write.
     * @throws java.lang.NullPointerException If the given <tt>String</tt> is <code>null</code>.
     * @see org.obicere.cc.methods.protocol.consumers.StringConsumer
     * @see org.obicere.cc.methods.protocol.consumers.AbstractConsumer#IDENTIFIER_STRING
     */

    public synchronized void write(final String value) {
        stringC.write(value);
    }

    public synchronized void write(final boolean[] value) {
        booleanC.write(value);
    }

    public synchronized void write(final byte[] value) {
        byteC.write(value);
    }

    public synchronized void write(final char[] value) {
        charC.write(value);
    }

    public synchronized void write(final short[] value) {
        shortC.write(value);
    }

    public synchronized void write(final int[] value) {
        intC.write(value);
    }

    public synchronized void write(final long[] value) {
        longC.write(value);
    }

    public synchronized void write(final float[] value) {
        floatC.write(value);
    }

    public synchronized void write(final double[] value) {
        doubleC.write(value);
    }

    private synchronized int nextIdentifier() {
        return buffer.read();
    }

    public synchronized boolean readBoolean() {
        return booleanC.read();
    }

    public synchronized byte readByte() {
        return byteC.read();
    }

    public synchronized short readShort() {
        return shortC.read();
    }

    public synchronized char readChar() {
        return charC.read();
    }

    public synchronized int readInt() {
        return intC.read();
    }

    public synchronized long readLong() {
        return longC.read();
    }

    public synchronized float readFloat() {
        return floatC.read();
    }

    public synchronized double readDouble() {
        return doubleC.read();
    }

    public synchronized String readString() {
        return stringC.read();
    }

    public synchronized boolean[] readBooleanArray() {
        return booleanC.readArray();
    }

    public synchronized byte[] readByteArray() {
        return byteC.readArray();
    }

    public synchronized short[] readShortArray() {
        return shortC.readArray();
    }

    public synchronized char[] readCharArray() {
        return charC.readArray();
    }

    public synchronized int[] readIntArray() {
        return intC.readArray();
    }

    public synchronized float[] readFloatArray() {
        return floatC.readArray();
    }

    public synchronized long[] readLongArray() {
        return longC.readArray();
    }

    public synchronized double[] readDoubleArray() {
        return doubleC.readArray();
    }

    public synchronized boolean hasNext() {
        return buffer.peek() != 0;
    }

    public boolean hasBoolean() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_BOOLEAN;
    }

    public boolean hasByte() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_BYTE;
    }

    public boolean hasShort() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_SHORT;
    }

    public boolean hasChar() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_CHAR;
    }

    public boolean hasInt() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_INT;
    }

    public boolean hasFloat() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_FLOAT;
    }

    public boolean hasLong() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_LONG;
    }

    public boolean hasDouble() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_DOUBLE;
    }

    public boolean hasString() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_STRING;
    }

    public boolean hasArray() {
        return buffer.peek() == AbstractConsumer.IDENTIFIER_ARRAY;
    }

    public synchronized <T> void write(final T[] value) {
        Objects.requireNonNull(value);
        final int length = value.length;
        writeIdentifier(AbstractConsumer.IDENTIFIER_ARRAY);
        intC.writeRaw(length);
        for (final Object t : value) {
            final Class cls = t.getClass();
            final int identifier = identifierFor(t.getClass());
            final int size = sizeFor(cls);
            if (identifier == -1 || size == -1) {
                throw new IllegalArgumentException("Non-supported class: " + cls);
            }
            writeIdentifier(identifier);
            writeFor(cls, t);
        }
    }

    @SuppressWarnings("unchecked")
    // This is all checked - not really though
    public synchronized <T, S> T readArray(final Class<T> cls) {
        final Class<S> component = (Class<S>) cls.getComponentType();
        if (component.isPrimitive()) {
            switch (component.getCanonicalName()) {
                case "boolean":
                    return (T) readBooleanArray();
                case "byte":
                    return (T) readByteArray();
                case "short":
                    return (T) readShortArray();
                case "char":
                    return (T) readCharArray();
                case "int":
                    return (T) readIntArray();
                case "float":
                    return (T) readFloatArray();
                case "long":
                    return (T) readLongArray();
                case "double":
                    return (T) readDoubleArray();
            }
        }
        final int length = intC.readRaw();
        final S[] array = (S[]) Array.newInstance(component, length);
        for (int i = 0; i < length; i++) {
            array[i] = (S) readMethodFor(component, nextIdentifier());
        }
        return (T) array;
    }

    private <T> Object readMethodFor(final Class<T> component, final int id) {
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
        }
        return null;
    }

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
                }
            } else {
                write((Object[]) obj);
            }
            return;
        }
        switch (cls.getCanonicalName()) {
            case "boolean":
            case "java.lang.Boolean":
                booleanC.writeRaw((boolean) obj);
                return;

            case "byte":
            case "java.lang.Byte":
                byteC.writeRaw((byte) obj);
                return;

            case "short":
            case "char":
            case "java.lang.Short":
            case "java.lang.Character":
                shortC.writeRaw((short) obj);
                return;

            case "int":
            case "java.lang.Integer":
                intC.writeRaw((int) obj);
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
                stringC.writeRaw((String) obj);
                return;
            default:
                throw new AssertionError("Could not write object for class: " + cls);
        }
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