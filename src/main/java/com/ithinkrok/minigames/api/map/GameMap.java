package com.ithinkrok.minigames.api.map;

import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.SharedObjectAccessor;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.schematic.PastedSchematic;
import com.ithinkrok.minigames.api.schematic.Schematic;
import com.ithinkrok.minigames.api.schematic.SchematicPaster;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.util.BoundingBox;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.minigames.api.util.io.ConfigHolder;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

/**
 * Created by paul on 20/02/16.
 */
public interface GameMap extends LanguageLookup, ConfigHolder, SchematicPaster.BoundsChecker, SharedObjectAccessor {
    World getWorld();

    void addPastedSchematic(PastedSchematic schematic);

    void removePastedSchematic(PastedSchematic schematic);

    GameMapInfo getInfo();

    CustomItem getCustomItem(String name);

    CustomItem getCustomItem(int identifier);

    void bindTaskToMap(GameTask task);

    void unloadMap();

    @Override
    boolean hasSharedObject(String name);

    @Override
    Config getSharedObject(String name);

    @Override
    Config getSharedObjectOrEmpty(String name);

    void teleportUser(User user);

    @Override
    String getLocale(String name);

    @Override
    String getLocale(String name, Object... args);

    @Override
    boolean hasLocale(String name);

    List<CustomListener> getListeners();

    @Override
    void addListener(String name, CustomListener listener);

    @Override
    void addCustomItem(CustomItem item);

    @Override
    void addLanguageLookup(LanguageLookup languageLookup);

    @Override
    void addSharedObject(String name, Config config);

    @Override
    void addSchematic(Schematic schematic);

    @Override
    void addTeamIdentifier(TeamIdentifier teamIdentifier);

    @Override
    void addGameState(GameState gameState);

    @Override
    void addKit(Kit kit);

    @Override
    void addCommand(CommandConfig command);

    @Override
    void addMapInfo(GameMapInfo mapInfo);

    @Override
    void addBook(JSONBook book);

    Map<String, CustomListener> getListenerMap();

    Schematic getSchematic(String name);

    JSONBook getBook(String name);

    @Override
    boolean canPaste(BoundingBox bounds);

    Entity spawnEntity(Vector location, EntityType type);

    Entity spawnEntity(Location location, EntityType type);

    Location getLocation(Vector location);

    Location getLocation(double x, double y, double z);

    Block getBlock(Vector location);

    Block getBlock(int x, int y, int z);

    Location getSpawn();
}
