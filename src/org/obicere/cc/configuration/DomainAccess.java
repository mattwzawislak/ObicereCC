package org.obicere.cc.configuration;

import java.util.logging.Logger;

/**
 * @author Obicere
 */
public abstract class DomainAccess {

    protected final Logger log = Logger.getLogger(DomainAccess.class.getCanonicalName());

    protected final Domain access;

    public DomainAccess(final Domain access) {
        this.access = access;
    }

    protected Domain getAccess() {
        return access;
    }

}
