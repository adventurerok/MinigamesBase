package com.ithinkrok.minigames.api.event.user.world;

import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by paul on 17/01/16.
 */
public class UserChatEvent extends UserEvent implements Cancellable {

    private final AsyncPlayerChatEvent event;

    public UserChatEvent(User user, AsyncPlayerChatEvent event) {
        super(user);
        this.event = event;
    }

    public Set<User> getRecipients() {
        Set<User> result = new HashSet<>();

        for(Player player : event.getRecipients()) {
            result.add(getUserGameGroup().getUser(player.getUniqueId()));
        }

        return result;
    }

    public boolean addRecipient(User recipient) {
        if(!recipient.isPlayer()) return false;
        return event.getRecipients().add(recipient.getPlayer());
    }

    public boolean removeRecipient(User recipient) {
        if(!recipient.isPlayer()) return false;
        return event.getRecipients().remove(recipient.getPlayer());
    }

    public String getMessage() {
        return event.getMessage();
    }

    public String getFormat() {
        return event.getFormat();
    }

    public void setMessage(String message) {
        event.setMessage(message);
    }

    public void setFormat(String format) {
        event.setFormat(format);
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

}
