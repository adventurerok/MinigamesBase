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
import java.util.*;

/**
 * Created by paul on 02/01/16.
 */
public class ClickableInventory {


    private final String title;
    private final String identifier;
    private final Map<String, ClickableItem> items = new LinkedHashMap<>();


    public ClickableInventory(Config config) {
        this.title = config.getString("title", "Missing Inv Title");
        this.identifier = config.getString("identifier", title);

        loadFromConfig(config.getConfigList("items"));
    }


    @SuppressWarnings("unchecked")
    public void loadFromConfig(List<Config> items) {
        for (Config config : items) {
            String className = config.getString("class");
            ItemStack display = MinigamesConfigs.getItemStack(config, "display");
            int slot = config.getInt("slot", -1);

            try {
                Class<? extends ClickableItem> itemClass = (Class<? extends ClickableItem>) Class.forName(className);

                Constructor<? extends ClickableItem> constructor = itemClass.getConstructor(ItemStack.class, int.class);
                ClickableItem item = constructor.newInstance(display, slot);

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
        this(title, title);
    }


    public ClickableInventory(String title, String identifier) {
        this.title = title;
        this.identifier = identifier;
    }


    public String getIdentifier() {
        return identifier;
    }

    public Collection<ClickableItem> getItems() {
        return items.values();
    }


    public Inventory createInventory(User user, Inventory old) {
        int highestSlot = items.size();
        for (ClickableItem item : items.values()) {
            if (item.getSlot() + 1 > highestSlot) {
                highestSlot = item.getSlot() + 1;
            }
        }

        if (old == null || highestSlot > old.getSize() || !Objects.equals(old.getTitle(), title)) {
            old = user.createInventory(highestSlot, title);
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
            event.setDisplay(InventoryUtils.addIdentifier(event.getDisplay().clone(), item.getIdentifier()));

            if (item.getSlot() >= 0) {
                inventory.setItem(item.getSlot(), event.getDisplay());
            } else {
                inventory.addItem(event.getDisplay());
            }
        }

        return inventory;
    }


    public void inventoryClick(UserInventoryClickEvent event) {
        event.setCancelled(true);

        if (InventoryUtils.isEmpty(event.getItemInSlot())) return;
        String identifier = InventoryUtils.getIdentifier(event.getItemInSlot());

        ClickableItem item = items.get(identifier);
        if (item == null) {
            throw new RuntimeException("Item in slot " + event.getItemInSlot() + " of inventory " + identifier +
                                       " is not registered. Identifier is " + identifier);
        }

        item.onClick(new UserClickItemEvent(event.getUser(), this, item, event.getClickType()));
    }
}
