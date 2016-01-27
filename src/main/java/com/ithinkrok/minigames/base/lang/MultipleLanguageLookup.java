package com.ithinkrok.minigames.base.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 01/01/16.
 */
public class MultipleLanguageLookup implements LanguageLookup {

    private List<LanguageLookup> languageLookupList;

    public MultipleLanguageLookup() {
        this(new ArrayList<>());
    }

    public MultipleLanguageLookup(List<LanguageLookup> languageLookupList) {
        this.languageLookupList = languageLookupList;
    }

    public void addLanguageLookup(LanguageLookup add) {
        languageLookupList.add(0, add); //This is only done a few times. ArrayList is used due to faster iteration
    }

    public void removeLanguageLookup(LanguageLookup remove) {
        languageLookupList.remove(remove);
    }

    @Override
    public String getLocale(String name) {
        for(LanguageLookup lookup : languageLookupList){
            if(!lookup.hasLocale(name)) continue;

            return lookup.getLocale(name);
        }

        return null;
    }

    @Override
    public String getLocale(String name, Object... args) {
        for(LanguageLookup lookup : languageLookupList){
            if(!lookup.hasLocale(name)) continue;

            return lookup.getLocale(name, args);
        }

        return null;
    }

    @Override
    public boolean hasLocale(String name) {
        for(LanguageLookup lookup : languageLookupList) {
            if(lookup.hasLocale(name)) return true;
        }

        return false;
    }
}
