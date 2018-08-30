package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.MapWorldInfo;
import com.ithinkrok.minigames.base.generation.VoidGenerator;
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
    public World loadWorld(GameGroup gameGroup, GameMap map, MapWorldInfo info) {
        World world = Bukkit.getServer().getWorld(info.getWorldFolder());

        if(world == null) {
            wasLoaded = false;

            WorldCreator creator = new WorldCreator(info.getWorldFolder());

            creator.environment(info.getEnvironment());

            if(info.getGenerator() != null) {
                switch(info.getGenerator()) {
                    case "void":
                        creator.generator(new VoidGenerator());
                        break;
                    default:
                        creator.generator(info.getGenerator());
                }
            }

            world = creator.createWorld();
        }

        world.setAutoSave(true);

        return world;
    }

    @Override
    public void unloadWorld(World world) {
        if(wasLoaded) return;

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
