package org.obicere.cc.process;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.configuration.DomainAccess;

/**
 * @author Obicere
 */
public abstract class StartingProcess extends DomainAccess implements Runnable, Comparable<StartingProcess> {

    public StartingProcess(final Domain access) {
        super(access);
    }

    public abstract int priority();

    @Override
    public int compareTo(final StartingProcess process) {
        return Integer.compare(priority(), process.priority());
    }

}
