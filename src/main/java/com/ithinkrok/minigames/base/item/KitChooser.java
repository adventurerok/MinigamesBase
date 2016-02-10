package com.ithinkrok.minigames.base.item;

import com.ithinkrok.minigames.base.Kit;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.base.inventory.ClickableInventory;
import com.ithinkrok.minigames.base.inventory.ClickableItem;
import com.ithinkrok.minigames.base.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.base.util.InventoryUtils;
import com.ithinkrok.minigames.base.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by paul on 11/01/16.
 */
public class KitChooser implements CustomListener {

    private final Map<String, ItemStack> choosableKits = new LinkedHashMap<>();

    private String chosenLocale, alreadyLocale, titleLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfig();

        Config kits = config.getConfigOrNull("choosable_kits");

        for (String kitName : kits.getKeys(false)) {
            choosableKits.put(kitName, MinigamesConfigs.getItemStack(kits, kitName));
        }

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


        for (String kitName : choosableKits.keySet()) {
            Kit kit = event.getUserGameGroup().getKit(kitName);
            ItemStack display = choosableKits.get(kitName);
            if (InventoryUtils.getDisplayName(display) == null) {
                InventoryUtils.setItemNameAndLore(display, kit.getFormattedName());
            }

            ClickableItem item = new ClickableItem(display.clone()) {
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
