package org.obicere.cc.util.protocol.consumers;

import org.obicere.cc.util.protocol.Buffer;

import java.util.InputMismatchException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class CharConsumer extends AbstractConsumer {
    public CharConsumer(final Buffer buffer) {
        super(buffer);
    }

    public char read(){
        if (nextIdentifier() != IDENTIFIER_CHAR) {
            throw new InputMismatchException();
        }
        return (char) readRawShort();
    }

    public char readRaw(){
        return (char) readRawShort();
    }

    public char[] readArray(){
        checkArray();
        final int length = readRawInt();
        final char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = read();
        }
        return array;
    }

    public void write(final char value){
        writeIdentifier(IDENTIFIER_CHAR);
        writeRawShortValue((short) value);
    }

    public void writeRaw(final char value){
        writeRawShortValue((short) value);
    }

    public void write(final char[] value){
        Objects.requireNonNull(value);
        writeIdentifier(IDENTIFIER_ARRAY);
        writeRawIntValue(value.length);
        for (final char aValue : value) {
            write(aValue);
        }
    }
}
