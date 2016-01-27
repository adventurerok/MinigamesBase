package com.ithinkrok.minigames.base.event.user.state;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.user.UserEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * Created by paul on 02/01/16.
 */
public class UserFoodLevelChangeEvent extends UserEvent implements Cancellable {

    private final FoodLevelChangeEvent event;

    public UserFoodLevelChangeEvent(User user, FoodLevelChangeEvent event) {
        super(user);
        this.event = event;
    }

    public int getFoodLevel() {
        return event.getFoodLevel();
    }

    public void setFoodLevel(int level) {
        event.setFoodLevel(level);
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
