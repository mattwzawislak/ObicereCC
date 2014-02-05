package org.obicere.cc.executor.language;

import org.obicere.cc.executor.compiler.Compiler;
import org.obicere.cc.executor.compiler.CompilerHandler;

import java.util.Collections;
import java.util.List;

/**
 * @author Obicere
 */
public class Language {

    private final String name;
    private final List<String> keywords;
    private final List<String> literalMatchers;
    private final Compiler compiler;

    public Language(final String name, final List<String> keywords, final List<String> literalMatchers) {
        this.name = name;
        this.compiler = CompilerHandler.getCompilerByName(name);
        this.keywords = keywords;
        this.literalMatchers = literalMatchers;
    }

    public List<String> getLiteralMatchers() {
        return literalMatchers;
    }

    public boolean isKeyword(final String word) {
        return Collections.binarySearch(keywords, word) >= 0;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

}
