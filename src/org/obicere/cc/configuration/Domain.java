package org.obicere.cc.configuration;

import org.obicere.cc.executor.language.LanguageManager;
import org.obicere.cc.gui.swing.AbstractFrameManager;
import org.obicere.cc.gui.swing.Splash;
import org.obicere.cc.process.StartingProcess;
import org.obicere.cc.shutdown.ShutDownHookManager;
import org.obicere.cc.util.Updater;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The domain provides access to common elements that contain information
 * pertaining to different classes across the application.
 * <p>
 * The domain provides global a global context to be shared by any aspect
 * of the program. There should only be one domain at a time. If there
 * exists more than one domain, then there will effectively be multiple
 * application contexts. Each context will - most likely - be different,
 * which means global state variables may get mixed up.
 * <p>
 * There are measures to counter this error though. The boot should be
 * responsible for instantiating the domain. The dedicated boot can
 * therefore also handle startup tasks.
 *
 * @author Obicere
 * @version 1.0
 * @see #getStartingProcesses()
 * @see org.obicere.cc.configuration.DomainAccess
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
     * @throws java.lang.AssertionError if a fully qualified domain already
     *                                  exists.
     */

    public Domain() {
        if (fullyQualified || globalDomain != null) {
            throw new AssertionError("A global domain has already been qualified.");
        }
        // Would be bloody amazing to get this to be dynamic... Reflection can be messy though
        // Initialize elements in access
        updater = new Updater(this);
        //frameManager = new SwingFrameManager(this);
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
     * Provides access to a domain without inheriting {@link
     * org.obicere.cc.configuration.DomainAccess domain access} to the
     * domain. This value is instantiated and fully qualified once a domain
     * has already been made.
     * <p>
     * This was provided to allow domain access to classes that OOP-based
     * access would be unreasonable.
     * <p>
     * Note, that the initial domain and this domain are the same instance.
     * Unless some reflection trickery happens, but a good person wouldn't
     * do that... would they?
     *
     * @return The initial domain - fully qualified.
     */

    public static Domain getGlobalDomain() {
        globalDomain.checkQualification();
        return globalDomain;
    }

    /**
     * Ensures that the domain has been fully qualified. This method will
     * not block the thread in the case that an element required for domain
     * qualification attempts to access the domain before it has been
     * qualified. This is also why classes inheriting {@link
     * org.obicere.cc.configuration.DomainAccess domain access} should not
     * attempt to access other parts of the domain during their
     * construction.
     * <p>
     * That didn't make sense. This basically is like trying to create an
     * instance of an interface through reflection. Don't do it.
     *
     * @throws java.lang.IllegalAccessError If the domain has not been
     *                                      fully qualified.
     */

    private void checkQualification() {
        if (!fullyQualified) {
            throw new IllegalAccessError("Cannot access domain until it is fully qualified.");
        }
    }

    /**
     * Returns a list of all the processes that are considered a startup
     * utility. These processes often makes changes that the boot sequence
     * may not advocate. Such example would be a hypothetical boot
     * sequence, <code>NoUpdateBoot</code> where updates should not be
     * checked.
     * <p>
     * With the case of the <code>NoUpdateBoot</code> it obviously makes no
     * sense to actually update. So the boot sequence can choose to not run
     * the {@link org.obicere.cc.util.Updater updater } process, simply by
     * checking instances.
     * <p>
     * This allows for extra features to be enabled or disabled.
     *
     * @return The list of starting processes - sorted by priority.
     */

    public List<StartingProcess> getStartingProcesses() {
        checkQualification();
        return startingProcesses;
    }

    /**
     * Provides domain access to the updater. In the case that the
     * developer chooses to run post-startup updating tasks. As of v1.0 no
     * such cases have been made - but better be overly redundant than to
     * not not not be.
     * <p>
     * Right?
     * <p>
     * todo: stop questioning self so much
     *
     * @return The instance of the global updater.
     * @see org.obicere.cc.util.Updater
     */

    public Updater getUpdater() {
        checkQualification();
        return updater;
    }

    /**
     * Provides domain access to the frame manager. Note that the manager
     * is lazily set by the boot sequence, so it may not be readily
     * available. It is suggested to access the frame manager after the
     * boot sequence has finished and the process has been handed off to
     * the launcher.
     *
     * @return The instance of the currently used frame manager.
     */

    public AbstractFrameManager getFrameManager() {
        checkQualification();
        return frameManager;
    }

    /**
     * Sets the frame manager whenever needed. There are measures in place
     * to ensure that the manager is only ever set once. And to ensure that
     * a non-null manager was provided. Should either of these be violated,
     * the manager will not be set.
     * <p>
     * Note that this means that potentially unsafe access could still be
     * used to modify the manager. If do choose to go down this path,
     * please dispose of the original manager - however that is done - as
     * to not create memory leaks.
     *
     * @param manager Attempts to set the global frame manager instance to
     *                this instance.
     * @throws java.lang.NullPointerException If the given <code>manager</code>
     *                                        is <code>null</code>.
     * @throws java.lang.AssertionError       If a frame manager has
     *                                        already been set.
     */

    public void setFrameManager(final AbstractFrameManager manager) {
        if (manager == null) {
            throw new NullPointerException("Cannot set frame manager to null.");
        }
        if (this.frameManager != null) {
            throw new AssertionError("Frame manager already set.");
        }
        this.frameManager = manager;
    }

    /**
     * Provides domain access to the global hook manager. This is used for
     * both UI settings and program data that must persist. Access to the
     * hook manager - and other domain-access-points for that matter -
     * should only be done once the hook manager task is completed. This is
     * because the hook manager may update important settings to their
     * non-default values.
     * <p>
     * However, access has been granted in the case that a boot sequence
     * wishes to remove a certain feature of the hook manager, inject their
     * own hook, or just in general break things and blame me for it.
     * <p>
     * todo:consider adding a hook manager notify to signal when safe
     * access is available.
     *
     * @return The global hook manager instance.
     */

    public ShutDownHookManager getHookManager() {
        checkQualification();
        return hookManager;
    }

    /**
     * Provides access to the splash screen. This is used primarily to show
     * status updates during the boot sequence. Beyond that it is useless
     * and should be disposed of afterwards.
     * <p>
     * This is also globally available in case a developer wishes to
     * implement their own splash screen specific to the launcher.
     *
     * @return The default globally available splash screen.
     */

    public Splash getSplash() {
        checkQualification();
        return splash;
    }

    /**
     * Provides access to the path creator. This should always be the first
     * process to be completed, since any file access within the project's
     * folders require this. This will handle the default file system for
     * the program, so no usage for this is present as of v1.0.
     *
     * @return The default folder management system.
     */

    public Paths getPaths() {
        checkQualification();
        return paths;
    }

    /**
     * Provides access to the language files. This is used primarily in the
     * UI such that different languages can be selected by the user easily.
     * Domain access has been provided though, so that if additional
     * settings are added that pertain to the languages, the system won't
     * have to be redone.
     *
     * @return The global language manager instance.
     */

    public LanguageManager getLanguageManager() {
        checkQualification();
        return languageManager;
    }

    /**
     * Provides the client version. This has been provided here since this
     * pertains to a global field. That way if a system were to change
     * formats between versions, these changes can be reflected without
     * creating permanent changes. Also provides the client version to be
     * displayed on the top of the UI - if that feature is deemed desirable
     * by the launcher.
     *
     * @return The current client version.
     */

    public double getClientVersion() {
        return CURRENT_CLIENT_VERSION;
    }

}
