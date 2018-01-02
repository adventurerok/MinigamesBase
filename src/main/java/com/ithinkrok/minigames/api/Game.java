package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.api.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesProtocol;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskScheduler;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.disguise.Disguise;
import com.ithinkrok.minigames.base.BaseUser;
import com.ithinkrok.msm.common.economy.Economy;
import com.ithinkrok.msm.common.economy.EconomyContext;
import com.ithinkrok.msm.common.economy.provider.EconomyProvider;
import com.ithinkrok.util.math.Variables;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by paul on 20/02/16.
 */
public interface Game extends TaskScheduler, DatabaseTaskRunner, Nameable {
    ClientMinigamesProtocol getProtocol();

    Collection<String> getAvailableGameGroupTypes();

    Collection<? extends GameGroup> getGameGroups();

    Path getAssetDirectory();

    Path getRamdiskDirectory();

    Path getConfigDirectory();

    Path getMapDirectory();

    Path getResourceDirectory();

    void registerListeners();

    void registerGameGroupConfig(String name, String configFile);

    GameGroup getGameGroup(String ggName);

    void unload();

    void removeGameGroup(GameGroup gameGroup);

    void makeEntityRepresentUser(User user, Entity entity);

    void makeEntityActualUser(User user, Entity entity);

    void setGameGroupForWorlds(GameGroup gameGroup, Collection<World> worlds);

    GameGroup getGameGroupFromWorldName(String worldName);

    void makeEntityRepresentTeam(Team team, Entity entity);

    void disguiseUser(User user, Disguise disguise);

    void unDisguiseUser(User user);

    void rejoinPlayer(Player player);

    boolean sendPlayerToHub(Player player);

    GameGroup createGameGroup(String type, List<String> params);

    GameGroup getSpawnGameGroup();

    Logger getLogger();

    void preJoinGameGroup(UUID playerUUID, String type, String name, List<String> params);

    BaseUser getUser(Entity entity);

    /**
     * Check if the server should restart due to low resources
     */
    void checkResourcesRestart();

    void setupCustomEntity(Entity entity, String name, Variables variables);

    /**
     * @return An Economy with server and global level access
     */
    Economy getEconomy();
}
