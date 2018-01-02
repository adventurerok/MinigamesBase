package com.ithinkrok.minigames.api.util;

import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.StringUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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

        return itemStackFromConfig(config);
    }

    public static List<ItemStack> getItemStackList(Config config, String path) {
        List<Object> list = config.getList(path, Object.class);

        List<ItemStack> result = new ArrayList<>();

        for(Object itemObject : list) {
            ItemStack item;

            if(itemObject instanceof String) {
                item = InventoryUtils.parseItem((String) itemObject);
            } else if(itemObject instanceof Config) {
                item = itemStackFromConfig((Config) itemObject);
            } else continue;

            if(item != null) {
                result.add(item);
            }
        }

        return result;
    }

    private static ItemStack itemStackFromConfig(Config config) {
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

                item.setItemMeta(meta);
            }
        }

        if(config.contains("potion")) {
            if(mat != Material.POTION) throw new RuntimeException("potion data can only be added to potions!");

            Config potionConfig = config.getConfigOrEmpty("potion");

            PotionMeta meta = (PotionMeta) item.getItemMeta();

            PotionType type = PotionType.valueOf(potionConfig.getString("type", "WATER").toUpperCase());
            boolean extended = config.getBoolean("extended", false);
            boolean upgraded = config.getBoolean("upgraded", false);

            PotionData potionData = new PotionData(type, extended, upgraded);
            meta.setBasePotionData(potionData);


            if(potionConfig.contains("effects")) {
                Config effectsConfig = potionConfig.getConfigOrNull("effects");
                for(String effectName : effectsConfig.getKeys(false)) {
                    PotionEffectType effectType = PotionEffectType.getByName(effectName);

                    Config effectConfig = effectsConfig.getConfigOrEmpty(effectName);

                    int duration = (int) (effectConfig.getDouble("duration", 1.0) * 20);

                    int amp = effectConfig.getInt("level") - 1;

                    PotionEffect effect = new PotionEffect(effectType, duration, amp);

                    meta.addCustomEffect(effect, true);
                }
            }

            item.setItemMeta(meta);
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

    public static MapPoint getMapPoint(Config config, String path) {
        if (config.isString(path)) {
            return new MapPoint(config.getString(path));
        } else if (config.isConfig(path)) {
            return new MapPoint(config.getConfigOrNull(path));
        } else throw new IllegalArgumentException("There is no MapPoint at location " + path +
                                                  " in config " + config.toString());
    }

    public static List<MapPoint> getMapPointList(Config config, String path) {
        List<Object> points = config.getList(path, Object.class);
        List<MapPoint> results = new ArrayList<>();

        for (Object pointToLoad : points) {
            MapPoint point = null;

            if(pointToLoad instanceof String) {
                point = new MapPoint((String) pointToLoad);
            } else if(pointToLoad instanceof Config) {
                point = new MapPoint((Config) pointToLoad);
            } else throw new RuntimeException("Invalid MapPoint type: " + pointToLoad.getClass());

            results.add(point);
        }

        return results;
    }
}
