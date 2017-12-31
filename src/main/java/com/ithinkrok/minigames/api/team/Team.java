package com.ithinkrok.minigames.api.team;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.Nameable;
import com.ithinkrok.minigames.api.SharedObjectAccessor;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.metadata.MetadataHolder;
import com.ithinkrok.minigames.api.task.TaskScheduler;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.user.UserResolver;
import com.ithinkrok.msm.common.economy.Account;
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

    /**
     * @return A UUID to uniquely identify this team (e.g. for an economy account)
     */
    UUID getUuid();

    Account getEconomyAccount();

}
