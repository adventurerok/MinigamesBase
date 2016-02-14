package com.ithinkrok.minigames.base.util.io;

import com.ithinkrok.minigames.base.GameState;
import com.ithinkrok.minigames.base.Kit;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.minigames.base.map.GameMapInfo;
import com.ithinkrok.minigames.base.schematic.Schematic;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomListener;

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
}
