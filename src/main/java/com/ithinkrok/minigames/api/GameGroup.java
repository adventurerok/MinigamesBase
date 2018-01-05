package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.api.database.Database;
import com.ithinkrok.minigames.api.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.api.entity.CustomEntity;
import com.ithinkrok.minigames.api.event.game.GameEvent;
import com.ithinkrok.minigames.api.event.team.TeamEvent;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.GameMapInfo;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.metadata.MetadataHolder;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.api.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.api.schematic.SchematicResolver;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskScheduler;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.team.TeamUserResolver;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.CountdownConfig;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.msm.common.economy.Economy;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.ListenerHolder;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.lang.Messagable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 20/02/16.
 * <p>
 * A group of users playing an instance of a particular minigame.
 */
public interface GameGroup
        extends LanguageLookup, Messagable, TaskScheduler, SharedObjectAccessor, MetadataHolder<Metadata>,
        SchematicResolver, TeamUserResolver, DatabaseTaskRunner, ListenerHolder {

    List<String> getParameters();

    /**
     * Gets the maximum number of users allowed in this gamegroup
     * <p>
     * Note: Despite being called "getMaxPlayers", this returns the maximum number of users allowed in the gamegroup,
     * which includes non-player users.
     *
     * @return The maximum number of users allowed in this gamegroup
     */
    int getMaxPlayers();

    /**
     * Gets the motd set by {@link #setMotd(String)}, or by the gamegroup config if {@link #setMotd(String)} has not been called.
     *
     * @return The motd set by {@link #setMotd(String)}, or by the gamegroup config if {@link #setMotd(String)} has
     * not been called
     */
    String getMotd();

    /**
     * Sets the motd of the gamegroup.
     * <p>
     * The motd can be displayed by signs on hub servers, or in the minecraft server list.
     *
     * @param motd The motd to set
     */
    void setMotd(String motd);

    Database getDatabase();

    /**
     * Gets the type of this gamegroup
     * <p>
     * The type is the name of the gamegroup config, NOT the path of the gamegroup config in the config directory.
     *
     * @return The type of this gamegroup
     */
    String getType();

    /**
     * Gets the acceptingPlayers flag of this gamegroup
     * <p>
     * The acceptingPlayers flag is true if players looking for a game should be sent to this gamegroup. If it is
     * false, spectators can still join the gamegroup. It defaults to {@code true}.
     *
     * @return If new players looking for a game should be allowed into this gamegroup, {@code true} by default
     */
    boolean isAcceptingPlayers();

    /**
     * Sets the acceptingPlayers flag of this gamegroup
     * <p>
     * The acceptingPlayers flag is true if players looking for a game should be sent to this gamegroup. If it is
     * false, spectators can still join the gamegroup. It defaults to {@code true}.
     * <p>
     * Only on rare occasions should acceptingPlayers be set to {@code true} when it was previously {@code false}, as
     * this could cause new players looking for a game to join this gamegroup. GameGroups should be killed when the
     * game ends, instead of doing this. See {@link #kill()}.
     *
     * @param acceptingPlayers The new acceptingPlayers flag for this gamegroup
     */
    void setAcceptingPlayers(boolean acceptingPlayers);

    /**
     * Changes the gamestate of this gamegroup to the gamestate named by the {@code gameStateName} parameter
     *
     * @param gameStateName The name of the new game state to change to
     * @throws IllegalArgumentException If there is no gamestate with the given name, or if {@code gameStateName} is
     *                                  null
     */
    void changeGameState(String gameStateName);

    /**
     * Changes the map of this gamegroup to the map named by the {@code mapName} parameter
     * <p>
     * This will teleport all players in the gamegroup to the spawn of the new map, and unload the old map.
     *
     * @param mapName The name of the new map to change to
     * @throws IllegalArgumentException If there is no map with the given name, or if {@code mapName} is null
     */
    void changeMap(String mapName);

    ClientMinigamesRequestProtocol getRequestProtocol();

    ControllerInfo getControllerInfo();

    void requestControllerInfo();

    GameMapInfo getMap(String mapName);

    @SuppressWarnings("unchecked")
    void changeGameState(GameState gameState);

    void changeMap(GameMapInfo mapInfo);

    void stopCountdown();

    Collection<? extends User> getUsers();

    /**
     * Creates a config that holds some details about this config, which can be sent to the controller via a
     * GameGroupUpdate payload.
     *
     * @return A config describing details about this gamegroup to be sent to the controller
     */
    Config toConfig();

    /**
     * Gets the name of this gamegroup.
     * <p>
     * The default naming scheme for gamegroups is SERVER_TYPE_INDEX, where SERVER refers to the name of the server
     * the gamegroup is on, TYPE refers to the type of the gamegroup (see {@link #getType()}), and INDEX refers to
     * the index of this type of gamegroup on the server (0 for the first gamegroup, 1 for the second, etc...)
     *
     * @return The name of this gamegroup, usually in the form SERVER_TYPE_INDEX
     */
    String getName();

    GameMap getCurrentMap();

    Countdown getCountdown();

    void userEvent(UserEvent event);

    void teamEvent(TeamEvent event);

    GameState getCurrentGameState();

    void startCountdown(CountdownConfig countdownConfig);

    CustomItem getCustomItem(String name);

    Collection<CustomItem> getAllCustomItems();

    CustomEntity getCustomEntity(String name);

    void gameEvent(GameEvent event);

    void unload();

    void bindTaskToCurrentGameState(GameTask task);

    void bindTaskToCurrentMap(GameTask task);

    Game getGame();

    boolean hasActiveCountdown();

    boolean hasActiveCountdown(String name);

    int getUserCount();

    Kit getKit(String name);

    GameState getGameState(String gameStateName);

    Collection<TeamIdentifier> getTeamIdentifiers();

    JSONBook getBook(String name);

    void kill();

    CommandConfig getCommand(String name);

    Map<String, CommandConfig> getCommands();


    /**
     * The Economy for this gamegroup will provide currencies specific to this GameGroup,
     * as well access to server and global level economy.
     *
     * @return The economy for this GameGroup
     */
    Economy getEconomy();
}
