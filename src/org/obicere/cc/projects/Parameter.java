package org.obicere.cc.projects;

public class Parameter {

    private final Class<?> cls;
    private final String   name;

    public Parameter(final Class<?> cls, final String name) {
        this.cls = cls;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return cls;
    }

}
