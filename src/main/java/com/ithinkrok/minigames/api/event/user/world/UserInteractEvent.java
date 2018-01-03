package com.ithinkrok.minigames.api.event.user.world;

import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.event.Cancellable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 01/01/16.
 */
public abstract class UserInteractEvent extends BaseUserEvent implements Cancellable {

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

        String identifier = InventoryUtils.getIdentifier(getItem());
        if (identifier == null) return null;
        return getGameGroup().getCustomItem(identifier);
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
