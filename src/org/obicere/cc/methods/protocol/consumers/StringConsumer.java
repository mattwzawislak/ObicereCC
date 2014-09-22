package org.obicere.cc.methods.protocol.consumers;

import org.obicere.cc.methods.protocol.Buffer;

import java.util.InputMismatchException;
import java.util.Objects;

/**
 * @author Obicere
 */
public class StringConsumer extends AbstractConsumer {
    public StringConsumer(final Buffer buffer) {
        super(buffer);
    }

    public String read(){
        if (nextIdentifier() != IDENTIFIER_STRING) {
            throw new InputMismatchException();
        }
        return readRaw();
    }

    public String readRaw(){
        final int length = readRawInt();
        final char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char) readRawShort();
        }
        return new String(chars);
    }

    public void write(final String value){
        Objects.requireNonNull(value, "Cannot write null string to buffer.");
        writeIdentifier(IDENTIFIER_STRING);
        writeRaw(value);
    }

    public void writeRaw(final String value){
        final char[] data = value.toCharArray();
        writeRawIntValue(data.length);
        for (final char c : data) {
            writeRawShortValue((short) c);
        }
    }
}
