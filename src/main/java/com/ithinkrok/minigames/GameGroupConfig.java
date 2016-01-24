package com.ithinkrok.minigames;

import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.schematic.Schematic;
import com.ithinkrok.minigames.team.TeamIdentifier;
import com.ithinkrok.minigames.util.io.ConfigHolder;
import com.ithinkrok.minigames.util.io.ConfigParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 24/01/16.
 */
public class GameGroupConfig {

    private final String name;
    private final ConfigurationSection baseConfig;

    private final String startGameStateName;
    private final String startMapName;

    public GameGroupConfig(String name, ConfigurationSection baseConfig) {
        this.name = name;
        this.baseConfig = baseConfig;

        startGameStateName = baseConfig.getString("start_game_state");
        startMapName = baseConfig.getString("start_map");
    }

    public GameGroup createGameGroup(Game game) {
        GameGroup gameGroup = new GameGroup(game);

        GameGroupLoader loader = new GameGroupLoader();
        
        //Name is the name of the config file to prevent it being duped
        ConfigParser.parseConfig(game, loader, gameGroup, gameGroup, name, baseConfig);
        
        gameGroup.setDefaultListeners(loader.listenerMap);
        gameGroup.setTeamIdentifiers(loader.teamIdentifierMap.values());
        gameGroup.setKits(loader.kitMap.values());
        gameGroup.setCustomItems(loader.customItemMap);
        gameGroup.setSchematics(loader.schematicMap);
        gameGroup.setGameStates(loader.gameStateMap);
        gameGroup.setSharedObjects(loader.sharedObjectMap);
        gameGroup.setLanguageLookups(loader.languageLookupList);

        gameGroup.prepareStart();
        gameGroup.changeGameState(startGameStateName);

        //This can be set to null if the start game state provides the map
        if(startMapName != null) gameGroup.changeMap(startMapName);

        return gameGroup;
    }

    private static class GameGroupLoader implements ConfigHolder {

        private HashMap<String, Listener> listenerMap = new HashMap<>();
        private HashMap<String, CustomItem> customItemMap = new HashMap<>();
        private HashMap<String, ConfigurationSection> sharedObjectMap = new HashMap<>();
        private HashMap<String, Schematic> schematicMap = new HashMap<>();
        private HashMap<String, TeamIdentifier> teamIdentifierMap = new HashMap<>();
        private HashMap<String, GameState> gameStateMap = new HashMap<>();
        private HashMap<String, Kit> kitMap = new HashMap<>();

        private List<LanguageLookup> languageLookupList = new ArrayList<>();

        @Override
        public void addListener(String name, Listener listener) {
            listenerMap.put(name, listener);
        }

        @Override
        public void addCustomItem(CustomItem customItem) {
            customItemMap.put(customItem.getName(), customItem);
        }

        @Override
        public void addLanguageLookup(LanguageLookup languageLookup) {
            languageLookupList.add(languageLookup);
        }

        @Override
        public void addSharedObject(String name, ConfigurationSection config) {
            sharedObjectMap.put(name, config);
        }

        @Override
        public void addSchematic(Schematic schematic) {
            schematicMap.put(schematic.getName(), schematic);
        }

        @Override
        public void addTeamIdentifier(TeamIdentifier teamIdentifier) {
            teamIdentifierMap.put(teamIdentifier.getName(), teamIdentifier);
        }

        @Override
        public void addGameState(GameState gameState) {
            gameStateMap.put(gameState.getName(), gameState);
        }

        @Override
        public void addKit(Kit kit) {
            kitMap.put(kit.getName(), kit);
        }
    }
}
