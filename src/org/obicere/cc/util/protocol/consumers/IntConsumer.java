package org.obicere.cc.util.protocol.consumers;

import org.obicere.cc.util.protocol.ProtocolBuffer;

import java.util.InputMismatchException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class IntConsumer extends AbstractConsumer {
    public IntConsumer(final ProtocolBuffer buffer) {
        super(buffer);
    }

    public int read() {
        if (nextIdentifier() != IDENTIFIER_INT) {
            throw new InputMismatchException();
        }
        return readRawInt();
    }

    public int readRaw() {
        return readRawInt();
    }

    public int[] readArray() {
        checkArray();
        return readRawArray();
    }

    public int[] readRawArray() {
        final int length = readRawInt();
        final int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = read();
        }
        return array;
    }

    public void write(final int value) {
        writeIdentifier(IDENTIFIER_INT);
        writeRawIntValue(value);
    }

    public void writeRaw(final int value) {
        writeRawIntValue(value);
    }

    public void write(final int[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeRawIntValue(value.length);
        for (final int aValue : value) {
            write(aValue);
        }
    }
}
