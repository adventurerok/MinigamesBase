package com.ithinkrok.minigames.base.listener;

import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.util.CustomItemGiver;
import com.ithinkrok.minigames.base.event.user.game.UserJoinEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

/**
 * Created by paul on 04/01/16.
 */
public class GiveCustomItemsOnJoin implements Listener {

    private CustomItemGiver customItemGiver;

    @MinigamesEventHandler
    public void onListenerEnabled(ListenerLoadedEvent event) {
        ConfigurationSection config = event.getConfig();

        customItemGiver = new CustomItemGiver(config);
    }

    @MinigamesEventHandler(priority = MinigamesEventHandler.LOW)
    public void onUserJoin(UserJoinEvent event) {
        customItemGiver.giveToUser(event.getUser());
    }


}
