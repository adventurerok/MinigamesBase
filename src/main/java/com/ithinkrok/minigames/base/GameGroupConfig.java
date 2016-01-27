package com.ithinkrok.minigames.base;

/**
 * Created by paul on 24/01/16.
 */
public class GameGroupConfig {

    private final String name;
    private final String configFile;

    private String startGameStateName;
    private String startMapName;

    public GameGroupConfig(String name, String configFile) {
        this.name = name;
        this.configFile = configFile;
//        this.baseConfig = baseConfig;
//
//        startGameStateName = baseConfig.getString("start_game_state");
//        startMapName = baseConfig.getString("start_map");
    }


    public String getName() {
        return name;
    }

    public String getConfigFile() {
        return configFile;
    }

}
