package org.obicere.cc.methods.protocol.consumers;

import org.obicere.cc.methods.protocol.Buffer;

import java.util.InputMismatchException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class FloatConsumer extends AbstractConsumer {
    public FloatConsumer(final Buffer buffer) {
        super(buffer);
    }

    public float read(){
        if (nextIdentifier() != IDENTIFIER_FLOAT) {
            throw new InputMismatchException();
        }
        return Float.intBitsToFloat(readRawInt());
    }

    public float readRaw(){
        return Float.intBitsToFloat(readRawInt());
    }

    public float[] readArray(){
        checkArray();
        final int length = readRawInt();
        final float[] array = new float[length];
        for (int i = 0; i < length; i++) {
            array[i] = read();
        }
        return array;
    }

    public void write(final float value){
        final int write = Float.floatToIntBits(value);
        writeIdentifier(IDENTIFIER_FLOAT);
        writeRawIntValue(write);
    }

    public void writeRaw(final float value){
        final int write = Float.floatToIntBits(value);
        writeRawIntValue(write);
    }

    public void write(final float[] value){
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeRawIntValue(value.length);
        for (final float aValue : value) {
            write(aValue);
        }
    }
}
