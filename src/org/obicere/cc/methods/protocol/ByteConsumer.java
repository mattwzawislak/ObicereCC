package org.obicere.cc.methods.protocol;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Obicere
 */
public class ByteConsumer {

    private static final int IDENTIFIER_BOOLEAN = 0x00;
    private static final int IDENTIFIER_BYTE    = 0x01;
    private static final int IDENTIFIER_SHORT   = 0x03;
    private static final int IDENTIFIER_CHAR    = 0x05;
    private static final int IDENTIFIER_INT     = 0x06;
    private static final int IDENTIFIER_LONG    = 0x08;
    private static final int IDENTIFIER_FLOAT   = 0x0A;
    private static final int IDENTIFIER_DOUBLE  = 0x0C;
    private static final int IDENTIFIER_STRING  = 0x0E;
    private static final int IDENTIFIER_ARRAY   = 0x0F;

    private static final int IDENTIFIER_SIZE = 1;

    private static final int BUFFER_SIZE_8_BIT  = (1 << 0) + IDENTIFIER_SIZE;
    private static final int BUFFER_SIZE_16_BIT = (1 << 1) + IDENTIFIER_SIZE;
    private static final int BUFFER_SIZE_32_BIT = (1 << 2) + IDENTIFIER_SIZE;
    private static final int BUFFER_SIZE_64_BIT = (1 << 3) + IDENTIFIER_SIZE;

    private static final int MAXIMUM_BUFFER_SIZE = 1 << 30; //  allows maximum of 134,217,728 bytes. (128MB)

    private static final float DEFAULT_GROWTH = 2.0f;

    private static final int DEFAULT_SIZE = 32;

    private int bufferSize;

    private int lastWriteIndex = 0;

    private int lastReadIndex = 0;


    private final float growth;

    private byte[] buffer;

    public static void main(final String[] args) {
        final ByteConsumer consumer = new ByteConsumer();
        final int[][] i = {{1, 2, 3}, {4, 5, 6}};
        consumer.write(i);
        System.out.println(Arrays.toString(consumer.toOutputArray()));
        final int[][] newI = consumer.readArray(int[][].class);
        System.out.println(Arrays.deepToString(newI));
    }

    protected ByteConsumer() {
        this.growth = DEFAULT_GROWTH;
        this.buffer = new byte[DEFAULT_SIZE];
        this.bufferSize = DEFAULT_SIZE;
    }

    protected ByteConsumer(final int initialBuffer) {
        if (initialBuffer <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be > 0; given size: " + initialBuffer);
        }
        this.growth = DEFAULT_GROWTH;
        this.buffer = new byte[initialBuffer];
        this.bufferSize = initialBuffer;
    }

    protected ByteConsumer(final int initialBuffer, final float growth) {
        if (initialBuffer <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be > 0; given size: " + initialBuffer);
        }
        if (growth <= 1.0) {
            throw new IllegalArgumentException("Growth ratio must be greater than 1; given growth: " + growth);
        }
        this.growth = growth;
        this.buffer = new byte[initialBuffer];
        this.bufferSize = initialBuffer;
    }

    protected void expand() {
        int newLength = (int) (growth * bufferSize);
        if (newLength > MAXIMUM_BUFFER_SIZE) {
            newLength = MAXIMUM_BUFFER_SIZE;
        }
        final byte[] newBuffer = new byte[newLength];
        System.arraycopy(buffer, 0, newBuffer, 0, lastWriteIndex);
        this.buffer = newBuffer;
        this.bufferSize = newLength;
    }

    private void checkWriteSize(final int newWrite) {
        if (lastWriteIndex + newWrite > bufferSize) {
            expand();
        }
    }

    public byte[] toOutputArray() {
        final byte[] output = new byte[lastWriteIndex];
        System.arraycopy(buffer, 0, output, 0, lastWriteIndex);
        return output;
    }

    private void writeIdentifier(final int identifier) {
        checkWriteSize(1);
        buffer[lastWriteIndex++] = (byte) identifier;
    }

    private void writeRawByteValue(final int b) {
        checkWriteSize(1);
        buffer[lastWriteIndex++] = (byte) (b & 0xFF);
    }

    private void writeRawShortValue(final int s) {
        writeRawByteValue(s >> 8);
        writeRawByteValue(s >> 0);
    }

    private void writeRawIntValue(final int i) {
        writeRawShortValue(i >> 16);
        writeRawShortValue(i >> 0);
    }

    private void writeRawLongValue(final long l) {
        writeRawIntValue((int) (l >> 32)); // Damn 32-bit ALUs
        writeRawIntValue((int) (l >> 0));
    }

    private void writeRawStringValue(final String str) {
        final char[] data = str.toCharArray();
        writeRawIntValue(data.length);
        for (final char c : data) {
            writeRawShortValue(c);
        }
    }

    public synchronized void write(final boolean value) {
        writeIdentifier(IDENTIFIER_BOOLEAN);
        writeRawByteValue(value ? 1 : 0);
    }

    public synchronized void write(final byte value) {
        writeIdentifier(IDENTIFIER_BYTE);
        writeRawByteValue(value);
    }

    public synchronized void write(final short value) {
        writeIdentifier(IDENTIFIER_SHORT);
        writeRawShortValue(value);
    }

    public synchronized void write(final char value) {
        writeIdentifier(IDENTIFIER_CHAR);
        writeRawShortValue(value);
    }

    public synchronized void write(final int value) {
        writeIdentifier(IDENTIFIER_INT);
        writeRawIntValue(value);
    }

    public synchronized void write(long value) {
        writeIdentifier(IDENTIFIER_LONG);
        writeRawLongValue(value);
    }

    public synchronized void write(final float value) {
        final int write = Float.floatToIntBits(value);
        writeIdentifier(IDENTIFIER_FLOAT);
        writeRawIntValue(write);
    }

    public synchronized void write(final double value) {
        final long write = Double.doubleToLongBits(value);
        writeIdentifier(IDENTIFIER_DOUBLE);
        writeRawLongValue(write);
    }

    public synchronized void write(final String value) {
        Objects.requireNonNull(value, "Cannot write null string to buffer.");
        final int length = value.length();
        writeIdentifier(IDENTIFIER_STRING);
        writeRawStringValue(value);
    }

    public synchronized <T> void write(final T[] value) {
        Objects.requireNonNull(value);
        final int length = value.length;
        final Class<?> cls = value.getClass().getComponentType();
        final int identifier = identifierFor(cls);
        final int size = sizeFor(cls);
        if (identifier == -1 || size == -1) {
            throw new IllegalArgumentException("Non-supported class: " + cls);
        }
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(identifier);
        writeRawIntValue(length);
        for (final Object t : value) {
            System.out.println("Writing: " + t);
            writeFor(cls, t);
        }
        System.out.println("Done writing");
    }

    public synchronized void write(final boolean[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_BOOLEAN);
        writeRawIntValue(value.length);
        for (int i = 0; i < value.length; i++) {
            writeRawByteValue(value[i] ? 1 : 0);
        }
    }

    public synchronized void write(final byte[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_BYTE);
        writeRawIntValue(value.length);
        for (int i = 0; i < value.length; i++) {
            writeRawByteValue(value[i]);
        }
    }

    public synchronized void write(final char[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_CHAR);
        writeRawIntValue(value.length);
        for (int i = 0; i < value.length; i++) {
            writeRawShortValue(value[i]);
        }
    }

    public synchronized void write(final short[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_SHORT);
        writeRawIntValue(value.length);
        for (int i = 0; i < value.length; i++) {
            writeRawShortValue(value[i]);
        }
    }

    public synchronized void write(final int[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_INT);
        writeRawIntValue(value.length);
        for (int i = 0; i < value.length; i++) {
            writeRawIntValue(value[i]);
        }
    }

    public synchronized void write(final long[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_LONG);
        writeRawIntValue(value.length);
        for (int i = 0; i < value.length; i++) {
            writeRawLongValue(value[i]);
        }
    }

    public synchronized void write(final float[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_FLOAT);
        writeRawIntValue(value.length);
        for (int i = 0; i < value.length; i++) {
            writeRawIntValue(Float.floatToIntBits(value[i]));
        }
    }

    public synchronized void write(final double[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeIdentifier(IDENTIFIER_DOUBLE);
        writeRawIntValue(value.length);
        for (int i = 0; i < value.length; i++) {
            writeRawLongValue(Double.doubleToLongBits(value[i]));
        }
    }

    private synchronized byte peekNext() {
        return buffer[lastReadIndex];
    }

    private synchronized byte next() {
        return buffer[lastReadIndex++];
    }

    public boolean hasBoolean() {
        return peekNext() == IDENTIFIER_BOOLEAN;
    }

    public boolean hasByte() {
        return peekNext() == IDENTIFIER_BYTE;
    }

    public boolean hasShort() {
        return peekNext() == IDENTIFIER_SHORT;
    }

    public boolean hasChar() {
        return peekNext() == IDENTIFIER_CHAR;
    }

    public boolean hasInt() {
        return peekNext() == IDENTIFIER_INT;
    }

    public boolean hasFloat() {
        return peekNext() == IDENTIFIER_FLOAT;
    }

    public boolean hasLong() {
        return peekNext() == IDENTIFIER_LONG;
    }

    public boolean hasDouble() {
        return peekNext() == IDENTIFIER_DOUBLE;
    }

    public boolean hasString() {
        return peekNext() == IDENTIFIER_STRING;
    }

    public boolean hasArray() {
        return peekNext() == IDENTIFIER_ARRAY;
    }

    private byte readRawByte() {
        return next();
    }

    private short readRawShort() {
        return (short) (
                readRawByte() << 8 |
                readRawByte() << 0);
    }

    private int readRawInt() {
        return (readRawShort() << 16 |
                readRawShort() << 0);
    }

    private long readRawLong() {
        return ((long) readRawInt() << 32 |
                (long) readRawInt() << 0);
    }

    private String readRawString() {
        final int length = readRawInt();
        final char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char) readRawShort();
        }
        return new String(chars);
    }

    public synchronized boolean readBoolean() {
        if (next() != IDENTIFIER_BOOLEAN) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        return readRawByte() != 0;
    }

    public synchronized byte readByte() {
        if (next() != IDENTIFIER_BYTE) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        return readRawByte();
    }

    public synchronized short readShort() {
        if (next() != IDENTIFIER_SHORT) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        return readRawShort();
    }

    public synchronized char readChar() {
        if (next() != IDENTIFIER_CHAR) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        return (char) readRawShort();
    }

    public synchronized int readInt() {
        if (next() != IDENTIFIER_INT) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        return readRawInt();
    }

    public synchronized long readLong() {
        if (next() != IDENTIFIER_LONG) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        return readRawLong();
    }

    public synchronized float readFloat() {
        if (next() != IDENTIFIER_FLOAT) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        return Float.intBitsToFloat(readRawInt());
    }

    public synchronized double readDouble() {
        if (next() != IDENTIFIER_DOUBLE) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        return Double.longBitsToDouble(readRawLong());
    }

    public synchronized String readString() {
        if (next() != IDENTIFIER_STRING) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        return readRawString();
    }

    public synchronized <T> T[] readArray(final Class<T[]> cls) {
        if (next() != IDENTIFIER_ARRAY) {
            throw new InvalidProtocolException(lastReadIndex);
        }
        final int nextID = next();
        final int length = readRawInt();
        final Class<?> component = cls.getComponentType();
        final T[] array = (T[]) Array.newInstance(cls.getComponentType(), length);
        for (int i = 0; i < length; i++) {
            array[i] = (T) readMethodFor(cls, nextID);
        }
        return array;
    }

    private <T> Object readMethodFor(final Class<T[]> cls, final int id) {
        switch (id) {
            case IDENTIFIER_BOOLEAN:
                return readRawByte() != 0;
            case IDENTIFIER_BYTE:
                return readRawByte();
            case IDENTIFIER_SHORT:
                return readRawShort();
            case IDENTIFIER_CHAR:
                return readRawShort();
            case IDENTIFIER_INT:
                return readRawInt();
            case IDENTIFIER_FLOAT:
                return Float.intBitsToFloat(readRawInt());
            case IDENTIFIER_LONG:
                return readRawLong();
            case IDENTIFIER_DOUBLE:
                return Double.longBitsToDouble(readRawLong());
            case IDENTIFIER_STRING:
                return readRawString();
            case IDENTIFIER_ARRAY:
                return readArray(cls);
        }
        return null;
    }

    private Class<?> classFor(final int identifier) {
        switch (identifier) {
            case IDENTIFIER_BOOLEAN:
                return Boolean.class;
            case IDENTIFIER_BYTE:
                return Byte.class;
            case IDENTIFIER_SHORT:
                return Short.class;
            case IDENTIFIER_CHAR:
                return Character.class;
            case IDENTIFIER_INT:
                return Integer.class;
            case IDENTIFIER_FLOAT:
                return Float.class;
            case IDENTIFIER_LONG:
                return Long.class;
            case IDENTIFIER_DOUBLE:
                return Double.class;
            case IDENTIFIER_STRING:
                return String.class;
            case IDENTIFIER_ARRAY:
                return Object[].class;
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
            } else if (component.isArray()) {
                write((Object[]) obj);
            }
            return;
        }
        switch (cls.getCanonicalName()) {
            case "boolean":
            case "java.lang.Boolean":
                final int value = ((boolean) obj) ? 1 : 0;
                writeRawByteValue(value);
                return;

            case "byte":
            case "java.lang.Byte":
                writeRawByteValue((byte) obj);
                return;

            case "short":
            case "char":
            case "java.lang.Short":
            case "java.lang.Character":
                writeRawShortValue((byte) obj);
                return;

            case "int":
            case "java.lang.Integer":
                writeRawIntValue((int) obj);
                return;

            case "long":
            case "java.lang.Long":
                writeRawLongValue((long) obj);
                return;

            case "float":
            case "java.lang.Float":
                final int intValue = Float.floatToIntBits((float) obj);
                writeRawIntValue(intValue);
                return;

            case "double":
            case "java.lang.Double":
                final long longValue = Double.doubleToLongBits((double) obj);
                writeRawLongValue(longValue);
                return;

            case "java.lang.String":
                writeRawStringValue((String) obj);
                return;
            default:
                throw new AssertionError("Could not write object for class: " + cls);
        }
    }

    private int identifierFor(final Class<?> cls) {
        Objects.requireNonNull(cls);
        if (cls.isArray()) {
            return IDENTIFIER_ARRAY;
        }
        switch (cls.getCanonicalName()) {
            case "boolean":
            case "java.lang.Boolean":
                return IDENTIFIER_BOOLEAN;

            case "byte":
            case "java.lang.Byte":
                return IDENTIFIER_BYTE;

            case "short":
            case "java.lang.Short":
                return IDENTIFIER_SHORT;

            case "char":
            case "java.lang.Character":
                return IDENTIFIER_CHAR;

            case "int":
            case "java.lang.Integer":
                return IDENTIFIER_INT;

            case "long":
            case "java.lang.Long":
                return IDENTIFIER_LONG;

            case "float":
            case "java.lang.Float":
                return IDENTIFIER_FLOAT;

            case "double":
            case "java.lang.Double":
                return IDENTIFIER_DOUBLE;

            case "java.lang.String":
                return IDENTIFIER_STRING;
            default:
                return -1; // Unsupported type
        }
    }

    // This will only return header size. Additional memory may need to be allocated
    private int sizeFor(final Class<?> cls) {
        Objects.requireNonNull(cls);
        if (cls.isArray()) {
            return BUFFER_SIZE_32_BIT; // Only lastWriteIndex (int) is recorded in array header
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
            case "java.lang.String": // Like the array, only lastWriteIndex is in header
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
