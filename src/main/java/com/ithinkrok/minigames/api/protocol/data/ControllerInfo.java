package com.ithinkrok.minigames.api.protocol.data;

import com.ithinkrok.util.config.Config;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

    public Collection<GameGroupInfo> getAcceptingGameGroups(String type, List<String> params) {
        Collection<GameGroupInfo> result = new HashSet<>();

        for(GameGroupInfo gameGroupInfo : gameGroupInfoMap.values()) {
            if(type != null) {
                if (!gameGroupInfo.getType().equals(type)) continue;
                if(!params.isEmpty() && !params.equals(gameGroupInfo.getParams())) continue;
            }
            if(!gameGroupInfo.isAcceptingPlayers()) continue;

            result.add(gameGroupInfo);
        }

        return result;
    }

    public Collection<GameGroupInfo> getGameGroups(String type, List<String> params) {
        Collection<GameGroupInfo> result = new HashSet<>();

        for(GameGroupInfo gameGroupInfo : gameGroupInfoMap.values()) {
            if(type != null && !type.isEmpty()) {
                if (!gameGroupInfo.getType().equals(type)) continue;
                if(!params.isEmpty() && !params.equals(gameGroupInfo.getParams())) continue;
            }

            result.add(gameGroupInfo);
        }

        return result;
    }
}
