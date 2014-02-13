package org.obicere.cc.executor.language;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.executor.Result;
import org.obicere.cc.executor.compiler.CompilerCommand;
import org.obicere.cc.executor.compiler.Compiler;
import org.obicere.cc.tasks.projects.Project;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;

public abstract class Language {

    private final String name;
    private final String skeleton;
    private final File directory;
    private final String[] keywords;
    private final String[] literalsMatchers;
    private final Compiler compiler;

    protected Language(final String name) {
        try {
            this.name = name;
            this.directory = new File(Global.Paths.DATA, name);
            if (!directory.exists() && !directory.mkdir()) {
                System.err.println("Failed to create directory for " + name);
            }
            final URL comp = Language.class.getClassLoader().getResource(Global.URLs.COMPILERS + name);
            final URL lang = Language.class.getClassLoader().getResource(Global.URLs.LANGUAGES + name);
            if (comp == null || lang == null) {
                throw new AssertionError();
            }
            final Scanner compiler = new Scanner(comp.openStream());

            final String extension = compiler.nextLine();
            final String[] extensions = extension.split(";");
            final String sourceExt = extensions[0];
            final String compiledExt = extensions[1];

            final LinkedList<CompilerCommand> commandList = new LinkedList<>();
            while (compiler.hasNextLine()) {
                final String command = compiler.nextLine();
                final String[] commandData = command.split(";");
                commandList.add(new CompilerCommand(commandData[0], commandData[1]));
            }
            final CompilerCommand[] commands = commandList.toArray(new CompilerCommand[commandList.size()]);

            this.compiler = new Compiler(name, sourceExt, compiledExt, commands);

            final Scanner language = new Scanner(lang.openStream());
            this.keywords = language.nextLine().split(" ");
            this.skeleton = language.nextLine().replace("\\n", "\n").replace("\\t", "\t");

            final LinkedList<String> literalList = new LinkedList<>();
            while (language.hasNextLine()) {
                literalList.add(language.nextLine());
            }
            this.literalsMatchers = literalList.toArray(new String[literalList.size()]);
        } catch (final Exception e) {
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

    protected String getRawSkeleton(){
        return skeleton;
    }

    protected Compiler getCompiler() {
        return compiler;
    }

    public abstract String getSkeleton(final Project project);

    public abstract Result[] compileAndRun(final Project project);

    @Override
    public boolean equals(final Object obj){
        return obj instanceof Language && ((Language) obj).getName().equals(getName());
    }

}
