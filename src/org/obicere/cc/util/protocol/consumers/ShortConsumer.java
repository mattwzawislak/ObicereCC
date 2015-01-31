package org.obicere.cc.util.protocol.consumers;

import org.obicere.cc.util.protocol.Buffer;

import java.util.InputMismatchException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class ShortConsumer extends AbstractConsumer {

    public ShortConsumer(final Buffer buffer) {
        super(buffer);
    }

    public short read() {
        if (nextIdentifier() != IDENTIFIER_SHORT) {
            throw new InputMismatchException();
        }
        return readRawShort();
    }

    public short readRaw() {
        return readRawShort();
    }

    public short[] readArray() {
        checkArray();
        final int length = readRawInt();
        final short[] array = new short[length];
        for (int i = 0; i < length; i++) {
            array[i] = read();
        }
        return array;
    }

    public void write(final short value) {
        writeIdentifier(IDENTIFIER_SHORT);
        writeRawShortValue(value);
    }

    public void writeRaw(final short value) {
        writeRawShortValue(value);
    }

    public void write(final short[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeRawIntValue(value.length);
        for (final short aValue : value) {
            write(aValue);
        }
    }
}
