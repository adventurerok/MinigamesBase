package com.ithinkrok.minigames.api.inventory;

import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 02/01/16.
 */
public class ClickableInventory {


    private final String title;
    private final Map<Integer, ClickableItem> items = new LinkedHashMap<>();

    public ClickableInventory(Config config) {
        this.title = config.getString("title", "Missing Inv Title");

        loadFromConfig(config.getConfigList("items"));
    }

    @SuppressWarnings("unchecked")
    public void loadFromConfig(List<Config> items) {
        for (Config config : items) {
            String className = config.getString("class");
            ItemStack display = MinigamesConfigs.getItemStack(config, "display");
            try {
                Class<? extends ClickableItem> itemClass = (Class<? extends ClickableItem>) Class.forName(className);

                Constructor<? extends ClickableItem> constructor = itemClass.getConstructor(ItemStack.class);
                ClickableItem item = constructor.newInstance(display);

                Config itemConfig = config.getConfigOrNull("config");
                if (itemConfig != null) item.configure(itemConfig);

                addItem(item);
            } catch (ReflectiveOperationException | ClassCastException e) {
                throw new RuntimeException("Failed while loading ClickableItem from config", e);
            }
        }
    }

    public void addItem(ClickableItem item) {
        items.put(item.getIdentifier(), item);
    }

    public ClickableInventory(String title) {
        this.title = title;
    }

    public Inventory createInventory(User user) {
        Inventory inventory = user.createInventory(items.size(), title);

        return populateInventory(inventory, user);
    }

    public Inventory createInventory(User user, Inventory old) {
        if(old == null || items.size() > old.getSize()) {
            old = user.createInventory(items.size(), title);
        } else {
            old.clear();
        }

        return populateInventory(old, user);
    }

    public Inventory populateInventory(Inventory inventory, User user) {
        for (ClickableItem item : items.values()) {
            CalculateItemForUserEvent event = new CalculateItemForUserEvent(user, this, item);

            item.onCalculateItem(event);
            if (event.getDisplay() == null) continue;
            if (InventoryUtils.getIdentifier(event.getDisplay()) == -1) {
                event.setDisplay(InventoryUtils.addIdentifier(event.getDisplay().clone(), item.getIdentifier()));
            }

            inventory.addItem(event.getDisplay());
        }

        return inventory;
    }

    public void inventoryClick(UserInventoryClickEvent event) {
        event.setCancelled(true);

        if (InventoryUtils.isEmpty(event.getItemInSlot())) return;
        int identifier = InventoryUtils.getIdentifier(event.getItemInSlot());

        ClickableItem item = items.get(identifier);
        if (item == null) return;

        item.onClick(new UserClickItemEvent(event.getUser(), this, item, event.getClickType()));
    }
}
