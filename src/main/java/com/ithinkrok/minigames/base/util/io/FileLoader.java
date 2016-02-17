package com.ithinkrok.minigames.base.util.io;

import com.ithinkrok.minigames.base.util.JSONBook;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.config.Config;

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
