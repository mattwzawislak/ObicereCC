package org.obicere.cc.executor.language;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.Command;
import org.obicere.cc.executor.compiler.Compiler;
import org.obicere.cc.gui.GUI;
import org.obicere.cc.gui.projects.Editor;
import org.obicere.cc.tasks.projects.Project;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public abstract class Language {

    private final boolean includeParameters;

    private final String stringType;
    private final String characterType;
    private final String integerType;
    private final String floatType;
    private final String arrayOpen;
    private final String arrayClose;
    private final String name;
    private final String skeleton;

    private final File directory;
    private final String[] keywords;
    private final String[] literalsMatchers;
    private final Compiler compiler;
    private final Command exectorCommand;

    private final Casing fieldCasing;
    private final Casing methodCasing;
    private final Casing classCasing;

    protected Language(final String name, final File file) {
        try {
            this.name = name;
            this.directory = new File(Global.Paths.DATA, name);
            if (!directory.exists() && !directory.mkdir()) {
                System.err.println("Failed to create directory for " + name);
            }

            final Properties properties = new Properties();
            properties.load(new FileInputStream(file));

            this.keywords = properties.getProperty("keywords", "").split(",");
            this.literalsMatchers = properties.getProperty("literal", "").split(",");
            this.skeleton = properties.getProperty("skeleton", "").replace("\\n", "\n").replace("\\t", "\t");

            this.fieldCasing = Casing.forName(properties.getProperty("fieldCasing", ""));
            this.methodCasing = Casing.forName(properties.getProperty("methodCasing", ""));
            this.classCasing = Casing.forName(properties.getProperty("classCasing", ""));
            this.includeParameters = Boolean.valueOf(properties.getProperty("includeParameters", "false"));

            this.stringType = properties.getProperty("string", "");
            this.characterType = properties.getProperty("character", "");
            this.integerType = properties.getProperty("integer", "");
            this.floatType = properties.getProperty("float", "");

            final String[] executor = properties.getProperty("executorCommand", " ; ").split(";");
            this.exectorCommand = new Command(executor[0], executor[1]);

            final String[] array = properties.getProperty("array", ",").split(",");
            this.arrayOpen = array[0];
            this.arrayClose = array[1];

            final String compiledExt = properties.getProperty("compiledExtension", "");
            final String sourceExt = properties.getProperty("sourceExtension", "");
            final String[] commandValues = properties.getProperty("compilerArguments").split(",");
            final Command[] commands = new Command[commandValues.length];
            for (int i = 0; i < commandValues.length; i++) {
                final String[] command = commandValues[i].split(";", 2);
                commands[i] = new Command(command[0], command[1]);
            }

            this.compiler = new Compiler(name, sourceExt, compiledExt, commands);

        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load language: " + name);
            throw new IllegalArgumentException();
        }
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

    public String[] getLiteralMatchers() {
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
        }
        for (int i = 0; i < size; i++) {
            builder.append(arrayClose);
        }
        return builder.toString();
    }

    public Command getExecutorCommand() {
        return exectorCommand;
    }

    public void displayError(final Project project, final String[] error) {
        final Editor editor = GUI.tabByName(project.getName(), this);
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
