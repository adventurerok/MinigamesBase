package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.api.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.api.event.game.GameEvent;
import com.ithinkrok.minigames.api.event.team.TeamEvent;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.GameMapInfo;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.metadata.MetadataHolder;
import com.ithinkrok.minigames.api.schematic.SchematicResolver;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskScheduler;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.team.TeamUserResolver;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.CountdownConfig;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.lang.Messagable;

import java.util.Collection;
import java.util.Map;

/**
 * Created by paul on 20/02/16.
 */
public interface GameGroup
        extends LanguageLookup, Messagable, TaskScheduler, SharedObjectAccessor, MetadataHolder<Metadata>,
        SchematicResolver, TeamUserResolver, DatabaseTaskRunner {
    int getMaxPlayers();

    String getMotd();

    void setMotd(String motd);

    String getType();

    boolean isAcceptingPlayers();

    void setAcceptingPlayers(boolean acceptingPlayers);

    void changeGameState(String gameStateName);

    void changeMap(String mapName);

    GameMapInfo getMap(String mapName);

    @SuppressWarnings("unchecked")
    void changeGameState(GameState gameState);

    void changeMap(GameMapInfo mapInfo);

    void stopCountdown();

    Collection<? extends User> getUsers();

    Config toConfig();

    String getName();

    void prepareStart();

    GameMap getCurrentMap();

    Countdown getCountdown();


    void userEvent(UserEvent event);

    void teamEvent(TeamEvent event);

    GameState getCurrentGameState();

    void startCountdown(CountdownConfig countdownConfig);

    void startCountdown(String name, String localeStub, int seconds);

    CustomItem getCustomItem(String name);

    CustomItem getCustomItem(int identifier);

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

    String getChatPrefix();


}
