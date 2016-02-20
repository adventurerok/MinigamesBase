package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.base.Nameable;
import com.ithinkrok.minigames.base.SharedObjectAccessor;
import com.ithinkrok.minigames.base.metadata.Metadata;
import com.ithinkrok.minigames.base.metadata.MetadataHolder;
import com.ithinkrok.minigames.base.task.GameRunnable;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.task.TaskScheduler;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.minigames.base.user.UserResolver;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.lang.Messagable;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by paul on 20/02/16.
 */
public interface Team
        extends Messagable, LanguageLookup, SharedObjectAccessor, TaskScheduler, UserResolver, MetadataHolder<Metadata>,Nameable {
    void makeEntityRepresentTeam(Entity entity);

    Collection<CustomListener> getListeners();

    GameGroup getGameGroup();

    int getUserCount();

    Collection<? extends User> getUsers();

    TeamIdentifier getTeamIdentifier();

    ChatColor getChatColor();

    Color getArmorColor();

    DyeColor getDyeColor();

    void addUser(User user);

    void removeUser(User user);

    boolean hasPlayerOfKit(String kitName);

    void updateUserScoreboards();

    void removeFromGameGroup();

}
