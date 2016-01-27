package com.ithinkrok.minigames.base.util.io;

import com.ithinkrok.minigames.base.lang.LanguageLookup;
import org.bukkit.configuration.ConfigurationSection;

import java.nio.file.Path;

/**
 * Created by paul on 04/01/16.
 */
public interface FileLoader {

    ConfigurationSection loadConfig(String name);
    LanguageLookup loadLangFile(String name);
    Path getAssetDirectory();
}
