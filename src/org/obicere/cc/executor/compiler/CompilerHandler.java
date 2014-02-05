package org.obicere.cc.executor.compiler;

import org.obicere.cc.configuration.Global;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * org.obicere.cc.executor.compiler
 * Created: 2/4/14 3:18 PM
 *
 * @author Obicere
 * @version 1.0
 */
public class CompilerHandler {

    private static final LinkedList<Compiler> LOADED_COMPILERS = new LinkedList<>();

    public static void loadCompilers() {
        try {
            final Enumeration<URL> files = CompilerHandler.class.getClassLoader().getResources(Global.URLs.COMPILERS);
            while(files.hasMoreElements()){
                final URL url = files.nextElement();
                System.out.println(url);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    public static Compiler getCompilerByName(final String name){
        for(final Compiler compiler : LOADED_COMPILERS){
            if(compiler.getName().equals(name)){
                return compiler;
            }
        }
        return null;
    }

}
