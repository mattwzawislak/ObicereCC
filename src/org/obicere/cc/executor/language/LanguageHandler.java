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
public class LanguageHandler {

    private static final LinkedList<Language> LOADED_LANGUAGES = new LinkedList<>();
    private static final XMLParser XML_PARSER = XMLParser.getInstance();
    private static final FileFilter LANGUAGE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File pathname) {
            return pathname.getName().endsWith(".lng");
        }
    };

    public static Language loadFromFile(final File file){
        try{
            Splash.setStatus("Loading language " + file.getName());
            XML_PARSER.prepare(file);
            final Map<String, String> map = XML_PARSER.getAttributeMapping();
            final String name = map.get("name");
            final String keywordListing = map.get("keyword");
            final String literalListing = map.get("literal");
            final BinaryList<String> keywordList = new BinaryList<>();
            final LinkedList<String> literalList = new LinkedList<>();

            Collections.addAll(keywordList, keywordListing.split(", "));
            Collections.addAll(literalList, literalListing.replace("$quot;", "\"").split(", "));

            return new Language(name, keywordList, literalList);
        } catch(final Exception e){
            Splash.setStatus("Failed to load language from " + file.getName());
            e.printStackTrace();
            return null;
        }
    }

    public static List<Language> getLoadedLanguage(){
        return LOADED_LANGUAGES;
    }

    public static void loadLanguages(){
        final File parent = new File(Global.Paths.LANGUAGE);
        for(final File file : parent.listFiles(LANGUAGE_FILTER)){
            final Language language = loadFromFile(file);
            LOADED_LANGUAGES.add(language);
        }
    }
    public static Language byName(final String name){
        for(final Language language : LOADED_LANGUAGES){
            if(language.getName().equals(name)){
                return language;
            }
        }
        return null;
    }

}