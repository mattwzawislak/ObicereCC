package org.obicere.cc.executor.language;

import org.obicere.cc.executor.Case;
import org.obicere.cc.executor.Result;
import org.obicere.cc.tasks.projects.Parameter;
import org.obicere.cc.tasks.projects.Project;
import org.obicere.cc.tasks.projects.Runner;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

@LanguageIdentifier
public class JavaLanguage extends Language {

    //keywords
    public static final String KEYWORDS = "abstract,assert,boolean,break,byte,case,catch,char,class,const,continue,default,double,do,else,enum,extends,false,final,finally,float,for,goto,if,implements,import,instanceof,int,interface,long,native,new,null,package,private,protected,public,return,short,static,strictfp,super,switch,synchronized,this,throw,throws,transient,true,try,void,volatile,while";
    //skeleton
    public static final String SKELETON = "public class $name {\n\t\n\tpublic $return $method($parameter){\n\t\t\n\t}\n}";

    //literal
    public static final String LITERAL = "\"(?:[^\"\\\\\\n\\r\\u2028\\u2029]|\\\\(?:[^\\n\\rxu0-9]|0(?![0-9])|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|\\n|\\r\\n?))*\",(//.*+)|(?s)(/[*].*?[*]/)";

    //compiledExtension
    public static final String COMPILED_EXTENSION = ".class";
    //sourceExtension
    public static final String SOURCE_EXTENSION   = ".java";

    //compilerArguments
    public static final String COMPILER_ARGUMENTS = "javac; $exec -g -nowarn \"$file\"";

    //methodCasing
    public static final String METHOD_CASING      = "lower camel case";
    //fieldCasing
    public static final String FIELD_CASING       = "lower camel case";
    //classCasing
    public static final String CLASS_CASING       = "camel case";
    //includeParameters
    public static final String INCLUDE_PARAMETERS = "true";

    //executorCommand
    public static final String EXECUTOR_COMMAND = " ; ";

    //string
    public static final String STRING    = "String";
    //character
    public static final String CHARACTER = "char";
    //integer
    public static final String INTEGER   = "int";
    //float
    public static final String FLOAT     = "double";
    //array
    public static final String ARRAY     = "[,]";


    public JavaLanguage() {
        super("Java", JavaLanguage.class);
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

                final URL[] urls = new URL[]{getDirectory().toURI().toURL()};
                final ClassLoader cl = new URLClassLoader(urls);
                final Class<?> invoke = cl.loadClass(project.getName());
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
                return new Result[0];
            }
        }
        displayError(project, message);
        return new Result[0];
    }
}
