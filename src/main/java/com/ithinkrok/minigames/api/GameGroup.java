package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.base.*;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.base.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.base.event.game.GameEvent;
import com.ithinkrok.minigames.base.event.team.TeamEvent;
import com.ithinkrok.minigames.base.event.user.UserEvent;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.minigames.base.map.GameMap;
import com.ithinkrok.minigames.base.map.GameMapInfo;
import com.ithinkrok.minigames.base.metadata.Metadata;
import com.ithinkrok.minigames.base.metadata.MetadataHolder;
import com.ithinkrok.minigames.base.schematic.SchematicResolver;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.task.TaskScheduler;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.minigames.base.team.TeamUserResolver;
import com.ithinkrok.minigames.base.util.CountdownConfig;
import com.ithinkrok.minigames.base.util.JSONBook;
import com.ithinkrok.minigames.base.util.io.ConfigHolder;
import com.ithinkrok.minigames.base.util.io.FileLoader;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.lang.Messagable;

import java.util.Collection;
import java.util.Map;

/**
 * Created by paul on 20/02/16.
 */
public interface GameGroup
        extends LanguageLookup, Messagable, TaskScheduler, FileLoader, SharedObjectAccessor, MetadataHolder<Metadata>,SchematicResolver,
        TeamUserResolver, DatabaseTaskRunner, ConfigHolder {
    int getMaxPlayers();

    String getMotd();

    void setMotd(String motd);

    String getType();

    void setAcceptingPlayers(boolean acceptingPlayers);

    boolean isAcceptingPlayers();

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
