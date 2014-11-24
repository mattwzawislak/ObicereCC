package org.obicere.cc.configuration;

import org.obicere.cc.executor.language.LanguageManager;
import org.obicere.cc.gui.AbstractFrameManager;
import org.obicere.cc.gui.Splash;
import org.obicere.cc.gui.SwingFrameManager;
import org.obicere.cc.process.StartingProcess;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.util.Updater;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Obicere
 */
public class Domain {

    private static final double CURRENT_CLIENT_VERSION = 1.00;

    private static Domain globalDomain;

    private volatile boolean fullyQualified = false;

    private Updater              updater;
    private AbstractFrameManager frameManager;
    private ShutDownHookManager  hookManager;
    private Splash               splash;
    private Paths                paths;
    private LanguageManager      languageManager;

    private List<StartingProcess> startingProcesses = new LinkedList<>();

    public Domain() {
        // Would be bloody amazing to get this to be dynamic... Reflection can be messy though
        // Initialize elements in access
        updater = new Updater(this);
        frameManager = new SwingFrameManager(this);
        hookManager = new ShutDownHookManager(this);
        splash = new Splash(this);
        paths = new Paths(this);
        languageManager = new LanguageManager(this);
        //
        startingProcesses.add(updater);
        startingProcesses.add(hookManager);
        startingProcesses.add(paths);
        startingProcesses.add(languageManager);
        Collections.sort(startingProcesses);
        // Then allow access to elements

        fullyQualified = true;
        globalDomain = this;
    }

    public static Domain getGlobalDomain() {
        globalDomain.checkQualification();
        return globalDomain;
    }

    private void checkQualification() {
        if (!fullyQualified) {
            throw new IllegalAccessError("Cannot access domain until it is fully qualified.");
        }
    }

    public List<StartingProcess> getStartingProcesses() {
        checkQualification();
        return startingProcesses;
    }

    public Updater getUpdater() {
        checkQualification();
        return updater;
    }

    public AbstractFrameManager getFrameManager() {
        checkQualification();
        return frameManager;
    }

    public ShutDownHookManager getHookManager() {
        checkQualification();
        return hookManager;
    }

    public Splash getSplash() {
        checkQualification();
        return splash;
    }

    public Paths getPaths() {
        checkQualification();
        return paths;
    }

    public LanguageManager getLanguageManager() {
        checkQualification();
        return languageManager;
    }

    public double getClientVersion() {
        return CURRENT_CLIENT_VERSION;
    }

}
