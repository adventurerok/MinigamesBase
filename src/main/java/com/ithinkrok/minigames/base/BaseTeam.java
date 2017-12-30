package com.ithinkrok.minigames.base;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.task.GameRunnable;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskList;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
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
public class BaseTeam implements Listener, Team {

    private final UUID uuid = UUID.randomUUID();
    private final TeamIdentifier teamIdentifier;
    private final ConcurrentMap<UUID, BaseUser> usersInTeam = new ConcurrentHashMap<>();
    private final BaseGameGroup gameGroup;
    private final ClassToInstanceMap<Metadata> metadataMap = MutableClassToInstanceMap.create();
    private final TaskList teamTaskList = new TaskList();
    private final Collection<CustomListener> listeners = new ArrayList<>();

    public BaseTeam(TeamIdentifier teamIdentifier, BaseGameGroup gameGroup) {
        this.teamIdentifier = teamIdentifier;
        this.gameGroup = gameGroup;

        listeners.add(new TeamListener());
    }

    @Override
    public void makeEntityRepresentTeam(Entity entity) {
        gameGroup.getGame().makeEntityRepresentTeam(this, entity);
    }

    @Override
    public Collection<CustomListener> getListeners() {
        return listeners;
    }

    @Override
    public GameGroup getGameGroup() {
        return gameGroup;
    }

    @Override
    public int getUserCount() {
        return getUsers().size();
    }

    @Override
    public Collection<BaseUser> getUsers() {
        return usersInTeam.values();
    }

    @Override
    public TeamIdentifier getTeamIdentifier() {
        return teamIdentifier;
    }

    @Override
    public ChatColor getChatColor() {
        return teamIdentifier.getChatColor();
    }

    @Override
    public Color getArmorColor() {
        return teamIdentifier.getArmorColor();
    }

    @Override
    public DyeColor getDyeColor() {
        return teamIdentifier.getDyeColor();
    }

    @Override
    public void addUser(User user) {
        if (!(user instanceof BaseUser)) {
            throw new UnsupportedOperationException("Only supports BaseUser");
        }

        usersInTeam.put(user.getUuid(), (BaseUser) user);
    }

    @Override
    public void removeUser(User user) {
        usersInTeam.remove(user.getUuid());
    }

    @Override
    public boolean hasPlayerOfKit(String kitName) {
        for (User user : getUsers()) {
            if (kitName.equals(user.getKitName())) return true;
        }

        return false;
    }

    @Override
    public void updateUserScoreboards() {
        getUsers().forEach(User::updateScoreboard);
    }

    @Override
    public void removeFromGameGroup() {
        for (Metadata metadata : metadataMap.values()) {
            metadata.removed();
        }

        metadataMap.clear();
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getName() {
        return teamIdentifier.getName();
    }

    @Override
    public String getFormattedName() {
        return teamIdentifier.getFormattedName();
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

    @SuppressWarnings("unchecked")
    @Override
    public <B extends Metadata> B removeMetadata(Class<? extends B> clazz) {
        B metadata = (B) metadataMap.remove(clazz);

        if(metadata != null) metadata.removed();

        return metadata;
    }

    @Override
    public boolean hasSharedObject(String name) {
        return gameGroup.hasSharedObject(name);
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
        sendMessageNoPrefix(gameGroup.getMessagePrefix() + message);
    }


    @Override
    public void sendMessageNoPrefix(String message) {
        for (User user : getUsers()) {
            user.sendMessageNoPrefix(message);
        }
    }

    @Override
    public void sendMessageNoPrefix(Config message) {
        for (User user : getUsers()) {
            user.sendMessageNoPrefix(message);
        }

    }

    @Override
    public String getMessagePrefix() {
        return "";
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
