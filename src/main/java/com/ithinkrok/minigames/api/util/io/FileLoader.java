package com.ithinkrok.minigames.api.util.io;

import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LanguageLookup;

import java.nio.file.Path;

/**
 * Created by paul on 04/01/16.
 */
public interface FileLoader {

    Config loadConfig(String path);
    LanguageLookup loadLangFile(String path);
    JSONBook loadBook(String name, String path);
    Path getAssetDirectory();
}
