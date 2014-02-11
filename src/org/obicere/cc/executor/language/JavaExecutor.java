package org.obicere.cc.executor.language;

import org.obicere.cc.executor.Case;
import org.obicere.cc.executor.Result;
import org.obicere.cc.gui.GUI;
import org.obicere.cc.gui.projects.Editor;
import org.obicere.cc.methods.CustomClassLoader;
import org.obicere.cc.tasks.projects.Parameter;
import org.obicere.cc.tasks.projects.Project;
import org.obicere.cc.tasks.projects.Runner;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Obicere
 */
public class JavaExecutor extends Language {

    protected JavaExecutor() {
        super("Java");
    }

    @Override
    public Result[] compileAndRun(final Project project) {
        final File file = project.getFile(this);
        final String[] message = getCompiler().compile(file);
        if (message.length == 0) {
            try {
                final Class<?> cls = project.getRunner();
                final Runner runner = (Runner) cls.newInstance();
                final Parameter[] parameters = runner.getParameters();
                final Case[] cases = runner.getCases();
                final Class<?>[] searchClasses = new Class<?>[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    searchClasses[i] = parameters[i].getType();
                }

                final File compiledFile = new File(getDirectory(), project.getName() + getCompiledExtension());
                final Class<?> invoke = CustomClassLoader.loadClassFromFile(compiledFile);

                final Method method = invoke.getDeclaredMethod(runner.getMethodName(), searchClasses);

                final Result[] results = new Result[cases.length];
                for (int i = 0; i < results.length; i++) {
                    final Case thisCase = cases[i];
                    final Object result = method.invoke(invoke.newInstance(), thisCase.getParameters());
                    results[i] = new Result(result, thisCase.getExpectedResult(), thisCase.getParameters());
                }
                return results;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        final Editor editor = GUI.tabByName(project.getName(), this);
        final StringBuilder builder = new StringBuilder();
        for (final String str : message) {
            builder.append(str.replace(file.getAbsolutePath(), "line"));
            builder.append("\n");
        }
        if (editor != null) {
            editor.setInstructionsText(builder.toString(), true);
        } else {
            JOptionPane.showMessageDialog(null, builder.toString());
        }
        return new Result[0];
    }

    public String getSkeleton(final Project project) {
        try {
            final Class<?> cls = project.getRunner();
            final Runner runner = (Runner) cls.newInstance();

            final Parameter[] parameterList = runner.getParameters();
            final String returnType = runner.getReturnType().getCanonicalName();
            final String methodName = runner.getMethodName();

            final StringBuilder parameters = new StringBuilder();
            for (final Parameter parameter : parameterList) {
                if (parameters.length() != 0) {
                    parameters.append(", ");
                }
                parameters.append(parameter.getType().getCanonicalName());
                parameters.append(' ');
                parameters.append(parameter.getName());
            }

            String skeleton = getRawSkeleton();
            skeleton = skeleton.replace("$parameter", parameters.toString());
            skeleton = skeleton.replace("$name", project.getName());
            skeleton = skeleton.replace("$return", returnType);
            return skeleton.replace("$method", methodName);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
