package com.ithinkrok.minigames.base.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.MapWorldInfo;
import com.ithinkrok.minigames.base.generation.VoidGenerator;
import com.ithinkrok.minigames.base.util.io.DirectoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by paul on 20/02/16.
 */
public class InstanceWorldHandler implements WorldHandler {

    private Path ramdiskPath;

    @Override
    public World loadWorld(GameGroup gameGroup, GameMap map, MapWorldInfo info) {
        String worldName = map.getInfo().getName() + "_" + info.getName();

        String randomWorldName = getRandomWorldName(worldName);
        Path copyFrom = gameGroup.getGame().getMapDirectory().resolve(info.getWorldFolder());

        boolean ramdisk = gameGroup.getGame().getRamdiskDirectory() != null;
        Path copyTo;

        if (ramdisk) {
            ramdiskPath = gameGroup.getGame().getRamdiskDirectory().resolve(randomWorldName);
            copyTo = ramdiskPath;
        } else {
            copyTo = Paths.get("./" + randomWorldName + "/");
        }

        try {
            DirectoryUtils.copy(copyFrom, copyTo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ramdisk) {
            try {
                Path linkLocation = Paths.get("./" + randomWorldName + "/");
                Files.deleteIfExists(linkLocation);

                Files.createSymbolicLink(linkLocation, copyTo);
            } catch (IOException e) {
                System.out.println("Failed to create symbolic link for map: " + randomWorldName);
                e.printStackTrace();
            }
        }

        try {
            Files.deleteIfExists(copyTo.resolve("uid.dat"));
        } catch (IOException e) {
            System.out.println("Could not delete uid.dat for world. This could cause errors");
            e.printStackTrace();
        }

        WorldCreator creator = new WorldCreator(randomWorldName);

        creator.environment(info.getEnvironment());
        creator.generator(new VoidGenerator());

        World world = creator.createWorld();
        world.setAutoSave(false);

        return world;
    }

    private String getRandomWorldName(String mapName) {
        int count = 0;
        String randomWorldName;
        do {
            randomWorldName = mapName + "-" + String.format("%04X", count++);
        } while (Bukkit.getWorld(randomWorldName) != null);

        return randomWorldName;
    }

    @Override
    public void unloadWorld(World world) {
        if (!world.getPlayers().isEmpty()) System.out.println("There are still players in an unloading map!");

        for (Player player : world.getPlayers()) {
            player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
        }

        boolean success = Bukkit.unloadWorld(world, false);

        if (ramdiskPath != null) {
            try {
                DirectoryUtils.delete(ramdiskPath);
            } catch (IOException e) {
                System.out.println("Failed to delete map on ramdisk.");
                e.printStackTrace();
            }
        }

        try {
            DirectoryUtils.delete(world.getWorldFolder().toPath());
        } catch (IOException e) {
            System.out.println("Failed to unload map. When bukkit tried to unload, it returned " + success);
            System.out.println("Please make sure there are no players in the map before deleting?");
            e.printStackTrace();
        }
    }
}
