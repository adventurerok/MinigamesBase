package com.ithinkrok.minigames.util.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.entity.CustomEntity;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.math.MapVariables;

import java.util.List;

public class CustomEntitySpawner implements CustomListener {


    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameMap> event) {
        Config config = event.getConfigOrEmpty();
        GameGroup gameGroup = event.getCreator();

        List<Config> spawnConfigs = config.getConfigList("spawns");
        for (Config spawn : spawnConfigs) {
            MapPoint pos = MinigamesConfigs.getMapPoint(spawn, "pos");
            String type = spawn.getString("type");
            MapVariables variables = new MapVariables(spawn.getConfigOrEmpty("variables"));

            gameGroup.doInFuture(task -> {
                CustomEntity ent = gameGroup.getCustomEntity(type);

                ent.spawnCustomEntity(gameGroup, gameGroup.getCurrentMap().getLocation(pos), variables);

            });
        }

    }

}
