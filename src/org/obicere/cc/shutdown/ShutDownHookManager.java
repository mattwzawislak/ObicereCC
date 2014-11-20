package org.obicere.cc.shutdown;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.configuration.DomainAccess;
import org.obicere.cc.util.Reflection;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Stream;

public class ShutDownHookManager extends DomainAccess {

    private final ShutDownHook[] hooks;

    public ShutDownHookManager(final Domain access) {
        super(access);
        final Stream<Class<?>> hooks = Reflection.where(c -> ShutDownHook.class.isAssignableFrom(c) && !ShutDownHook.class.equals(c) && !SettingsShutDownHook.class.equals(c));
        final List<ShutDownHook> goodHooks = new LinkedList<>();
        hooks.forEach(e -> {
            if (e != null) {
                final ShutDownHook hook = (ShutDownHook) Reflection.newInstance(e);
                if (hook == null) {
                    log.log(Level.WARNING, "Failed to create hook: " + e.getName());
                    // Error initializing
                }
                goodHooks.add(hook);
            }
        });
        this.hooks = new ShutDownHook[goodHooks.size()];
        goodHooks.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        final Iterator<ShutDownHook> iterator = goodHooks.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            this.hooks[i] = iterator.next();
        }
    }

    public ShutDownHook[] getShutDownHooks() {
        return hooks;
    }

    @Override
    public void run() {
        for (final ShutDownHook hook : hooks) {
            log.log(Level.INFO, "Adding hook: " + hook.getName());
            switch (hook.getHookPriority()) {
                case ShutDownHook.PRIORITY_RUNTIME_SHUTDOWN:
                    Runtime.getRuntime().addShutdownHook(hook);
                    break;
                case ShutDownHook.PRIORITY_WINDOW_CLOSING:
                    access.getFrameManager().addWindowClosingHook(hook);
                    break;
            }
        }
    }

    public ShutDownHook hookByName(final String name) {
        Objects.requireNonNull(name);
        for (final ShutDownHook hook : hooks) {
            if (name.equals(hook.getName())) {
                return hook;
            }
        }
        throw new HookNotFoundException(name);
    }

    public <T extends ShutDownHook> T hookByClass(final Class<T> cls) {
        Objects.requireNonNull(cls);
        for (final ShutDownHook hook : hooks) {
            if (cls.isInstance(hook)) {
                return cls.cast(hook);
            }
        }
        throw new HookNotFoundException(cls.getCanonicalName());
    }
}
