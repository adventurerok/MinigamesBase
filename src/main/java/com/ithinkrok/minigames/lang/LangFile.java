package com.ithinkrok.minigames.lang;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by paul on 20/11/15.
 *
 * Handles a language properties file
 */
public class LangFile implements LanguageLookup{

    private Map<Object, String> languageStrings = new HashMap<>();

    public LangFile(File in) throws IOException {
        this(new FileInputStream(in));
    }

    public LangFile(Path in) throws IOException {
        this(Files.newInputStream(in));
    }

    public LangFile(InputStream in) throws IOException {
        this(loadProperties(in));
    }

    private static Properties loadProperties(InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.load(in);

        return properties;
    }

    public LangFile(Properties properties) {
        for(Object key : properties.keySet()){
            String value = properties.getProperty(key.toString());
            value = value.replace('&', '§');
            languageStrings.put(key, value);
        }
    }

    @Override
    public String getLocale(String locale){
        String result = languageStrings.get(locale);

        if(result == null){
            try{
                //print the stack trace
                throw new RuntimeException("Missing language string for: " + locale);
            } catch(RuntimeException e){
                e.printStackTrace();
            }

            languageStrings.put(locale, locale);
            return locale;
        }

        return result;
    }

    @Override
    public String getLocale(String locale, Object...args){
        return String.format(getLocale(locale), args);
    }

    @Override
    public boolean hasLocale(String name) {
        String result = languageStrings.get(name);
        return result != null && !result.equals(name);
    }
}
