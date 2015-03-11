package org.obicere.cc.util.protocol.consumers;

import org.obicere.cc.util.protocol.Buffer;

import java.util.InputMismatchException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class DoubleConsumer extends AbstractConsumer {
    public DoubleConsumer(final Buffer buffer) {
        super(buffer);
    }

    public double read() {
        if (nextIdentifier() != IDENTIFIER_DOUBLE) {
            throw new InputMismatchException();
        }
        return Double.longBitsToDouble(readRawLong());
    }

    public double readRaw() {
        return Double.longBitsToDouble(readRawLong());
    }

    public double[] readArray() {
        checkArray();
        return readRawArray();
    }

    public double[] readRawArray() {
        final int length = readRawInt();
        final double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = read();
        }
        return array;
    }

    public void write(final double value) {
        final long write = Double.doubleToLongBits(value);
        writeIdentifier(IDENTIFIER_DOUBLE);
        writeRawLongValue(write);
    }

    public void writeRaw(final double value) {
        final long write = Double.doubleToLongBits(value);
        writeRawLongValue(write);
    }

    public void write(final double[] value) {
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeRawIntValue(value.length);
        for (final double aValue : value) {
            write(aValue);
        }
    }
}
