package org.obicere.cc.configuration;

import java.util.logging.Logger;

/**
 * @author Obicere
 */
public abstract class StartupTask implements Runnable {

    protected final Logger log = Logger.getLogger(StartupTask.class.getCanonicalName());

    protected final Domain domain;

    public StartupTask(final Domain domain) {
        this.domain = domain;
    }

    protected Domain getDomain() {
        return domain;
    }

}
