package com.ithinkrok.minigames.util.metadata;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;

import java.util.HashMap;
import java.util.Map;

public class Museum extends Metadata {

    private final Map<String, MuseumLocation> locations = new HashMap<>();

    public Museum(GameGroup gameGroup) {
        Config config = gameGroup.getSharedObjectOrEmpty("museum");

        Config locationsConfig = config.getConfigOrEmpty("locations");
        for(String key : locationsConfig.getKeys(false)) {
            Config locConfig = locationsConfig.getConfigOrEmpty(key);

            locations.put(key, new MuseumLocation(key, locConfig));
        }
    }

    public MuseumLocation getLocation(String name) {
        return locations.get(name);
    }

    public MuseumLocation getLocation(MapPoint point) {
        MuseumLocation closest = null;
        double closestDistSq = Double.POSITIVE_INFINITY;

        //TODO nether handling

        for (MuseumLocation loc : locations.values()) {
            double distSq = point.distanceSquared(loc.pos);

            if(distSq < closestDistSq && distSq < loc.size * loc.size) {
                closestDistSq = distSq;

                closest = loc;
            }
        }

        return closest;
    }

    public static Museum getOrCreate(GameGroup gameGroup) {
        Museum mus = gameGroup.getMetadata(Museum.class);

        if(mus == null) {
            mus = new Museum(gameGroup);
            gameGroup.setMetadata(mus);
        }

        return mus;
    }


    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }


    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return true;
    }

    public class MuseumLocation {
        private final String name;
        private final MapPoint pos;
        private final String past;
        private final String future;
        private final double size; //radius


        public MuseumLocation(String name, Config cfg) {
            this.name = name;
            pos = MinigamesConfigs.getMapPoint(cfg, "pos");
            past = cfg.getString("past");
            future = cfg.getString("future");
            size = 10000;
        }


        public String getName() {
            return name;
        }


        public MapPoint getPos() {
            return pos;
        }


        public MuseumLocation getPast() {
            return past != null ? Museum.this.getLocation(past) : null;
        }


        public MuseumLocation getFuture() {
            return future != null ? Museum.this.getLocation(future) : null;
        }
    }
}
