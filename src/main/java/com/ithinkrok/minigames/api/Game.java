package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.api.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesProtocol;
import com.ithinkrok.minigames.api.task.TaskScheduler;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.user.UserResolver;
import com.ithinkrok.minigames.api.util.disguise.Disguise;
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
public interface Game extends TaskScheduler, UserResolver, DatabaseTaskRunner, Nameable {
    ClientMinigamesProtocol getProtocol();

    Collection<String> getAvailableGameGroupTypes();

    Collection<? extends GameGroup> getGameGroups();

    Path getAssetDirectory();

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