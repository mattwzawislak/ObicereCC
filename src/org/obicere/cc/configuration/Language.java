package org.obicere.cc.configuration;

/**
 * This adds abstract support for multiple languages.
 *
 * @author Obicere
 * @version 1.1
 */
public enum Language {

    JAVA("Java");

    private final String name;

    private Language(final String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }

}
