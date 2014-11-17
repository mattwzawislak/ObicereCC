package org.obicere.cc.executor.language;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.Command;
import org.obicere.cc.executor.compiler.ProcessExecutor;
import org.obicere.cc.gui.FrameManager;
import org.obicere.cc.gui.projects.Editor;
import org.obicere.cc.projects.Parameter;
import org.obicere.cc.projects.Project;
import org.obicere.cc.util.StringSubstitute;

import javax.swing.JOptionPane;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class Language {

    private static final Logger log = Logger.getLogger(Language.class.getCanonicalName());

    private final String name;

    private final File            directory;
    private final ProcessExecutor processExecutor;

    private LanguageStreamer streamer;

    protected Language(final String name) {
        try {
            this.name = name;
            this.directory = new File(Global.Paths.DATA, name);
            if (!directory.exists() && !directory.mkdir()) {
                log.log(Level.WARNING, "Failed to create directory for " + name);
            }
            final String src = getSourceExtension();
            final String cmp = getCompiledExtension();
            final Command[] commands = getCommands();

            this.processExecutor = new ProcessExecutor(name, src, cmp, commands);

        } catch (final Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Failed to load language: " + name);
            throw new IllegalArgumentException();
        }
    }

    public void requestStreamer() {
        this.streamer = new LanguageStreamer(this);
    }

    public LanguageStreamer streamer() {
        return streamer;
    }

    public boolean isKeyword(final String word) {
        final String[] keywords = getKeyWords();

        int low = 0;
        int high = keywords.length - 1;

        while (low <= high) {
            final int mid = (low + high) / 2;
            final int comp = keywords[mid].compareTo(word);

            if (comp < 0) {
                low = mid + 1;
            } else if (comp > 0) {
                high = mid - 1;
            } else {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public File getDirectory() {
        return directory;
    }

    protected ProcessExecutor getProcessExecutor() {
        return processExecutor;
    }


    public String getSkeleton(final Project project) {
        try {

            final Class<?> returnType = project.getRunner().getReturnType();

            final String skeleton = getRawSkeleton();
            final StringSubstitute substitute = new StringSubstitute();

            substitute.put("parameter", buildParameters(project));
            substitute.put("method", getMethodString(project));
            substitute.put("name", getClassString(project));
            substitute.put("return", getStringForClass(returnType));

            return substitute.apply(skeleton);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getMethodString(final Project project) {
        final Casing methodCasing = getMethodCasing();
        final String methodName = project.getRunner().getMethodName();
        if (methodCasing != null) {
            return methodCasing.performCase(methodName);
        } else {
            return methodName;
        }
    }

    private String getClassString(final Project project) {
        final Casing classCasing = getClassCasing();
        final String className = project.getName();
        if (classCasing != null) {
            return classCasing.performCase(className);
        } else {
            return className;
        }
    }

    protected abstract Casing getParameterCasing();

    protected abstract Casing getMethodCasing();

    protected abstract Casing getClassCasing();

    public int getTabSize() {
        return 4;
    }

    protected abstract boolean shouldDisplayParameterTypes();

    protected String getStringType() {
        return "";
    }

    protected String getCharacterType() {
        return "";
    }

    protected String getIntegerType() {
        return "";
    }

    protected String getFloatType() {
        return "";
    }

    protected String getBooleanType() {
        return "";
    }

    protected String getArrayOpen() {
        return "";
    }

    protected String getArrayClose() {
        return "";
    }

    public abstract String getSourceExtension();

    public abstract String getCompiledExtension();

    protected abstract String getRawSkeleton();

    protected abstract String[] getKeyWords();

    public abstract String[] getLiteralMatches();

    protected abstract Command[] getCommands();

    public abstract Result[] compileAndRun(final Project project);

    public abstract CodeFormatter getCodeFormatter();

    private String getArray(final int size) {
        if (size <= 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(size * 2);
        for (int i = 0; i < size; i++) {
            builder.append(getArrayOpen());
            builder.append(getArrayClose());
        }
        return builder.toString();
    }

    private int getArrayDimension(final Class<?> cls) {

        int count = 0;
        Class<?> subCls = cls;
        while (subCls.isArray()) {
            subCls = subCls.getComponentType();
            count++;
        }
        return count;
    }

    public String buildParameters(final Project project) {
        final StringBuilder builder = new StringBuilder();
        final Parameter[] params = project.getRunner().getParameters();

        final boolean displayTypes = shouldDisplayParameterTypes();

        final Casing param = getParameterCasing();

        for (int i = 0; i < params.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            if (displayTypes) {
                builder.append(getStringForClass(params[i].getType()));
            }
            if (param != null) {
                builder.append(param.performCase(params[i].getName()));
            } else {
                builder.append(params[i].getName());
            }
        }
        return builder.toString();
    }

    private String getStringForClass(final Class<?> cls) {
        System.out.println(cls);
        final StringBuilder builder = new StringBuilder();
        final String clsName = cls.getSimpleName().replaceAll("(\\[|\\])+", "");
        switch (clsName) {
            case "int":
            case "Integer":
                builder.append(getIntegerType());
                break;

            case "char":
            case "Character":
                builder.append(getCharacterType());
                break;

            case "float":
            case "Float":
            case "double":
            case "Double":
                builder.append(getFloatType());
                break;

            case "boolean":
            case "Boolean":
                builder.append(getBooleanType());
                break;

            case "String":
                builder.append(getStringType());
                break;
        }
        final int count = getArrayDimension(cls);
        if (count >= 1) {
            builder.append(getArray(count));
        }
        return builder.toString();
    }

    public void displayError(final Project project, final String... error) {
        final Editor editor = FrameManager.tabByName(project.getName(), this);
        final StringBuilder builder = new StringBuilder();
        final String path = project.getFile(this).getAbsolutePath();
        for (final String str : error) {
            builder.append(str.replace(path, "line"));
            builder.append(System.lineSeparator());
        }
        if (editor != null) {
            editor.setInstructionsText(builder.toString(), true);
        } else {
            JOptionPane.showMessageDialog(null, builder.toString());
        }
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Language && ((Language) obj).getName().equals(getName());
    }

    @Override
    public String toString() {
        return name;
    }

}
