package com.ithinkrok.minigames.hub.item;

import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 18/09/16.
 */
public class SuperPopper implements CustomListener {


    @CustomEventHandler
    public void onUserRightClick(UserInteractEvent event) {
        User user = event.getUser();

        if(!user.getInventory().contains(Material.ARROW)) {
            user.getInventory().addItem(new ItemStack(Material.ARROW));
        }
    }
}
