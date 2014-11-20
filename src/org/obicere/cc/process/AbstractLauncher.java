package org.obicere.cc.process;

import java.util.logging.Logger;

/**
 * @author Obicere
 */
public abstract class AbstractLauncher {

    protected final Logger log = Logger.getLogger(AbstractLauncher.class.getCanonicalName());

    public abstract void launch();

}
