package com.ithinkrok.minigames.util.command;

import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.util.inventory.MinigamesShop;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

import java.util.ArrayList;
import java.util.List;

public class ShopCommand implements CustomListener {


    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        if(!event.getCommand().requireUser(event.getCommandSender())) return;

        MinigamesShop.showToUser(event.getCommand().getUser());
    }

}
