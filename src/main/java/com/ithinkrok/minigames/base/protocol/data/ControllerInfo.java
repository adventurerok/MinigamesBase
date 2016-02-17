package com.ithinkrok.minigames.base.protocol.data;

import com.ithinkrok.util.config.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by paul on 16/02/16.
 */
public class ControllerInfo {

    private final Map<String, GameGroupInfo> gameGroupInfoMap = new ConcurrentHashMap<>();

    public void clearGameGroups() {
        gameGroupInfoMap.clear();
    }

    public GameGroupInfo updateGameGroup(Config gameGroupConfig) {
        String name = gameGroupConfig.getString("name");

        GameGroupInfo info = gameGroupInfoMap.get(name);
        if(info == null) {
            info = new GameGroupInfo(this, gameGroupConfig);

            gameGroupInfoMap.put(name, info);
        } else {
            info.fromConfig(gameGroupConfig);
        }

        return info;
    }

    public GameGroupInfo getGameGroup(String name) {
        return gameGroupInfoMap.get(name);
    }

    public GameGroupInfo removeGameGroup(String name) {
        return gameGroupInfoMap.remove(name);
    }

    public Collection<GameGroupInfo> getAcceptingGameGroups(String type) {
        Collection<GameGroupInfo> result = new HashSet<>();

        for(GameGroupInfo gameGroupInfo : gameGroupInfoMap.values()) {
            if(!gameGroupInfo.getType().equals(type)) continue;
            if(!gameGroupInfo.isAcceptingPlayers()) continue;

            result.add(gameGroupInfo);
        }

        return result;
    }
}
