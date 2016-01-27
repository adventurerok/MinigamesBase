package com.ithinkrok.minigames.base.util.playerstate;

import com.ithinkrok.minigames.base.util.InventoryUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 31/12/15.
 */

public class ArmorCapture implements EntityEquipment {

    private ItemStack helmet, chestplate, leggings, boots, holding;

    public ArmorCapture(ItemStack[] armorContents){
        setArmorContents(armorContents);
    }

    @Override
    public ItemStack getItemInHand() {
        return holding;
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        if(InventoryUtils.isEmpty(stack)) stack = null;
        holding = stack;
    }

    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        if(InventoryUtils.isEmpty(helmet)) helmet = null;

        this.helmet = helmet;
    }

    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        if(InventoryUtils.isEmpty(chestplate)) chestplate = null;

        this.chestplate = chestplate;
    }

    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        if(InventoryUtils.isEmpty(leggings)) leggings = null;

        this.leggings = leggings;
    }

    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void setBoots(ItemStack boots) {
        if(InventoryUtils.isEmpty(boots)) boots = null;

        this.boots = boots;
    }

    @Override
    public ItemStack[] getArmorContents() {
        return new ItemStack[]{boots, leggings, chestplate, helmet};
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        Validate.notNull(items, "items cannot be null");
        Validate.isTrue(items.length >= 4, "items must be of length 4");

        setBoots(items[0]);
        setLeggings(items[1]);
        setChestplate(items[2]);
        setHelmet(items[3]);
    }

    @Override
    public void clear() {
        setHelmet(null);
        setChestplate(null);
        setLeggings(null);
        setBoots(null);
    }

    @Override
    public float getItemInHandDropChance() {
        return 0;
    }

    @Override
    public void setItemInHandDropChance(float chance) {

    }

    @Override
    public float getHelmetDropChance() {
        return 0;
    }

    @Override
    public void setHelmetDropChance(float chance) {

    }

    @Override
    public float getChestplateDropChance() {
        return 0;
    }

    @Override
    public void setChestplateDropChance(float chance) {

    }

    @Override
    public float getLeggingsDropChance() {
        return 0;
    }

    @Override
    public void setLeggingsDropChance(float chance) {

    }

    @Override
    public float getBootsDropChance() {
        return 0;
    }

    @Override
    public void setBootsDropChance(float chance) {

    }

    @Override
    public Entity getHolder() {
        throw new UnsupportedOperationException("ArmorCapture has no entity");
    }
}
