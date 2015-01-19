package org.obicere.cc.configuration;

import java.util.logging.Logger;

/**
 * Provides a basis for domain access. This will include a protected {@link
 * java.util.logging.Logger logger}. This method denotes the class will
 * utilize the domain and therefore must gain non-global access to the
 * domain.
 *
 * @author Obicere
 * @version 1.0
 * @see org.obicere.cc.configuration.Domain
 */

public abstract class DomainAccess {

    protected final Logger log = Logger.getLogger(DomainAccess.class.getCanonicalName());

    protected final Domain access;

    /**
     * Constructs a domain access point to allow the inheriting class full
     * access to the domain.
     *
     * @param access The instance of the fully qualified domain.
     */

    public DomainAccess(final Domain access) {
        this.access = access;
    }

    /**
     * Accesses the domain through a method as opposed to {@link #access
     * DomainAccess#access} - which may not be present in future releases
     * since it breaks encapsulation. In general, this method should be
     * used instead of field access.
     *
     * @return Accesses the domain safely for the current release (v1.0)
     * and future releases.
     */

    protected Domain getAccess() {
        return access;
    }

}
