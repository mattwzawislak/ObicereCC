package org.obicere.cc.shutdown;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.methods.IOUtils;
import org.obicere.cc.tasks.projects.Project;

import java.io.File;
import java.math.BigInteger;


public class SaveProgressHook extends ShutDownHook {

    private static final String NAME = "save.progress";

    public SaveProgressHook() {
        super(NAME, ShutDownHook.PRIORITY_RUNTIME_SHUTDOWN);
    }

    @Override
    public void run() {
        final StringBuilder builder = new StringBuilder();
        for (final Project project : Project.DATA) {
            if (project.isComplete()) {
                final String complete = String.format("|%040x|", new BigInteger(project.getName().getBytes()));
                builder.append(complete);
            }
        }
        try {
            final byte[] data = builder.toString().getBytes();
            IOUtils.write(new File(Global.Paths.DATA, "data.dat"), data);
        } catch (final Exception e) {
            System.err.println("Failed to save progress!");
            e.printStackTrace();
        }
    }
}
