package org.obicere.cc.shutdown;


public abstract class ShutDownHook extends  Thread {

    public static final int PRIORITY_WINDOW_CLOSING = 0x0;
    public static final int PRIORITY_RUNTIME_SHUTDOWN = 0x1;

    private final boolean conditional;
    private final String purpose;
    private final int priority;

    public ShutDownHook(final String name, final int priority){
        this(false, null, name, priority);
    }

    public ShutDownHook(final boolean conditional, final String purpose, final String name, final int priority){
        super(name);
        this.conditional = conditional;
        this.purpose = purpose;
        this.priority = priority;
    }

    public boolean conditional(){
        return conditional;
    }

    public String getPurpose(){
        return purpose;
    }

    public int getHookPriority(){
        return priority;
    }

    public abstract void run();

}
