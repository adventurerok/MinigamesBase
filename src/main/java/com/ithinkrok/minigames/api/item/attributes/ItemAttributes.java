package com.ithinkrok.minigames.api.item.attributes;

import com.ithinkrok.minigames.api.util.ReflectionUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents some attribute modifiers, that can be applied on items.
 *
 * @author Michel_0
 */
@SuppressWarnings("ConstantConditions")
public class ItemAttributes {

    private Object modifiers;

    /**
     * Constructor:
     * Generates a new instance with an empty NBT tag list.
     */
    public ItemAttributes() {
        try {
            this.modifiers = ReflectionUtils.getNMSClass("NBTTagList").newInstance();
            if (this.modifiers == null)
                Bukkit.getLogger().info("[ItemAttributeAPI] Incompatible Server version! Missing classes.");
        } catch (InstantiationException | IllegalAccessException e) {
            Bukkit.getLogger().info("[ItemAttributeAPI] Incompatible server version! Some methods can't be applied.");
        }
    }


    /**
     * Add an modifier to this set of attribute modifiers for an item.
     *
     * @param modifier The modifier, that should be added
     */
    public void addModifier(ItemAttributeModifier modifier) {
        if (this.modifiers == null) return;

        try {
            this.modifiers.getClass().getMethod("add", ReflectionUtils.getNMSClass("NBTBase"))
                    .invoke(this.modifiers, modifier.getNBT());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            Bukkit.getLogger().info("[ItemAttributeAPI] Incompatible server version! Some methods can't be applied.");
        }

    }

    public void fromConfig(List<Config> attributes) {
        if (this.modifiers == null) return;

        for (Config attribute : attributes) {
            try {
                this.modifiers.getClass().getMethod("add", ReflectionUtils.getNMSClass("NBTBase"))
                        .invoke(this.modifiers, new ItemAttributeModifier(attribute).getNBT());
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                Bukkit.getLogger().info("[ItemAttributeAPI] Incompatible server version! Some methods can't be applied.");
            }
        }

    }

    /**
     * Remove an prior added modifier from this set of attribute modifiers.
     *
     * @param i The index of the modifier, that should be removed
     */
    public void removeModifier(int i) {
        if (this.modifiers != null) {
            try {
                try {
                    // net.minecraft.server.v1_8_R3.NBTTagList.a(int)
                    this.modifiers.getClass().getMethod("a", int.class).invoke(this.modifiers, i);
                } catch (NoSuchMethodException e) {
                    // net.minecraft.server.v1_9_R1.NBTTagList.remove(int)
                    this.modifiers.getClass().getMethod("remove", int.class).invoke(this.modifiers, i);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                Bukkit.getLogger()
                        .info("[ItemAttributeAPI] Incompatible server version! Some methods can't be applied.");
            }
        }
    }

    /**
     * Iterates over all Modifiers and removes all equals like the given modifier.
     *
     * @param modifier The modifier, that should be removed
     */
    public void removeModifier(ItemAttributeModifier modifier) {
        if (this.modifiers != null) {
            try {
                int size = (int) this.modifiers.getClass().getMethod("size").invoke(this.modifiers);
                for (int i = 0; i < size; i++) {
                    if (this.modifiers.getClass().getMethod("get", int.class).invoke(this.modifiers, i)
                            .equals(modifier.getNBT())) {
                        try {
                            // net.minecraft.server.v1_8_R3.NBTTagList.a(int)
                            this.modifiers.getClass().getMethod("a", int.class).invoke(this.modifiers, i);
                        } catch (NoSuchMethodException e) {
                            // net.minecraft.server.v1_9_R1.NBTTagList.remove(int)
                            this.modifiers.getClass().getMethod("remove", int.class).invoke(this.modifiers, i);
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | SecurityException | NoSuchMethodException e) {
                Bukkit.getLogger()
                        .info("[ItemAttributeAPI] Incompatible server version! Some methods can't be applied.");
            }
        }
    }

    /**
     * Get all modifiers in this set of attribute modifiers.
     *
     * @return All modifiers or null if incompatible server version
     */
    public List<ItemAttributeModifier> getModifiers() {
        if (this.modifiers != null) {
            try {
                List<ItemAttributeModifier> modifiers = new ArrayList<>();
                int size = (int) this.modifiers.getClass().getMethod("size").invoke(this.modifiers);
                for (int i = 0; i < size; i++) {
                    modifiers.add(new ItemAttributeModifier(
                            this.modifiers.getClass().getMethod("get", int.class).invoke(this.modifiers, i)));
                }
                return modifiers;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                Bukkit.getLogger()
                        .info("[ItemAttributeAPI] Incompatible server version! Some methods can't be applied.");
                return null;
            }
        } else return null;
    }

    /**
     * Apply this set of attribute modifiers on an item.
     * The item won't be changed, but an new item with the modifiers will be returned.
     *
     * @param item The item, on which the modifiers should be added
     * @return The item with added modifiers or null if incompatible server version
     */
    public ItemStack apply(ItemStack item) {
        try {
            Object itemNMS =
                    ReflectionUtils.getCBClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class)
                            .invoke(null, item);
            itemNMS.getClass().getMethod("a", new Class[]{String.class, ReflectionUtils.getNMSClass("NBTBase")})
                    .invoke(itemNMS, "AttributeModifiers", this.modifiers);
            return (ItemStack) ReflectionUtils.getCBClass("inventory.CraftItemStack")
                    .getMethod("asBukkitCopy", ReflectionUtils.getNMSClass("ItemStack")).invoke(null, itemNMS);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            Bukkit.getLogger().info("[ItemAttributeAPI] Incompatible server version! Some methods can't be applied.");
            return null;
        }
    }

    /**
     * Get all attribute modifiers out of an item.
     * All present attributes will be overridden
     *
     * @param item The item where the modifiers should be read out
     */
    public void getFromStack(ItemStack item) {
        try {
            Object itemNMS =
                    ReflectionUtils.getCBClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class)
                            .invoke(null, item);
            Object itemNMSTag = itemNMS.getClass().getMethod("getTag").invoke(itemNMS);
            this.modifiers = itemNMSTag.getClass().getMethod("getList", new Class[]{String.class, int.class})
                    .invoke(itemNMSTag, "AttributeModifiers", 10);
            if (this.modifiers == null) this.modifiers = ReflectionUtils.getNMSClass("NBTTagList").newInstance();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
            Bukkit.getLogger().info("[ItemAttributeAPI] Incompatible server version! Some methods can't be applied.");
        }
    }


}