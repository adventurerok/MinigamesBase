package com.ithinkrok.minigames.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by paul on 05/01/16.
 */
public class TreeFeller {

    public static int fellTree(Location location, LocationChecker checker){
        LinkedList<Location> locations = new LinkedList<>();

        addSurrounding(locations, location);

        ArrayList<Location> ingotsToSpawn = new ArrayList<>();

        while(locations.size() > 0){
            Location pos = locations.removeFirst();
            if(!checker.check(pos)) continue;

            Block block = pos.getBlock();

            if(block.getType() != Material.LOG && block.getType() != Material.LOG_2) continue;
            //TODO if(game.isInBuilding(pos)) continue;

            block.setType(Material.AIR);
            ingotsToSpawn.add(pos);

            addSurrounding(locations, pos);
        }

        return ingotsToSpawn.size();
    }

    private static void addSurrounding(List<Location> locations, Location l){
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ() - 1d));

        locations.add(new Location(l.getWorld(), l.getX(), l.getY() + 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() + 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() + 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() + 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() + 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() + 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() + 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() + 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() + 1d, l.getZ() - 1d));

        locations.add(new Location(l.getWorld(), l.getX(), l.getY() - 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() - 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() - 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() - 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() - 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() - 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() - 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() - 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() - 1d, l.getZ() - 1d));
    }
}
