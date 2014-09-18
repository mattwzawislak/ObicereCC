package org.obicere.cc.shutdown;

import org.obicere.cc.gui.FrameManager;
import org.obicere.cc.methods.Reflection;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ShutDownHookManager {

    private static final Logger LOGGER = Logger.getLogger(SettingsShutDownHook.class.getCanonicalName());

    private static final ShutDownHook[] HOOKS;

    static {
        final Stream<Class<?>> hooks = Reflection.where(c -> ShutDownHook.class.isAssignableFrom(c) && !ShutDownHook.class.equals(c) && !SettingsShutDownHook.class.equals(c));
        final List<ShutDownHook> goodHooks = new LinkedList<>();
        hooks.forEach(e -> {
            if (e != null) {
                final ShutDownHook hook = (ShutDownHook) Reflection.newInstance(e);
                if (hook == null) {
                    LOGGER.log(Level.WARNING, "Failed to create hook: " + e.getName());
                    // Error initializing
                }
                goodHooks.add(hook);
            }
        });
        HOOKS = new ShutDownHook[goodHooks.size()];
        final Iterator<ShutDownHook> iterator = goodHooks.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            HOOKS[i] = iterator.next();
        }
    }

    private ShutDownHookManager() {
    }

    public static ShutDownHook[] getShutDownHooks() {
        return HOOKS;
    }

    public static void setup() {
        for (final ShutDownHook hook : HOOKS) {
            LOGGER.log(Level.INFO, "Adding hook: " + hook.getName());
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
                    FrameManager.WINDOW_CLOSING_HOOKS.add(listener);
                    break;
            }
        }
    }

    public static ShutDownHook hookByName(final String name) {
        Objects.requireNonNull(name);
        for (final ShutDownHook hook : HOOKS) {
            if (name.equals(hook.getName())) {
                return hook;
            }
        }
        throw new HookNotFoundException(name);
    }

    public static <T extends ShutDownHook> T hookByClass(final Class<T> cls) {
        Objects.requireNonNull(cls);
        for (final ShutDownHook hook : HOOKS) {
            if (cls.isInstance(hook)) {
                return cls.cast(hook);
            }
        }
        throw new HookNotFoundException(cls.getCanonicalName());
    }

}
