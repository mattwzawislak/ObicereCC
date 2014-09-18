package org.obicere.cc.projects;

import org.obicere.cc.executor.Case;
import org.obicere.cc.methods.SimpleRandom;

public abstract class Runner {

    protected final SimpleRandom random = new SimpleRandom();

    public abstract Case[] getCases();

    public abstract Parameter[] getParameters();

    public abstract String getMethodName();

    public abstract Class<?> getReturnType();

}
