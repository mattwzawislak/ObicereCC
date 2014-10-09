package org.obicere.cc.shutdown;

import org.obicere.cc.gui.projects.ProjectSelector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SaveProgressHook extends ShutDownHook {

    public static final String NAME = "save.progress";

    @HookValue()
    public static final String PROGRESS_COMPLETE = "progress.complete";

    private final Map<String, Boolean> completeList = new HashMap<>();

    public SaveProgressHook() {
        super(NAME, ShutDownHook.PRIORITY_RUNTIME_SHUTDOWN);
        loadComplete();
    }

    public boolean isComplete(final String name) {
        Objects.requireNonNull(name);
        final Boolean complete = completeList.get(name);
        if (complete == null) {
            return false;
        }
        return complete;
    }

    public void setComplete(final String name, final boolean complete) {
        Objects.requireNonNull(name);
        completeList.put(name, complete);
        ProjectSelector.setComplete(name, complete);
        /*for (final ProjectPanel panel : ProjectSelector.getProjectList()) {
            if (panel.getProject().getName().equals(name)) {
                panel.setComplete(complete);
                return;
            }
        }*/
    }

    private void loadComplete() {
        final String listing = getPropertyAsString(PROGRESS_COMPLETE);
        final String[] tokens = listing.split(";");
        for (final String str : tokens) {
            completeList.put(str, true);
        }
    }

    @Override
    public void run() {
        final StringBuilder builder = new StringBuilder();
        completeList.forEach((name, complete) -> {
            if (complete) {
                final String token = String.format("%s;", name);
                builder.append(token);
            }
        });
        setProperty(PROGRESS_COMPLETE, builder);

        // Be sure to update the properties accordingly before writing them.
        // Somewhat detrimental for now.
        // TODO: create a before-run-after-run system
        super.run();
    }
}
