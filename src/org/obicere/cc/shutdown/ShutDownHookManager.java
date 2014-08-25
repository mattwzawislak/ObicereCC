package org.obicere.cc.shutdown;

import org.obicere.cc.gui.GUI;
import org.obicere.cc.methods.Reflection;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class ShutDownHookManager {

    private static final ShutDownHook[] HOOKS;

    static {
        final Stream<Class<?>> hooks = Reflection.where(c -> ShutDownHook.class.isAssignableFrom(c) && !ShutDownHook.class.equals(c) && !SettingsShutDownHook.class.equals(c));
        final List<ShutDownHook> goodHooks = new LinkedList<>();
        hooks.forEach(e -> {
            if(e != null){
                final ShutDownHook hook = (ShutDownHook) Reflection.newInstance(e);
                if(hook == null){
                    // Error initializing
                }
                goodHooks.add(hook);
            }
        });
        HOOKS = new ShutDownHook[goodHooks.size()];
        final Iterator<ShutDownHook> iterator = goodHooks.iterator();
        for(int i = 0; iterator.hasNext(); i++){
            HOOKS[i] = iterator.next();
        }
    }

    private ShutDownHookManager() {
    }

    public static ShutDownHook[] getShutDownHooks(){
        return HOOKS;
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
