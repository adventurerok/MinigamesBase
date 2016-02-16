package com.ithinkrok.minigames.base.protocol.data;

import com.ithinkrok.util.config.Config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by paul on 16/02/16.
 */
public class ControllerInfo {

    private final Map<String, GameGroupInfo> gameGroupInfoMap = new ConcurrentHashMap<>();

    public void clearGameGroups() {
        gameGroupInfoMap.clear();
    }

    public void updateGameGroup(Config gameGroupConfig) {
        String name = gameGroupConfig.getString("name");

        GameGroupInfo info = gameGroupInfoMap.get(name);
        if(info == null) {
            info = new GameGroupInfo(gameGroupConfig);

            gameGroupInfoMap.put(name, info);
        } else {
            info.fromConfig(gameGroupConfig);
        }
    }

    public void removeGameGroup(String name) {
        gameGroupInfoMap.remove(name);
    }
}
