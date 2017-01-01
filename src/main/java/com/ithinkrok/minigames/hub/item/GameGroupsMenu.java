package com.ithinkrok.minigames.hub.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.hub.inventory.GameChooseInventory;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

import java.util.List;

/**
 * Created by paul on 08/12/16.
 */
public class GameGroupsMenu implements CustomListener {

    private String gameGroupType;
    private List<String> gameGroupParams;

    private String inventoryTitleLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, CustomItem> event) {
        Config config = event.getConfigOrEmpty();

        gameGroupType = config.getString("gamegroup_type", "");
        gameGroupParams = config.getStringList("gamegroup_params");

        inventoryTitleLocale = config.getString("inventory_title_locale", "gamegroups_menu.title");
    }

    @CustomEventHandler
    public void onRightClick(UserInteractEvent event) {
        String title = event.getGameGroup().getLocale(inventoryTitleLocale);

        ClickableInventory inventory = new GameChooseInventory(title, event.getUser(), gameGroupType, gameGroupParams);

        event.getUser().showInventory(inventory, null);
    }

}
