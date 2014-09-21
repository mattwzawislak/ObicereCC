package org.obicere.cc.methods.protocol;

import org.obicere.cc.methods.protocol.consumers.*;

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

    private final BooleanConsumer booleanC;
    private final ByteConsumer    byteC;
    private final ShortConsumer   shortC;
    private final CharConsumer    charC;
    private final IntConsumer     intC;
    private final FloatConsumer   floatC;
    private final LongConsumer    longC;
    private final DoubleConsumer  doubleC;
    private final StringConsumer  stringC;

    private final Buffer buffer;

    protected StreamConsumer() {
        this(Buffer.DEFAULT_SIZE, Buffer.DEFAULT_GROWTH);
    }

    protected StreamConsumer(final int initialLength) {
        this(initialLength, Buffer.DEFAULT_GROWTH);
    }

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

    public synchronized void clearRead() {
        buffer.clearReadBuffer();
    }

    public synchronized boolean shouldClear() {
        return buffer.length() == Buffer.MAXIMUM_BUFFER_SIZE;
    }

    public synchronized void writeAvailable(final OutputStream stream) throws IOException {
        buffer.writeAvailable(stream);
    }

    public synchronized void readAvailable(final InputStream stream) throws IOException {
        buffer.readAvailable(stream);
    }

    private void writeIdentifier(final int identifier) {
        buffer.write((byte) identifier);
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
        return buffer.peek() != -1;
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

    private int identifierFor(final Class<?> cls) {
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