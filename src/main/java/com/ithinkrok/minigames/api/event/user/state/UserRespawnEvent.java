package com.ithinkrok.minigames.api.event.user.state;

import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.user.User;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Created by paul on 21/12/16.
 */
public class UserRespawnEvent extends UserEvent {


    private final PlayerRespawnEvent event;

    public UserRespawnEvent(User user, PlayerRespawnEvent event) {
        super(user);
        this.event = event;
    }

    public Location getRespawnLocation() {
        return event.getRespawnLocation();
    }

    public void setRespawnLocation(Location loc) {
        event.setRespawnLocation(loc);
    }

    public boolean isBedSpawn() {
        return event.isBedSpawn();
    }
}
