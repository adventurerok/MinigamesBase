package com.ithinkrok.minigames.base.util.io;

import com.ithinkrok.minigames.base.GameState;
import com.ithinkrok.minigames.base.Kit;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.minigames.base.lang.LanguageLookup;
import com.ithinkrok.minigames.base.map.GameMapInfo;
import com.ithinkrok.minigames.base.schematic.Schematic;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

/**
 * Created by paul on 04/01/16.
 *
 * Holds things loaded from configs
 */
public interface ConfigHolder {

    void addListener(String name, Listener listener);

    void addCustomItem(CustomItem customItem);

    void addLanguageLookup(LanguageLookup languageLookup);

    void addSharedObject(String name, ConfigurationSection config);

    void addSchematic(Schematic schematic);

    void addTeamIdentifier(TeamIdentifier teamIdentifier);

    void addGameState(GameState gameState);

    void addKit(Kit kit);

    void addCommand(CommandConfig command);

    void addMapInfo(GameMapInfo mapInfo);
}
