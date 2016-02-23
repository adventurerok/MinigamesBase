package com.ithinkrok.minigames.hub.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 23/02/16.
 */
public class GameChooseMenu implements CustomListener {

    private final Map<String, ItemStack> gameGroups = new HashMap<>();

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, CustomItem> event) {
        Config config = event.getConfigOrEmpty();

        Config gameGroupsConfig = config.getConfigOrEmpty("gamegroups");

        for(String gameGroupType : gameGroupsConfig.getKeys(false)) {
            ItemStack item = MinigamesConfigs.getItemStack(gameGroupsConfig, gameGroupType);

            gameGroups.put(gameGroupType, item);
        }
    }

    @CustomEventHandler
    public void onRightClick(UserInteractEvent event) {
        ClickableInventory inventory = new ClickableInventory("Game Types");

        for(Map.Entry<String, ItemStack> entry : gameGroups.entrySet()) {

            ClickableItem item = new ClickableItem(entry.getValue(), -1) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    event.getUser().sendMessage("Sending you to game type: " + entry.getKey());

                    ClientMinigamesRequestProtocol requestProtocol = event.getUserGameGroup().getRequestProtocol();
                    requestProtocol.sendJoinGameGroupPacket(event.getUser().getUuid(), entry.getKey(), null);

                    event.getUser().closeInventory();
                }
            };

            inventory.addItem(item);
        }

        //TODO toggle sending the user to the lobby (if possible) or sending the user to a hub area

        event.getUser().showInventory(inventory, null);
    }
}
