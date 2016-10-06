package com.ithinkrok.minigames.util;

import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.util.math.MapVariables;
import com.ithinkrok.util.math.Variables;
import com.ithinkrok.util.config.ConfigUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by paul on 30/01/16.
 */
public class ItemGiver {

    private final List<CustomItemInfo> items = new ArrayList<>();
    private final List<BookInfo> books = new ArrayList<>();
    private boolean clearInventory = false;

    public ItemGiver(Config config) {
        if (config == null) config = ConfigUtils.EMPTY_CONFIG;
        clearInventory = config.getBoolean("clear_inventory");

        List<Config> itemConfigs = config.getConfigList("custom_items");

        items.addAll(itemConfigs.stream().map(CustomItemInfo::new).collect(Collectors.toList()));

        List<Config> bookConfigs = config.getConfigList("books");

        for(Config bookConfig : bookConfigs) {
            books.add(new BookInfo(bookConfig));
        }
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

        for(BookInfo bookInfo : books) {
            JSONBook book = user.getGameGroup().getBook(bookInfo.bookName);

            if(!book.isBookItemCreated() && !book.createBookItem(user)) continue;

            if(bookInfo.slot < 0) user.getInventory().addItem(book.getBookItem());
            else user.getInventory().setItem(bookInfo.slot, book.getBookItem());
        }
    }

    private static class CustomItemInfo {
        private final String customItem;
        private final int slot;
        private Variables customVariables;

        public CustomItemInfo(Config config) {
            customItem = config.getString("name");
            slot = config.getInt("slot", -1);

            if (!config.contains("custom_variables")) return;
            customVariables = new MapVariables(config.getConfigOrEmpty("custom_variables"));
        }
    }

    private static class BookInfo {
        private final String bookName;
        private final int slot;

        public BookInfo(Config config) {
            bookName = config.getString("name");
            slot = config.getInt("slot", -1);
        }
    }
}
