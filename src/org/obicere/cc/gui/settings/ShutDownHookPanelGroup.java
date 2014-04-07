package org.obicere.cc.gui.settings;

import org.obicere.cc.shutdown.ShutDownHook;
import org.obicere.cc.shutdown.ShutDownHookManager;

import javax.swing.*;

/**
 * @author Obicere
 */
public class ShutDownHookPanelGroup extends JPanel {

    public ShutDownHookPanelGroup() {
        final ShutDownHook[] hooks = ShutDownHookManager.getShutDownHooks();
        for (final ShutDownHook hook : hooks) {
            if (hook.isConditional()) {
                add(new ShutDownHookPanel(hook));
            }
        }
    }

}
