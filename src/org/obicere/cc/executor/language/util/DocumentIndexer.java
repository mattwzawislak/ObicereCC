package org.obicere.cc.executor.language.util;

/**
 * @author Obicere
 */
public interface DocumentIndexer<F extends Flag> {

    public Bound getBound();

    public F getFlag();

}
