package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.io.ListenerLoader;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomListener;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by paul on 07/01/16.
 */
public class Kit implements Nameable {

    private final String name;
    private final String formattedName;
    private final String description;
    private final ItemStack item;
    private final Collection<Config> listeners;

    public Kit(String name, String formattedName, String description, ItemStack item, Collection<Config> listeners) {
        this.name = name;

        if(formattedName != null) {
            this.formattedName = formattedName;
        } else {
            this.formattedName = WordUtils.capitalizeFully(name.replace('_', ' '));
        }

        this.description = description;

        if(item == null) item = new ItemStack(Material.WOOD_SWORD);

        InventoryUtils.setItemNameAndLore(item, this.formattedName, description);

        this.item = item;

        this.listeners = listeners;


    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormattedName() {
        return formattedName;
    }

    public String getDescription() {
        return description;
    }

    public Collection<CustomListener> createListeners(User user) {
        Collection<CustomListener> result = new ArrayList<>();

        for (Config listenerConfig : listeners) {
            try {
                result.add(ListenerLoader.loadListener(user, this, listenerConfig));
            } catch (Exception e) {
                System.out.println("Failed to create listener for kit: " + name);
                e.printStackTrace();
            }
        }

        return result;
    }
}
