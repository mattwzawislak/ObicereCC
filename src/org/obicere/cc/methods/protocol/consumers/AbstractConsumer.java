package org.obicere.cc.methods.protocol.consumers;

import org.obicere.cc.methods.protocol.Buffer;

import java.util.InputMismatchException;

/**
 * @author Obicere
 */
public abstract class AbstractConsumer {

    /**
     * The basic identifier for a <tt>boolean</tt>,
     *
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_BOOLEAN = 0x01;

    /**
     * The basic identifier for a signed <tt>byte</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_BYTE + 1</tt>.
     *
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_BYTE = 0x02;

    /**
     * The basic identifier for a signed <tt>short</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_SHORT + 1</tt>.
     *
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_SHORT = 0x04;

    /**
     * The basic identifier for a UTF-16 <tt>char</tt>. All characters added to this protocol should
     * be in a UTF-16 format.
     *
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_CHAR = 0x06;

    /**
     * The basic identifier for a signed <tt>int</tt>. Unless otherwise specified, the unsigned type
     * would be <tt>IDENTIFIER_INT + 1</tt>.
     *
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_INT = 0x07;

    /**
     * The basic identifier for a signed <tt>long</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_LONG + 1</tt>.
     *
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_LONG = 0x09;

    /**
     * The basic identifier for a signed <tt>float</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_FLOAT + 1</tt>.
     *
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_FLOAT = 0x0B;

    /**
     * The basic identifier for a signed <tt>double</tt>. Unless otherwise specified, the unsigned
     * type would be <tt>IDENTIFIER_DOUBLE + 1</tt>.
     *
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_DOUBLE = 0x0D;

    /**
     * The basic identifier for a UTF-16 <tt>String</tt>. All characters added to this protocol
     * should be in a UTF-16 format.
     *
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_STRING = 0x0F;

    /**
     * The basic identifier for an array of a designed type. Revision 1.0 of the protocol dictates
     * that a generic array is impossible and that a one-dimensional array must have a defined
     * component type specified by an identifier.
     *
     * @see AbstractConsumer#IDENTIFIER_BOOLEAN
     * @see AbstractConsumer#IDENTIFIER_BYTE
     * @see AbstractConsumer#IDENTIFIER_SHORT
     * @see AbstractConsumer#IDENTIFIER_CHAR
     * @see AbstractConsumer#IDENTIFIER_INT
     * @see AbstractConsumer#IDENTIFIER_LONG
     * @see AbstractConsumer#IDENTIFIER_FLOAT
     * @see AbstractConsumer#IDENTIFIER_DOUBLE
     * @see org.obicere.cc.methods.protocol.StreamConsumer#identifierFor
     */
    public static final int IDENTIFIER_ARRAY = 0x10;

    protected final Buffer buffer;

    public AbstractConsumer(final Buffer buffer) {
        this.buffer = buffer;
    }

    protected void writeRawByteValue(final int b) {
        buffer.write((byte) b);
    }

    protected void writeRawShortValue(final int s) {
        writeRawByteValue(s >> 8);
        writeRawByteValue(s >> 0);
    }

    protected void writeRawIntValue(final int i) {
        writeRawShortValue(i >> 16);
        writeRawShortValue(i >> 0);
    }

    protected void writeRawLongValue(final long l) {
        writeRawIntValue((int) (l >> 32)); // Damn 32-bit ALUs
        writeRawIntValue((int) (l >> 0));
    }

    protected void writeIdentifier(final int id) {
        writeRawByteValue(id);
    }

    protected byte readRawByte() {
        return buffer.read();
    }

    protected short readRawShort() {
        return (short) (
                (0xFF & readRawByte()) << 8 |
                (0xFF & readRawByte()) << 0);
    }

    protected int readRawInt() {
        return ((0xFFFF & readRawShort()) << 16 |
                (0xFFFF & readRawShort()) << 0);
    }

    protected long readRawLong() {
        return ((0xFFFFFFFFL & readRawInt()) << 32 |
                (0xFFFFFFFFL & readRawInt()) << 0);
    }

    protected int nextIdentifier() {
        return buffer.read();
    }

    protected void checkArray() {
        if (nextIdentifier() != IDENTIFIER_ARRAY) {
            throw new InputMismatchException();
        }
    }
}
