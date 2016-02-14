package com.ithinkrok.minigames.base.team;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.Nameable;
import com.ithinkrok.minigames.base.SharedObjectAccessor;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.game.MapChangedEvent;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.minigames.base.lang.Messagable;
import com.ithinkrok.minigames.base.metadata.Metadata;
import com.ithinkrok.minigames.base.metadata.MetadataHolder;
import com.ithinkrok.minigames.base.task.GameRunnable;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.task.TaskList;
import com.ithinkrok.minigames.base.task.TaskScheduler;
import com.ithinkrok.minigames.base.user.UserResolver;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by paul on 31/12/15.
 */
public class Team implements Listener, Messagable, LanguageLookup, SharedObjectAccessor, TaskScheduler, UserResolver,
        MetadataHolder<Metadata>, Nameable {

    private final TeamIdentifier teamIdentifier;
    private final ConcurrentMap<UUID, User> usersInTeam = new ConcurrentHashMap<>();
    private final GameGroup gameGroup;

    private final ClassToInstanceMap<Metadata> metadataMap = MutableClassToInstanceMap.create();

    private final TaskList teamTaskList = new TaskList();

    private final Collection<CustomListener> listeners = new ArrayList<>();

    public Team(TeamIdentifier teamIdentifier, GameGroup gameGroup) {
        this.teamIdentifier = teamIdentifier;
        this.gameGroup = gameGroup;

        listeners.add(new TeamListener());
    }

    public void makeEntityRepresentTeam(Entity entity) {
        gameGroup.getGame().makeEntityRepresentTeam(this, entity);
    }

    public Collection<CustomListener> getListeners() {
        return listeners;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }

    public int getUserCount() {
        return getUsers().size();
    }

    public Collection<User> getUsers() {
        return usersInTeam.values();
    }

    public TeamIdentifier getTeamIdentifier() {
        return teamIdentifier;
    }

    @Override
    public String getName() {
        return teamIdentifier.getName();
    }

    @Override
    public String getFormattedName() {
        return teamIdentifier.getFormattedName();
    }

    public ChatColor getChatColor() {
        return teamIdentifier.getChatColor();
    }

    public Color getArmorColor() {
        return teamIdentifier.getArmorColor();
    }

    public DyeColor getDyeColor() {
        return teamIdentifier.getDyeColor();
    }

    @Override
    public String getLocale(String name) {
        return gameGroup.getLocale(name);
    }

    @Override
    public String getLocale(String name, Object... args) {
        return gameGroup.getLocale(name, args);
    }

    @Override
    public boolean hasLocale(String name) {
        return gameGroup.hasLocale(name);
    }

    public void addUser(User user) {
        usersInTeam.put(user.getUuid(), user);
    }

    public void removeUser(User user) {
        usersInTeam.remove(user.getUuid());
    }

    @Override
    public <B extends Metadata> B getMetadata(Class<? extends B> clazz) {
        return metadataMap.getInstance(clazz);
    }

    @Override
    public <B extends Metadata> void setMetadata(B metadata) {
        Metadata oldMetadata = metadataMap.put(metadata.getMetadataClass(), metadata);

        if (oldMetadata != null && oldMetadata != metadata) {
            oldMetadata.cancelAllTasks();
            oldMetadata.removed();
        }
    }

    @Override
    public boolean hasMetadata(Class<? extends Metadata> clazz) {
        return metadataMap.containsKey(clazz);
    }

    @Override
    public Config getSharedObject(String name) {
        return gameGroup.getSharedObject(name);
    }

    @Override
    public Config getSharedObjectOrEmpty(String name) {
        return gameGroup.getSharedObjectOrEmpty(name);
    }

    @Override
    public boolean hasSharedObject(String name) {
        return gameGroup.hasSharedObject(name);
    }

    @Override
    public GameTask doInFuture(GameRunnable task) {
        GameTask gameTask = gameGroup.doInFuture(task);

        teamTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask doInFuture(GameRunnable task, int delay) {
        GameTask gameTask = gameGroup.doInFuture(task, delay);

        teamTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask repeatInFuture(GameRunnable task, int delay, int period) {
        GameTask gameTask = gameGroup.repeatInFuture(task, delay, period);

        teamTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public void cancelAllTasks() {
        teamTaskList.cancelAllTasks();
    }

    @Override
    public User getUser(UUID uuid) {
        if (uuid == null) return null;
        return usersInTeam.get(uuid);
    }

    public void updateUserScoreboards() {
        getUsers().forEach(User::updateScoreboard);
    }

    public void removeFromGameGroup() {
        for(Metadata metadata : metadataMap.values()) {
            metadata.removed();
        }

        metadataMap.clear();
    }

    private class TeamListener implements CustomListener {


        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventGameStateChange(GameStateChangedEvent event) {
            Iterator<Metadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                Metadata metadata = iterator.next();

                if (metadata.removeOnGameStateChange(event)) {
                    metadata.cancelAllTasks();
                    metadata.removed();
                    iterator.remove();
                }
            }
        }

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventMapChange(MapChangedEvent event) {
            Iterator<Metadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                Metadata metadata = iterator.next();

                if (metadata.removeOnMapChange(event)) {
                    metadata.cancelAllTasks();
                    metadata.removed();
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void sendLocale(String locale, Object... args) {
        sendMessage(getLocale(locale, args));
    }


    @Override
    public void sendMessage(String message) {
        sendMessageNoPrefix(gameGroup.getChatPrefix() + message);
    }


    @Override
    public void sendMessageNoPrefix(String message) {
        for (User user : getUsers()) {
            user.sendMessageNoPrefix(message);
        }
    }


    @Override
    public void sendLocaleNoPrefix(String locale, Object... args) {
        sendMessageNoPrefix(getLocale(locale, args));
    }


    @Override
    public LanguageLookup getLanguageLookup() {
        return gameGroup.getLanguageLookup();
    }


}
