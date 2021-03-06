package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

import java.util.ArrayList;
import java.util.List;

public class MinigamesShop implements CustomListener {

    public static void showToUser(User user) {
        Config shopShared = user.getGameGroup().getSharedObjectOrEmpty("shop");
        List<Config> minigameShop = shopShared.getConfigList("items");

        Config globalShopShared = user.getGameGroup().getSharedObjectOrEmpty("global_shop");
        List<Config> globalShop = globalShopShared.getConfigList("items");

        List<Config> combinedItems = new ArrayList<>(globalShop);
        combinedItems.addAll(minigameShop);

        if(combinedItems.isEmpty()) {
            user.sendLocale("command.shop.none");
            return;
        }

        user.sendLocale("command.shop.open");

        ClickableInventory inventory = new ClickableInventory("Shop");
        inventory.loadFromConfig(combinedItems);

        user.showInventory(inventory, null);
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        event.setCancelled(true);

        showToUser(event.getUser());
    }

}
