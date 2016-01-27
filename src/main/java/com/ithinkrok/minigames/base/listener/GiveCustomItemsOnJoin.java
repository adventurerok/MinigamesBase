package com.ithinkrok.minigames.base.listener;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.util.math.Variables;
import com.ithinkrok.minigames.base.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.minigames.base.util.ConfigUtils;
import com.ithinkrok.minigames.base.util.math.MapVariables;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private static class CustomItemInfo {
        private Variables customVariables;
        private String customItem;
        private int slot = -1;

        public CustomItemInfo(ConfigurationSection config) {
            customItem = config.getString("name");
            slot = config.getInt("slot", -1);

            if (!config.contains("custom_variables")) return;
            customVariables = new MapVariables(config.getConfigurationSection("custom_variables"));
        }
    }

    public static class CustomItemGiver {
        private boolean clearInventory = false;

        private List<CustomItemInfo> items = new ArrayList<>();

        public CustomItemGiver(ConfigurationSection config) {
            clearInventory = config.getBoolean("clear_inventory");

            List<ConfigurationSection> itemConfigs = ConfigUtils.getConfigList(config, "items");
            items.addAll(itemConfigs.stream().map(CustomItemInfo::new).collect(Collectors.toList()));
        }

        public void giveToUser(User user) {
            if(clearInventory) user.getInventory().clear();

            for (CustomItemInfo itemInfo : items) {
                CustomItem item = user.getGameGroup().getCustomItem(itemInfo.customItem);
                ItemStack itemStack;

                if (itemInfo.customVariables != null)
                    itemStack = item.createWithVariables(user.getGameGroup(), itemInfo.customVariables);
                else itemStack = user.createCustomItemForUser(item);

                if(itemInfo.slot < 0) user.getInventory().addItem(itemStack);
                else user.getInventory().setItem(itemInfo.slot, itemStack);
            }
        }
    }
}
