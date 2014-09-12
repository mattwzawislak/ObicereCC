package org.obicere.cc.methods.protocol;

/**
 * @author Obicere
 */
public class InvalidProtocolException extends RuntimeException {

    public InvalidProtocolException(final int invalidIndex) {
        super("Invalid protocol. Unexpected element at index: " + invalidIndex);
    }

}
