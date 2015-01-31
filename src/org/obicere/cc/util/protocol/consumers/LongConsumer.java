package org.obicere.cc.util.protocol.consumers;

import org.obicere.cc.util.protocol.Buffer;

import java.util.InputMismatchException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class LongConsumer extends AbstractConsumer {
    public LongConsumer(final Buffer buffer) {
        super(buffer);
    }

    public long read() {
        if (nextIdentifier() != IDENTIFIER_LONG) {
            throw new InputMismatchException();
        }
        return readRawLong();
    }

    public long readRaw() {
        return readRawLong();
    }

    public long[] readArray() {
        checkArray();
        final int length = readRawInt();
        final long[] array = new long[length];
        for (int i = 0; i < length; i++) {
            array[i] = read();
        }
        return array;
    }

    public void write(final long value) {
        writeIdentifier(IDENTIFIER_LONG);
        writeRawLongValue(value);
    }

    public void writeRaw(final long value) {
        writeRawLongValue(value);
    }

    public void write(final long[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeRawIntValue(value.length);
        for (final long aValue : value) {
            write(aValue);
        }
    }
}
