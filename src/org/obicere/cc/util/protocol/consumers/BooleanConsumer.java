package org.obicere.cc.util.protocol.consumers;

import org.obicere.cc.util.protocol.Buffer;

import java.util.InputMismatchException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class BooleanConsumer extends AbstractConsumer {

    public BooleanConsumer(final Buffer buffer) {
        super(buffer);
    }

    public boolean read(){
        if (nextIdentifier() != IDENTIFIER_BOOLEAN) {
            throw new InputMismatchException();
        }
        return readRawByte() != 0;
    }

    public boolean readRaw(){
        return readRawByte() != 0;
    }

    public boolean[] readArray(){
        checkArray();
        final int length = readRawInt();
        final boolean[] array = new boolean[length];
        for (int i = 0; i < length; i++) {
            array[i] = read();
        }
        return array;
    }

    public void write(final boolean value){
        writeIdentifier(IDENTIFIER_BOOLEAN);
        writeRawByteValue((byte) (value ? 1 : 0));
    }

    public void writeRaw(final boolean value){
        writeRawByteValue((byte) (value ? 1 : 0));
    }

    public void write(final boolean[] value){
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeRawIntValue(value.length);
        for (final boolean aValue : value) {
            write(aValue);
        }
    }

}
