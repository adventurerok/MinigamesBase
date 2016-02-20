package com.ithinkrok.minigames.api.util.io;

import com.ithinkrok.minigames.api.map.GameMapInfo;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.schematic.Schematic;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;

/**
 * Created by paul on 04/01/16.
 *
 * Holds things loaded from configs
 */
public interface ConfigHolder {

    void addListener(String name, CustomListener listener);

    void addCustomItem(CustomItem customItem);

    void addLanguageLookup(LanguageLookup languageLookup);

    void addSharedObject(String name, Config config);

    void addSchematic(Schematic schematic);

    void addTeamIdentifier(TeamIdentifier teamIdentifier);

    void addGameState(GameState gameState);

    void addKit(Kit kit);

    void addCommand(CommandConfig command);

    void addMapInfo(GameMapInfo mapInfo);

    void addBook(JSONBook book);
}
