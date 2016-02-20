package com.ithinkrok.minigames.api.util;

import com.ithinkrok.minigames.api.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 17/02/16.
 */
public class JSONBook {

    private final String name;
    private final String json;

    private ItemStack bookItem;

    public JSONBook(String name, String json) {
        this.name = name;
        this.json = json;
    }

    public String getName() {
        return name;
    }

    public String getJson() {
        return json;
    }

    public boolean giveToUser(User user) {
        if (bookItem != null) {
            return user.getInventory().addItem(bookItem.clone()).isEmpty();
        }

        if(!createBookItem(user)) return false;

        return user.getInventory().addItem(bookItem.clone()).isEmpty();
    }

    public boolean isBookItemCreated() {
        return bookItem != null;
    }

    public boolean createBookItem(User user) {
        if(bookItem != null) return true;
        if(!user.isPlayer()) return false;

        PlayerInventory inv = user.getInventory();

        int oldIndex = Math.max(inv.first(Material.WRITTEN_BOOK), 0);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "minecraft:give " + user.getName() + " written_book 1 0 " + json);

        for (int index = oldIndex; index < inv.getSize(); ++index) {
            ItemStack item = inv.getItem(index);
            if (!InventoryUtils.isMaterial(item, Material.WRITTEN_BOOK)) continue;

            bookItem = item.clone();
            bookItem.setAmount(1);

            inv.setItem(index, null);
            return true;
        }

        return false;
    }

    /**
     *
     * @return The book item if it has been created, otherwise null
     */
    public ItemStack getBookItem() {
        return bookItem.clone();
    }
}
