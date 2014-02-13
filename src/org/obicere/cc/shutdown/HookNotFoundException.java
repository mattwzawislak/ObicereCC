package org.obicere.cc.shutdown;
public class HookNotFoundException extends RuntimeException {

    public HookNotFoundException(final String hookName){
        super("Hook not found for name: " + hookName);
    }

    public HookNotFoundException(final String message, final String hookName){
        super(message + hookName);
    }

}
