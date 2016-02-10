package com.ithinkrok.minigames.base.util.io;

import com.ithinkrok.minigames.base.lang.LanguageLookup;
import com.ithinkrok.util.config.Config;

import java.nio.file.Path;

/**
 * Created by paul on 04/01/16.
 */
public interface FileLoader {

    Config loadConfig(String name);
    LanguageLookup loadLangFile(String name);
    Path getAssetDirectory();
}
