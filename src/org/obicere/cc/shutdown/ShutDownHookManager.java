package org.obicere.cc.shutdown;

import org.obicere.cc.gui.GUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ShutDownHookManager {

    private static final ShutDownHook[] HOOKS = new ShutDownHook[]{
            new SaveLayoutHook(),
            new SaveProgressHook()
    };

    private ShutDownHookManager() {
    }

    public static void setup() {
        for (final ShutDownHook hook : HOOKS) {
            System.out.println("Adding ShutDownHook: " + hook.getName());
            switch (hook.getHookPriority()) {
                case ShutDownHook.PRIORITY_RUNTIME_SHUTDOWN:
                    Runtime.getRuntime().addShutdownHook(hook);
                    break;
                case ShutDownHook.PRIORITY_WINDOW_CLOSING:
                    final WindowListener listener = new WindowAdapter() {
                        @Override
                        public void windowClosing(final WindowEvent e) {
                            hook.start();
                        }
                    };
                    GUI.WINDOW_CLOSING_HOOKS.add(listener);
                    break;
            }
        }
    }

    public static ShutDownHook hookByName(final String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        for (final ShutDownHook hook : HOOKS) {
            if (name.equals(hook.getName())) {
                return hook;
            }
        }
        throw new HookNotFoundException(name);
    }

    public static <T extends ShutDownHook> T hookByName(final Class<T> clz, final String name) {
        return clz.cast(hookByName(name));
    }

}
