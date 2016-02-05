package com.ithinkrok.minigames.base.util;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.minigames.base.util.math.MapVariables;
import com.ithinkrok.minigames.base.util.math.Variables;
import com.ithinkrok.msm.common.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by paul on 30/01/16.
 */
public class CustomItemGiver {

    private final List<CustomItemInfo> items = new ArrayList<>();
    private boolean clearInventory = false;

    public CustomItemGiver(ConfigurationSection config) {
        if (config == null) config = ConfigUtils.EMPTY_CONFIG;
        clearInventory = config.getBoolean("clear_inventory");

        List<ConfigurationSection> itemConfigs = ConfigUtils.getConfigList(config, "items");
        if (itemConfigs == null) return;

        items.addAll(itemConfigs.stream().map(CustomItemInfo::new).collect(Collectors.toList()));
    }

    public void giveToUser(User user) {
        if (clearInventory) user.getInventory().clear();

        for (CustomItemInfo itemInfo : items) {
            CustomItem item = user.getGameGroup().getCustomItem(itemInfo.customItem);
            ItemStack itemStack;

            if (itemInfo.customVariables != null)
                itemStack = item.createWithVariables(user.getGameGroup(), itemInfo.customVariables);
            else itemStack = user.createCustomItemForUser(item);

            if (itemInfo.slot < 0) user.getInventory().addItem(itemStack);
            else user.getInventory().setItem(itemInfo.slot, itemStack);
        }
    }

    private static class CustomItemInfo {
        private final String customItem;
        private final int slot;
        private Variables customVariables;

        public CustomItemInfo(ConfigurationSection config) {
            customItem = config.getString("name");
            slot = config.getInt("slot", -1);

            if (!config.contains("custom_variables")) return;
            customVariables = new MapVariables(config.getConfigurationSection("custom_variables"));
        }
    }
}
