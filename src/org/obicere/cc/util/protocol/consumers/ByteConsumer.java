package org.obicere.cc.util.protocol.consumers;

import org.obicere.cc.util.protocol.ProtocolBuffer;

import java.util.InputMismatchException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class ByteConsumer extends AbstractConsumer {

    public ByteConsumer(final ProtocolBuffer buffer) {
        super(buffer);
    }

    public byte read() {
        if (nextIdentifier() != IDENTIFIER_BYTE) {
            throw new InputMismatchException();
        }
        return readRawByte();
    }

    public byte readRaw() {
        return readRawByte();
    }

    public byte[] readArray() {
        checkArray();
        return readRawArray();
    }

    public byte[] readRawArray() {
        final int length = readRawInt();
        final byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = read();
        }
        return array;
    }

    public void write(final byte value) {
        writeIdentifier(IDENTIFIER_BYTE);
        writeRawByteValue(value);
    }

    public void writeRaw(final byte value) {
        writeRawByteValue(value);
    }

    public void write(final byte[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeRawIntValue(value.length);
        for (final byte aValue : value) {
            write(aValue);
        }
    }

}
