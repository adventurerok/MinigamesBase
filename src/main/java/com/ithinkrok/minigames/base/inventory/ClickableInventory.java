package com.ithinkrok.minigames.base.inventory;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.base.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.base.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.base.util.ConfigUtils;
import com.ithinkrok.minigames.base.util.InventoryUtils;
import org.bukkit.configuration.ConfigurationSection;
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
    private Map<Integer, ClickableItem> items = new LinkedHashMap<>();

    public ClickableInventory(ConfigurationSection config) {
        this.title = config.getString("title", "Missing Inv Title");

        loadFromConfig(ConfigUtils.getConfigList(config, "items"));
    }

    @SuppressWarnings("unchecked")
    public void loadFromConfig(List<ConfigurationSection> items) {
        for(ConfigurationSection config : items) {
            String className = config.getString("class");
            ItemStack display = ConfigUtils.getItemStack(config, "display");
            try {
                Class<? extends ClickableItem> itemClass = (Class<? extends ClickableItem>) Class.forName(className);

                Constructor<? extends ClickableItem> constructor = itemClass.getConstructor(ItemStack.class);
                ClickableItem item = constructor.newInstance(display);

                ConfigurationSection itemConfig = config.getConfigurationSection("config");
                if(itemConfig != null) item.configure(itemConfig);

                addItem(item);
            } catch (ReflectiveOperationException | ClassCastException e) {
                throw new RuntimeException("Failed while loading ClickableItem from config", e);
            }
        }
    }

    public ClickableInventory(String title) {
        this.title = title;
    }

    public void addItem(ClickableItem item) {
        items.put(item.getIdentifier(), item);
    }

    public Inventory createInventory(User user) {
        Inventory inventory = user.createInventory(items.size(), title);

        return populateInventory(inventory, user);
    }

    public Inventory populateInventory(Inventory inventory, User user) {
        for(ClickableItem item : items.values()) {
            CalculateItemForUserEvent event = new CalculateItemForUserEvent(user, this, item);

            item.onCalculateItem(event);
            if(event.getDisplay() == null) continue;
            if(InventoryUtils.getIdentifier(event.getDisplay()) == -1) {
                event.setDisplay(InventoryUtils.addIdentifier(event.getDisplay().clone(), item.getIdentifier()));
            }

            inventory.addItem(event.getDisplay());
        }

        return inventory;
    }

    public void inventoryClick(UserInventoryClickEvent event) {
        event.setCancelled(true);

        if(InventoryUtils.isEmpty(event.getItemInSlot())) return;
        int identifier = InventoryUtils.getIdentifier(event.getItemInSlot());

        ClickableItem item = items.get(identifier);
        if(item == null) return;

        item.onClick(new UserClickItemEvent(event.getUser(), this, item, event.getClickType()));
    }
}
