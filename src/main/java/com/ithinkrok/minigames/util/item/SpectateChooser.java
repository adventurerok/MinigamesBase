package com.ithinkrok.minigames.util.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * Created by paul on 17/01/16.
 */
public class SpectateChooser implements CustomListener {

    private String titleLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfigOrEmpty();

        titleLocale = config.getString("title_locale", "spectate_chooser.title");
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {

        ClickableInventory inv = new ClickableInventory(event.getGameGroup().getLocale(titleLocale));

        for(User user : event.getGameGroup().getUsers()) {
            if(!user.isInGame()) continue;

            ItemStack item = InventoryUtils.createItemWithNameAndLore(Material.SKULL_ITEM, 1, 3, user
                    .getFormattedName());

            UUID userUUID = user.getUuid();
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(user.getName());
            item.setItemMeta(meta);

            inv.addItem(new ClickableItem(item, -1) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    User clicked = event.getGameGroup().getUser(userUUID);

                    if(clicked == null){
                        event.getUser().redoInventory();
                        return;
                    }

                    event.getUser().teleport(clicked.getLocation());
                    event.getUser().closeInventory();
                }
            });
        }

        event.getUser().showInventory(inv, null);
    }
}
