package org.obicere.cc.shutdown;

/**
 *
 *
 * @author Obicere
 * @since 1.0
 */

public class SaveLayoutHook extends ShutDownHook {

    private static final String NAME = "save.layout";

    public SaveLayoutHook(final boolean conditional, final String purpose) {
        super(conditional, purpose, NAME, PRIORITY_WINDOW_CLOSING);
    }

    @Override
    public void run(){
        System.out.println("Ran shutdown hook: SaveLayoutHook");
    }

}
