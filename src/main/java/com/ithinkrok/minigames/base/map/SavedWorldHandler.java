package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.api.GameGroup;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

/**
 * Created by paul on 20/02/16.
 */
public class SavedWorldHandler implements WorldHandler {

    boolean wasLoaded = true;

    @Override
    public World loadWorld(GameGroup gameGroup, BaseMap map) {
        World world = Bukkit.getServer().getWorld(map.getInfo().getMapFolder());

        if(world == null) {
            wasLoaded = false;

            WorldCreator creator = new WorldCreator(map.getInfo().getMapFolder());

            creator.environment(map.getInfo().getEnvironment());

            world = creator.createWorld();
        }

        world.setAutoSave(true);

        return world;
    }

    @Override
    public void unloadWorld(BaseMap map) {
        if(wasLoaded) return;

        World world = map.getWorld();

        if (!world.getPlayers().isEmpty()) System.out.println("There are still players in an unloading map!");

        for (Player player : world.getPlayers()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        boolean success = Bukkit.unloadWorld(world, true);

        if(!success) {
            System.out.println("Failed to unload SAVED map");
        }
    }
}
