package com.ithinkrok.minigames.event.user.world;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 01/01/16.
 */
public abstract class UserInteractEvent extends UserEvent implements Cancellable {

    private boolean cooldown = false;

    public UserInteractEvent(User user) {
        super(user);
    }

    public abstract InteractType getInteractType();

    public abstract BlockFace getBlockFace();

    public boolean hasBlock() {
        return getClickedBlock() != null;
    }

    public abstract Block getClickedBlock();

    public boolean hasEntity() {
        return getClickedEntity() != null;
    }

    public abstract Entity getClickedEntity();

    public boolean hasCustomItem() {
        return getCustomItem() != null;
    }

    public CustomItem getCustomItem() {
        if (!hasItem()) return null;

        int identifier = InventoryUtils.getIdentifier(getItem());
        if (identifier < 0) return null;
        return getUserGameGroup().getCustomItem(identifier);
    }

    public boolean hasItem() {
        return getItem() != null;
    }

    public abstract ItemStack getItem();

    public boolean getStartCooldownAfterAction() {
        return cooldown;
    }

    public void setStartCooldownAfterAction(boolean cooldown) {
        this.cooldown = cooldown;
    }

    public enum InteractType {

        /**
         * If the interaction was not done by the User, but by an Entity that is representing the User.
         */
        REPRESENTING,

        /**
         * If the interaction was via left click (e.g. block breaking or attacking)
         */
        LEFT_CLICK,

        /**
         * If the interaction was via right click
         */
        RIGHT_CLICK,

        /**
         * If the interaction was physical (e.g. standing on a pressure plate)
         */
        PHYSICAL
    }
}
