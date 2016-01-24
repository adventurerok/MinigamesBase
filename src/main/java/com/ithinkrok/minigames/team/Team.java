package com.ithinkrok.minigames.team;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.SharedObjectAccessor;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.metadata.Metadata;
import com.ithinkrok.minigames.metadata.MetadataHolder;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.task.TaskScheduler;
import com.ithinkrok.minigames.user.UserResolver;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
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
        MetadataHolder<Metadata> {

    private TeamIdentifier teamIdentifier;
    private ConcurrentMap<UUID, User> usersInTeam = new ConcurrentHashMap<>();
    private GameGroup gameGroup;

    private ClassToInstanceMap<Metadata> metadataMap = MutableClassToInstanceMap.create();

    private TaskList teamTaskList = new TaskList();

    private Collection<Listener> listeners = new ArrayList<>();

    public Team(TeamIdentifier teamIdentifier, GameGroup gameGroup) {
        this.teamIdentifier = teamIdentifier;
        this.gameGroup = gameGroup;

        listeners.add(new TeamListener());
    }

    public void makeEntityRepresentTeam(Entity entity) {
        gameGroup.getGame().makeEntityRepresentTeam(this, entity);
    }

    public Collection<Listener> getListeners() {
        return listeners;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }

    public int getUserCount() {
        return getUsers().size();
    }

    public TeamIdentifier getTeamIdentifier() {
        return teamIdentifier;
    }

    public String getName() {
        return teamIdentifier.getName();
    }

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

    public void addUser(User user) {
        usersInTeam.put(user.getUuid(), user);
    }

    public void removeUser(User user) {
        usersInTeam.remove(user.getUuid());
    }

    @Override
    public boolean hasLocale(String name) {
        return gameGroup.hasLocale(name);
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
    public String getLocale(String name, Object... args) {
        return gameGroup.getLocale(name, args);
    }

    @Override
    public void sendMessageNoPrefix(String message) {
        for (User user : getUsers()) {
            user.sendMessageNoPrefix(message);
        }
    }

    public Collection<User> getUsers() {
        return usersInTeam.values();
    }

    @Override
    public void sendLocaleNoPrefix(String locale, Object... args) {
        sendMessageNoPrefix(getLocale(locale, args));
    }

    @Override
    public LanguageLookup getLanguageLookup() {
        return gameGroup.getLanguageLookup();
    }

    @Override
    public <B extends Metadata> B getMetadata(Class<? extends B> clazz) {
        return metadataMap.getInstance(clazz);
    }

    @Override
    public <B extends Metadata> void setMetadata(B metadata) {
        Metadata oldMetadata = metadataMap.put(metadata.getMetadataClass(), metadata);

        if(oldMetadata != null && oldMetadata != metadata) {
            oldMetadata.cancelAllTasks();
            oldMetadata.removed();
        }
    }

    @Override
    public boolean hasMetadata(Class<? extends Metadata> clazz) {
        return metadataMap.containsKey(clazz);
    }

    @Override
    public ConfigurationSection getSharedObject(String name) {
        return gameGroup.getSharedObject(name);
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

    private class TeamListener implements Listener {


        @MinigamesEventHandler(priority = MinigamesEventHandler.INTERNAL_FIRST)
        public void eventGameStateChange(GameStateChangedEvent event) {
            Iterator<Metadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                Metadata metadata = iterator.next();

                if (metadata.removeOnGameStateChange(event)){
                    metadata.cancelAllTasks();
                    metadata.removed();
                    iterator.remove();
                }
            }
        }

        @MinigamesEventHandler(priority = MinigamesEventHandler.INTERNAL_FIRST)
        public void eventMapChange(MapChangedEvent event) {
            Iterator<Metadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                Metadata metadata = iterator.next();

                if (metadata.removeOnMapChange(event)){
                    metadata.cancelAllTasks();
                    metadata.removed();
                    iterator.remove();
                }
            }
        }
    }
}
