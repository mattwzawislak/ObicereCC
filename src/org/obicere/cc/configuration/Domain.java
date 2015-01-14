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
 * The domain provides access to common elements that contain information pertaining to different
 * classes across the application.
 * <p>
 * The domain provides global a global context to be shared by any aspect of the program. There
 * should only be one domain at a time. If there exists more than one domain, then there will
 * effectively be multiple application contexts. Each context will - most likely - be different,
 * which means global state variables may get mixed up.
 * <p>
 * There are measures to counter this error though. The boot should be responsible for instantiating
 * the domain. The dedicated boot can therefore also handle startup tasks.
 *
 * @author Obicere
 * @version 1.0
 * @see #getStartingProcesses()
 */
public class Domain {

    private static final double CURRENT_CLIENT_VERSION = 1.00;

    private volatile static Domain globalDomain;

    private volatile boolean fullyQualified = false;

    private Updater              updater;
    private AbstractFrameManager frameManager;
    private ShutDownHookManager  hookManager;
    private Splash               splash;
    private Paths                paths;
    private LanguageManager      languageManager;

    private List<StartingProcess> startingProcesses = new LinkedList<>();

    /**
     * Constructs a new global domain and qualifies it.
     *
     * @throws java.lang.AssertionError if a fully qualified domain already exists.
     */

    public Domain() {
        if (fullyQualified || globalDomain != null) {
            throw new AssertionError("A global domain has already been qualified.");
        }
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

    /**
     * Provides access to a domain without inheriting {@link org.obicere.cc.configuration.DomainAccess
     * access} to the domain. This value is instantiated and fully qualified once a domain has
     * already been made.
     * <p>
     * Note, that the initial domain and this domain are the same instance. Unless some reflection
     * trickery happens, but a good person wouldn't do that... would they?
     *
     * @return  The initial domain - fully qualified.
     */

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
