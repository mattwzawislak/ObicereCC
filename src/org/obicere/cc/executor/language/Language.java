package org.obicere.cc.executor.language;

import org.obicere.cc.configuration.Global;
import org.obicere.cc.gui.Splash;
import org.obicere.utility.BinaryList;
import org.obicere.utility.io.XMLParser;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Obicere
 */
public class Language {

    private final String name;
    private final List<String> keywords;
    private final List<String> literalMatchers;


    public Language(final String name, final List<String> keywords, final List<String> literalMatchers){
        this.name = name;
        this.keywords = keywords;
        this.literalMatchers = literalMatchers;
    }

    public List<String> getLiteralMatchers(){
        return literalMatchers;
    }

    public boolean isKeyword(final String word){
        return Collections.binarySearch(keywords, word) >= 0;
    }

    public String getName(){
        return name;
    }

    public String toString(){
        return name;
    }

}
