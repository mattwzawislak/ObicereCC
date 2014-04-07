package org.obicere.cc.executor.language;

import org.obicere.cc.executor.Result;
import org.obicere.cc.tasks.projects.Project;

import java.io.File;

/**
 * @author Obicere
 */
public class CustomLanguage extends Language {

    private Process aliveProcess;

    protected CustomLanguage(final String name, final File file) {
        super(name, file);
    }

    @Override
    public String getSkeleton(Project project) {
        return null;
    }

    @Override
    public Result[] compileAndRun(Project project) {
        return new Result[0];
    }

    public Process startProcess() {
        return null;
    }
}
