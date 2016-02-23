package com.ithinkrok.minigames.util.item;

import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 11/01/16.
 */
public class KitChooser implements CustomListener {

    private List<String> choosableKits;

    private String chosenLocale, alreadyLocale, titleLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfig();

        choosableKits = config.getStringList("choosable_kits");

        chosenLocale = config.getString("chosen_locale", "kit_chooser.choose.chosen");
        alreadyLocale = config.getString("already_chosen_locale", "kit_chooser.choose.already_chosen");
        titleLocale = config.getString("title_locale", "kit_chooser.choose.title");
    }

    @CustomEventHandler
    public void onRightClick(UserInteractEvent event) {
        if (event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;
        event.setCancelled(true);

        User user = event.getUser();

        ClickableInventory inventory = new ClickableInventory(user.getLanguageLookup().getLocale(titleLocale));


        for (String kitName : choosableKits) {
            Kit kit = event.getUserGameGroup().getKit(kitName);
            ItemStack display = kit.getItem().clone();

            ClickableItem item = new ClickableItem(display, -1) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    if (kitName.equals(event.getUser().getKitName())) {
                        event.getUser().sendLocale(alreadyLocale, kit.getFormattedName());
                        return;
                    }

                    event.getUser().setKit(kit);
                    event.getUser().sendLocale(chosenLocale, kit.getFormattedName());
                    event.getUser().closeInventory();
                }
            };

            inventory.addItem(item);
        }

        user.showInventory(inventory, null);
    }
}
