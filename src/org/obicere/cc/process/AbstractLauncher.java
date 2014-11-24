package org.obicere.cc.process;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.configuration.DomainAccess;
/**
 * @author Obicere
 */
public abstract class AbstractLauncher extends DomainAccess{

    public AbstractLauncher(final Domain access) {
        super(access);
    }

    public abstract void launch();

}
