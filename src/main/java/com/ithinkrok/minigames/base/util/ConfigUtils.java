package com.ithinkrok.minigames.base.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 01/01/16.
 */
public class ConfigUtils {

    public static Location getLocation(ConfigurationSection config, World world, String path) {
        if (!path.isEmpty()) path = path + ".";

        return new Location(world, config.getDouble(path + "x"), config.getDouble(path + "y"),
                config.getDouble(path + "z"), (float) config.getDouble(path + "yaw"),
                (float) config.getDouble(path + "pitch"));
    }

    public static BoundingBox getBounds(ConfigurationSection config, String path) {
        if (!path.isEmpty()) path = path + ".";

        Vector min = getVector(config, path + "min");
        Vector max = getVector(config, path + "max");

        return new BoundingBox(min, max);
    }

    public static Vector getVector(ConfigurationSection config, String path) {
        if (!path.isEmpty()) path = path + ".";

        return new Vector(config.getDouble(path + "x"), config.getDouble(path + "y"), config.getDouble(path + "z"));
    }

    @SuppressWarnings("unchecked")
    public static List<ConfigurationSection> getConfigList(ConfigurationSection config, String path) {
        List<Map<?, ?>> list = config.getMapList(path);

        List<ConfigurationSection> result = new ArrayList<>();
        if (list == null) return result;

        for (Map<?, ?> vecMap : list) {
            ConfigurationSection vec = configFromMap((Map<String, Object>) vecMap);
            if (vec != null) result.add(vec);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static ConfigurationSection configFromMap(Map<String, Object> values) {
        values.replaceAll((s, o) -> {
            if (!(o instanceof Map<?, ?>)) return o;
            return configFromMap((Map<String, Object>) o);
        });

        MemoryConfiguration memory = new MemoryConfiguration();
        memory.addDefaults(values);

        return memory.getDefaults();
    }

    public static ItemStack getItemStack(ConfigurationSection config, String path) {
        if (config.isString(path)) return InventoryUtils.parseItem(config.getString(path));
        if (!config.isConfigurationSection(path)) return null;

        if(!path.isEmpty()) config = config.getConfigurationSection(path);

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

    public static List<Vector> getVectorList(ConfigurationSection config, String path) {
        List<Map<?, ?>> list = config.getMapList(path);

        List<Vector> result = new ArrayList<>();
        if (list == null) return result;

        for (Map<?, ?> vecMap : list) {
            Vector vec = vectorFromMap(vecMap);
            if (vec != null) result.add(vec);
        }

        return result;
    }

    private static Vector vectorFromMap(Map<?, ?> vecMap) {
        try {
            double x = ((Number) vecMap.get("x")).doubleValue();
            double y = ((Number) vecMap.get("y")).doubleValue();
            double z = ((Number) vecMap.get("z")).doubleValue();
            return new Vector(x, y, z);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }
}
