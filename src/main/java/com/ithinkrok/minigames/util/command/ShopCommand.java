package com.ithinkrok.minigames.util.command;

import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

import java.util.ArrayList;
import java.util.List;

public class ShopCommand implements CustomListener {


    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        Config shopShared = event.getCommand().getGameGroup().getSharedObjectOrEmpty("shop");
        List<Config> minigameShop = shopShared.getConfigList("items");

        Config globalShopShared = event.getCommand().getGameGroup().getSharedObjectOrEmpty("global_shop");
        List<Config> globalShop = globalShopShared.getConfigList("items");

        List<Config> combinedItems = new ArrayList<>(globalShop);
        combinedItems.addAll(minigameShop);

        if(combinedItems.isEmpty()) {
            event.getCommandSender().sendLocale("command.shop.none");
            return;
        }

        event.getCommandSender().sendLocale("command.shop.open");

        ClickableInventory inventory = new ClickableInventory("Shop");
        inventory.loadFromConfig(combinedItems);

        event.getCommand().getUser().showInventory(inventory, null);
    }

}
