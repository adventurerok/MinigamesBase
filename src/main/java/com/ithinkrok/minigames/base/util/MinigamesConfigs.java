package com.ithinkrok.minigames.base.util;

import com.ithinkrok.msm.common.util.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Created by paul on 05/02/16.
 */
public class MinigamesConfigs {

    public static BoundingBox getBounds(ConfigurationSection config, String path) {
        if (!path.isEmpty()) path = path + ".";

        Vector min = ConfigUtils.getVector(config, path + "min");
        Vector max = ConfigUtils.getVector(config, path + "max");

        return new BoundingBox(min, max);
    }

    public static CountdownConfig getCountdown(ConfigurationSection config, String path) {
        return getCountdown(config, path, null, 0, null);
    }

    public static CountdownConfig getCountdown(ConfigurationSection config, String path, String defaultName,
                                               int defaultSeconds, String defaultStub) {
        return new CountdownConfig(ConfigUtils.getConfigOrEmpty(config, path), defaultName, defaultSeconds,
                defaultStub);
    }

    public static ItemStack getItemStack(ConfigurationSection config, String path) {
        if (config.isString(path)) return InventoryUtils.parseItem(config.getString(path));
        if (!config.isConfigurationSection(path)) return null;

        if (!path.isEmpty()) config = config.getConfigurationSection(path);

        Material mat = Material.matchMaterial(config.getString("type"));
        int amount = config.getInt("amount", 1);
        int damage = config.getInt("damage", 0);

        String name = config.getString("name", null);

        //TODO add more options for ItemStack loading

        return InventoryUtils.createItemWithNameAndLore(mat, amount, damage, name);
    }

    public static SoundEffect getSoundEffect(ConfigurationSection config, String path) {
        if (config.isString(path)) return new SoundEffect(config.getString(path));
        else if (config.isConfigurationSection(path)) return new SoundEffect(config.getConfigurationSection(path));
        else return null;
    }
}
