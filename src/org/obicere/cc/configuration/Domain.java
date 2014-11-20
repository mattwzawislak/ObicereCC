package org.obicere.cc.configuration;

import org.obicere.cc.util.Updater;

/**
 * @author Obicere
 */
public class Domain {

    private static final double CURRENT_CLIENT_VERSION = 1.00;

    private volatile boolean fullyQualified = false;

    private Updater updater;

    public Domain() {
        // Initialize elements in domain
        updater = new Updater(this);
        // Then allow access to elements
        fullyQualified = true;
    }

    private void checkQualification() {
        if (!fullyQualified) {
            throw new IllegalAccessError("Cannot access domain until it is fully qualified.");
        }
    }

    public Updater getUpdater(){
        checkQualification();
        return updater;
    }

    public double getClientVersion() {
        return CURRENT_CLIENT_VERSION;
    }

}
