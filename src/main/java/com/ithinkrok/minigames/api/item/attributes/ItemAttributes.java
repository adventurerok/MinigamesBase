package com.ithinkrok.minigames.api.item.attributes;

import com.ithinkrok.util.config.Config;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents some attribute modifiers, that can be applied on items.
 *
 * @author Michel_0
 */
@SuppressWarnings("ConstantConditions")
public class ItemAttributes {

    private NBTTagList modifiers;


    /**
     * Constructor:
     * Generates a new instance with an empty NBT tag list.
     */
    public ItemAttributes() {
        this.modifiers = new NBTTagList();
    }


    /**
     * Add an modifier to this set of attribute modifiers for an item.
     *
     * @param modifier The modifier, that should be added
     */
    public void addModifier(ItemAttributeModifier modifier) {
        if (this.modifiers == null) return;

        this.modifiers.add(modifier.getNBT());
    }


    public void fromConfig(List<Config> attributes) {
        if (this.modifiers == null) return;

        for (Config attribute : attributes) {
            this.modifiers.add(new ItemAttributeModifier(attribute).getNBT());
        }

    }


    /**
     * Remove an prior added modifier from this set of attribute modifiers.
     *
     * @param i The index of the modifier, that should be removed
     */
    public void removeModifier(int i) {
        this.modifiers.remove(i);
    }


    /**
     * Iterates over all Modifiers and removes all equals like the given modifier.
     *
     * @param modifier The modifier, that should be removed
     */
    public void removeModifier(ItemAttributeModifier modifier) {
        int size = this.modifiers.size();

        for (int i = 0; i < size; i++) {
            if (this.modifiers.get(i).equals(modifier.getNBT())) {
                this.modifiers.remove(i);
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

            List<ItemAttributeModifier> modifiers = new ArrayList<>();
            int size = this.modifiers.size();
            for (int i = 0; i < size; i++) {
                modifiers.add(new ItemAttributeModifier(this.modifiers.get(i)));
            }
            return modifiers;

        } else return Collections.emptyList();
    }


    /**
     * Apply this set of attribute modifiers on an item.
     * The item won't be changed, but an new item with the modifiers will be returned.
     *
     * @param item The item, on which the modifiers should be added
     * @return The item with added modifiers or null if incompatible server version
     */
    public ItemStack apply(ItemStack item) {
        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        //creates the tag if it does not exist, and sets the tag compound's AttributeModifiers to ours
        nmsItem.a("AttributeModifiers", this.modifiers);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }


    /**
     * Get all attribute modifiers out of an item.
     * All present attributes will be overridden
     *
     * @param item The item where the modifiers should be read out
     */
    public void getFromStack(ItemStack item) {

        //MinigamesBase only targets one version of Minecraft at once.

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = nmsItem.getTag();

        this.modifiers = tag.getList("AttributeModifiers", 10);
        if (this.modifiers == null) {
            this.modifiers = new NBTTagList();
        }
    }


}