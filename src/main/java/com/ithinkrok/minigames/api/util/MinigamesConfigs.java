package com.ithinkrok.minigames.api.util;

import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.StringUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Created by paul on 05/02/16.
 */
public class MinigamesConfigs {

    public static BoundingBox getBounds(Config config, String path) {
        if (!path.isEmpty()) path = path + ".";

        Vector min = BukkitConfigUtils.getVector(config, path + "min");
        Vector max = BukkitConfigUtils.getVector(config, path + "max");

        return new BoundingBox(min, max);
    }

    public static CountdownConfig getCountdown(Config config, String path) {
        return getCountdown(config, path, null, 0, null);
    }

    public static CountdownConfig getCountdown(Config config, String path, String defaultName,
                                               int defaultSeconds, String defaultStub) {
        return new CountdownConfig(config.getConfigOrEmpty(path), defaultName, defaultSeconds,
                defaultStub);
    }

    public static ItemStack getItemStack(Config config, String path) {
        if (config.isString(path)) return InventoryUtils.parseItem(config.getString(path));
        if (!config.isConfig(path)) return null;

        if (!path.isEmpty()) config = config.getConfigOrNull(path);

        Material mat = Material.matchMaterial(config.getString("type"));
        int amount = config.getInt("amount", 1);
        int damage = config.getInt("damage", 0);

        String name = config.getString("name", null);
        if(name != null) name = StringUtils.convertAmpersandToSelectionCharacter(name);

        ItemStack item = InventoryUtils.createItemWithNameAndLore(mat, amount, damage, name);

        if(config.contains("lore")) {
            List<String> lore = config.getStringList("lore");

            for(int index = 0; index < lore.size(); ++index) {
                lore.set(index, StringUtils.convertAmpersandToSelectionCharacter(lore.get(index)));
            }

            InventoryUtils.addLore(item, lore);
        }

        if(config.contains("enchantments")) {
            List<Config> enchantments = config.getConfigList("enchantments");

            if(item.getType() != Material.ENCHANTED_BOOK) {
                for (Config enchantment : enchantments) {
                    Enchantment ench = Enchantment.getByName(enchantment.getString("name").toUpperCase());
                    int level = enchantment.getInt("level", 1);

                    item.addUnsafeEnchantment(ench, level);
                }
            } else {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();

                for (Config enchantment : enchantments) {
                    Enchantment ench = Enchantment.getByName(enchantment.getString("name").toUpperCase());
                    int level = enchantment.getInt("level", 1);

                    meta.addStoredEnchant(ench, level, true);
                }
            }
        }

        //TODO attributes support for 1.9

        return item;
    }

    public static SoundEffect getSoundEffect(Config config, String path) {
        if (config.isString(path)) return new SoundEffect(config.getString(path));
        else if (config.isConfig(path)) return new SoundEffect(config.getConfigOrNull(path));
        else return null;
    }

    public static ParticleEffect getParticleEffect(Config config, String path) {
        if(config.isConfig(path)) return new ParticleEffect(config.getConfigOrNull(path));
        else return null;
    }
}
