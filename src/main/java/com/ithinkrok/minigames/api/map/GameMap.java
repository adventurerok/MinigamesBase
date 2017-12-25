package com.ithinkrok.minigames.api.map;

import com.ithinkrok.minigames.api.SharedObjectAccessor;
import com.ithinkrok.minigames.api.entity.CustomEntity;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.schematic.PastedSchematic;
import com.ithinkrok.minigames.api.schematic.Schematic;
import com.ithinkrok.minigames.api.schematic.SchematicPaster;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 20/02/16.
 */
public interface GameMap extends LanguageLookup, SchematicPaster.BoundsChecker, SharedObjectAccessor {
    World getWorld();

    void addPastedSchematic(PastedSchematic schematic);

    void removePastedSchematic(PastedSchematic schematic);

    GameMapInfo getInfo();

    CustomItem getCustomItem(String name);

    CustomItem getCustomItem(int identifier);

    Collection<CustomItem> getAllCustomItems();

    CustomEntity getCustomEntity(String name);

    void bindTaskToMap(GameTask task);

    void unloadMap();

    boolean teleportUser(User user);

    List<CustomListener> getListeners();

    Map<String, CustomListener> getListenerMap();

    Schematic getSchematic(String name);

    JSONBook getBook(String name);

    Entity spawnEntity(Vector location, EntityType type);

    Entity spawnEntity(Location location, EntityType type);

    Location getLocation(Vector location);

    Location getLocation(double x, double y, double z);

    Block getBlock(Vector location);

    Block getBlock(int x, int y, int z);

    Location getSpawn();

    MapType getMapType();
}
