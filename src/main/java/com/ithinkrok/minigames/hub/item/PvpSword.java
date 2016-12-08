package com.ithinkrok.minigames.hub.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.inventory.UserItemHeldEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 08/12/16.
 */
public class PvpSword implements CustomListener {


    private int customItemId;

    private ItemStack helmetArmor, chestArmor, legsArmor, bootsArmor;

    private String pvpStartLocale, pvpEndLocale;

    @CustomEventHandler
    public void onListenerLoadedEvent(ListenerLoadedEvent<?, CustomItem> event) {
        customItemId = event.getRepresenting().getIdentifier();

        Config config = event.getConfigOrEmpty();

        helmetArmor = MinigamesConfigs.getItemStack(config, "armor.helmet");
        chestArmor = MinigamesConfigs.getItemStack(config, "armor.chestplate");
        legsArmor = MinigamesConfigs.getItemStack(config, "armor.leggings");
        bootsArmor = MinigamesConfigs.getItemStack(config, "armor.boots");

        pvpStartLocale = config.getString("pvp_start_locale", "pvp_sword.pvp_start");
        pvpEndLocale = config.getString("pvp_end_locale", "pvp_sword.pvp_end");
    }

    @CustomEventHandler
    public void onUserHeldChange(UserItemHeldEvent event) {
        User user = event.getUser();
        PlayerInventory inventory = user.getInventory();

        if(InventoryUtils.getIdentifier(event.getOldHeldItem()) == customItemId) {
            if(InventoryUtils.getIdentifier(event.getNewHeldItem()) == customItemId) return;

            //Clear the user's armor to show they are not in pvp mode
            user.clearArmor();

            user.sendLocale(pvpEndLocale);
        } else if(InventoryUtils.getIdentifier(event.getNewHeldItem()) == customItemId) {
            //Give the user diamond armor

            inventory.setHelmet(new ItemStack(helmetArmor));
            inventory.setChestplate(new ItemStack(chestArmor));
            inventory.setLeggings(new ItemStack(legsArmor));
            inventory.setBoots(new ItemStack(bootsArmor));

            user.resetUserStats(true);

            user.sendLocale(pvpStartLocale);
        }
    }
}
