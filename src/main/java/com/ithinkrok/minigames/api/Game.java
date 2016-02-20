package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.base.Nameable;
import com.ithinkrok.minigames.base.database.DatabaseTask;
import com.ithinkrok.minigames.base.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.base.protocol.ClientMinigamesProtocol;
import com.ithinkrok.minigames.base.task.GameRunnable;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.task.TaskScheduler;
import com.ithinkrok.minigames.base.user.UserResolver;
import com.ithinkrok.minigames.base.util.JSONBook;
import com.ithinkrok.minigames.base.util.disguise.Disguise;
import com.ithinkrok.minigames.base.util.io.FileLoader;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LangFile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by paul on 20/02/16.
 */
public interface Game extends TaskScheduler, UserResolver, FileLoader, DatabaseTaskRunner, Nameable {
    ClientMinigamesProtocol getProtocol();

    Collection<String> getAvailableGameGroupTypes();

    Collection<? extends GameGroup> getGameGroups();

    Path getRamdiskDirectory();

    Path getConfigDirectory();

    Path getMapDirectory();

    void registerListeners();

    void sendPlayerToHub();

    void registerGameGroupConfig(String name, String configFile);

    void removeUser(User user);

    GameGroup getGameGroup(String ggName);

    void unload();

    void removeGameGroup(GameGroup gameGroup);

    void makeEntityRepresentUser(User user, Entity entity);

    void makeEntityActualUser(User user, Entity entity);

    void setGameGroupForMap(GameGroup gameGroup, String mapName);

    void makeEntityRepresentTeam(Team team, Entity entity);

    void disguiseUser(User user, EntityType type);

    void disguiseUser(User user, Disguise disguise);

    void unDisguiseUser(User user);

    void rejoinPlayer(Player player);

    boolean sendPlayerToHub(Player player);

    GameGroup createGameGroup(String type);

    GameGroup getSpawnGameGroup();

    Logger getLogger();

    void preJoinGameGroup(UUID playerUUID, String type, String name);
}
