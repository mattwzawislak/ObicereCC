package org.obicere.cc.executor.language;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.Command;
import org.obicere.cc.executor.compiler.Compiler;
import org.obicere.cc.gui.FrameManager;
import org.obicere.cc.gui.projects.Editor;
import org.obicere.cc.methods.Reflection;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Language {

    private static final Logger LOGGER = Logger.getLogger(Language.class.getCanonicalName());

    private final Class<? extends Language> subclass;
    private final boolean                   includeParameters;

    private final String stringType;
    private final String characterType;
    private final String integerType;
    private final String floatType;
    private final String arrayOpen;
    private final String arrayClose;
    private final String name;
    private final String skeleton;

    private final File     directory;
    private final String[] keywords;
    private final String[] literalsMatchers;
    private final Compiler compiler;
    private final Command  executorCommand;

    private final Casing fieldCasing;
    private final Casing methodCasing;
    private final Casing classCasing;

    protected Language(final String name, final Class<? extends Language> subclass) {
        try {
            this.subclass = subclass;
            this.name = name;
            this.directory = new File(Global.Paths.DATA, name);
            if (!directory.exists() && !directory.mkdir()) {
                LOGGER.log(Level.WARNING, "Failed to create directory for " + name);
            }

            this.keywords = loadField("KEYWORDS").split(",");
            this.literalsMatchers = loadField("LITERAL").split(",");
            this.skeleton = loadField("SKELETON").replace("\\n", "\n").replace("\\t", "\t");

            this.fieldCasing = Casing.forName(loadField("FIELD_CASING"));
            this.methodCasing = Casing.forName(loadField("METHOD_CASING"));
            this.classCasing = Casing.forName(loadField("CLASS_CASING"));
            this.includeParameters = Boolean.valueOf(loadField("INCLUDE_PARAMETERS"));

            this.stringType = loadField("STRING");
            this.characterType = loadField("CHARACTER");
            this.integerType = loadField("INTEGER");
            this.floatType = loadField("FLOAT");

            final String[] executor = loadField("EXECUTOR_COMMAND").split(";");
            this.executorCommand = new Command(executor[0], executor[1]);

            final String[] array = loadField("ARRAY").split(",");
            this.arrayOpen = array[0];
            this.arrayClose = array[1];

            final String compiledExt = loadField("COMPILED_EXTENSION");
            final String sourceExt = loadField("SOURCE_EXTENSION");
            final String[] commandValues = loadField("COMPILER_ARGUMENTS").split(",");
            final Command[] commands = new Command[commandValues.length];
            for (int i = 0; i < commandValues.length; i++) {
                final String[] command = commandValues[i].split(";", 2);
                commands[i] = new Command(command[0], command[1]);
            }

            this.compiler = new Compiler(name, sourceExt, compiledExt, commands);

        } catch (final Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Failed to load language: " + name);
            throw new IllegalArgumentException();
        }
    }

    private String loadField(final String name) {
        return (String) Reflection.getStaticField(subclass, name);
    }

    public boolean isKeyword(final String word) {
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

    public String[] getLiteralMatches() {
        return literalsMatchers;
    }

    public String getName() {
        return name;
    }

    public String getSourceExtension() {
        return compiler.getSourceExtension();
    }

    public String getCompiledExtension() {
        return compiler.getCompiledExtension();
    }

    public File getDirectory() {
        return directory;
    }

    protected String getRawSkeleton() {
        return skeleton;
    }

    protected Compiler getCompiler() {
        return compiler;
    }

    public abstract String getSkeleton(final Project project);

    public abstract Result[] compileAndRun(final Project project);

    public String fieldCase(final String token) {
        return fieldCasing.performCase(token);
    }

    public String methodCase(final String token) {
        return methodCasing.performCase(token);
    }

    public String classCase(final String token) {
        return classCasing.performCase(token);
    }

    public boolean displayParameters() {
        return includeParameters;
    }

    public String getStringType() {
        return stringType;
    }

    public String getCharacterType() {
        return characterType;
    }

    public String getIntegerType() {
        return integerType;
    }

    public String getFloatType() {
        return floatType;
    }

    public String getArrayOpen() {
        return arrayOpen;
    }

    public String getArrayClose() {
        return arrayClose;
    }

    public String getArray(final int size) {
        if (size <= 0) {
            return "";
        }
        final StringBuilder builder = new StringBuilder(size * 2);
        for (int i = 0; i < size; i++) {
            builder.append(arrayOpen);
            builder.append(arrayClose);
        }
        return builder.toString();
    }

    public Command getExecutorCommand() {
        return executorCommand;
    }

    public void displayError(final Project project, final String[] error) {
        final Editor editor = FrameManager.tabByName(project.getName(), this);
        final StringBuilder builder = new StringBuilder();
        final String path = project.getFile(this).getAbsolutePath();
        for (final String str : error) {
            builder.append(str.replace(path, "line"));
            builder.append("\n");
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

}
