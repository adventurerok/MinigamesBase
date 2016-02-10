package com.ithinkrok.minigames.base.item;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.base.inventory.ClickableInventory;
import com.ithinkrok.minigames.base.inventory.ClickableItem;
import com.ithinkrok.minigames.base.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.base.util.InventoryUtils;
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

        ClickableInventory inv = new ClickableInventory(event.getUserGameGroup().getLocale(titleLocale));

        for(User user : event.getUserGameGroup().getUsers()) {
            if(!user.isInGame()) continue;

            ItemStack item = InventoryUtils.createItemWithNameAndLore(Material.SKULL_ITEM, 1, 3, user
                    .getFormattedName());

            UUID userUUID = user.getUuid();
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwner(user.getName());
            item.setItemMeta(meta);

            inv.addItem(new ClickableItem(item) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    User clicked = event.getUserGameGroup().getUser(userUUID);

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
