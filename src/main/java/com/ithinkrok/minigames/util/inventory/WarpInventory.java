package com.ithinkrok.minigames.util.inventory;

import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.StringUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class WarpInventory {


    public static void showToUser(String title, List<Config> warps, User user) {
        ClickableInventory inventory = new ClickableInventory(title);

        for (Config warp : warps) {
            ItemStack display;
            display = MinigamesConfigs.getItemStack(warp, "item");

            if (display == null) {
                display = new ItemStack(Material.EMPTY_MAP);
            }


            String warpName = StringUtils.convertAmpersandToSelectionCharacter(warp.getString("name"));
            display = InventoryUtils.setItemName(display, warpName);
            if (warp.contains("desc")) {
                List<String> lore = warp.getStringList("lore").stream()
                        .map(StringUtils::convertAmpersandToSelectionCharacter)
                        .collect(Collectors.toList());

                display = InventoryUtils.addLore(display, lore);
            }

            ClickableItem clickable = new ClickableItem(display, warp.getInt("slot", -1)) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    MapPoint pos = MinigamesConfigs.getMapPoint(warp, "pos");
                    event.getUser().teleport(pos);
                }
            };

            inventory.addItem(clickable);
        }

        user.showInventory(inventory, null);
    }

}
