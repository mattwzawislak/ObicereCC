package org.obicere.cc.tasks.projects;

import org.obicere.cc.executor.Case;

public abstract class Runner {

    public abstract Case[] getCases();

    public abstract Parameter[] getParameters();

    public abstract String getMethodName();

    public abstract Class<?> getReturnType();

}
