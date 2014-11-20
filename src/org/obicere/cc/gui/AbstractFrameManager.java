package org.obicere.cc.gui;

import org.obicere.cc.configuration.Domain;
import org.obicere.cc.configuration.DomainAccess;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.gui.projects.Editor;
import org.obicere.cc.projects.Project;
import org.obicere.cc.shutdown.ShutDownHook;

import java.awt.Window;

/**
 * @author Obicere
 */
public abstract class AbstractFrameManager extends DomainAccess {

    public AbstractFrameManager(final Domain access) {
        super(access);
    }

    public abstract Window getWindow();

    public abstract void openProject(final Project project, final Language language);

    public abstract <T> T getTab(final Class<T> cls);

    public abstract Editor getTab(final String name, final Language language);

    public abstract void removeTab(final String name, final Language language);

    public abstract void addWindowClosingHook(final ShutDownHook hook);

    public void buildGUI() {
        run();
    }

}
