package com.ithinkrok.minigames.api.entity;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.Nameable;
import com.ithinkrok.minigames.api.event.map.MapEntityDeathEvent;
import com.ithinkrok.minigames.api.inventory.WeightedInventory;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.util.math.Variables;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 24/12/16.
 */
public class CustomEntity implements Nameable, CustomListener {

    private final String name;

    private final EntityType type;

    private final Config config;

    private final boolean doDefaultDrops;
    private final Calculator expDrop;
    private final Map<WeightedInventory, Calculator> customDrops = new HashMap<>();

    private boolean loadedDrops = false;

    public CustomEntity(String name, Config config) {
        this.name = name;

        this.config = config;

        this.type = EntityType.fromName(config.getString("type").toUpperCase());

        Config dropConfig = config.getConfigOrEmpty("drops");
        doDefaultDrops = dropConfig.getBoolean("do_default", true);

        if(dropConfig.contains("exp")) {
            expDrop = new ExpressionCalculator(dropConfig.getString("exp"));
        } else {
            expDrop = null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormattedName() {
        return name;
    }

    private double calculate(Variables variables, Config config, String path) {
        return calculate(variables, config, path, null);
    }

    private double calculate(Variables variables, Config config, String path, String def) {
        return new ExpressionCalculator(config.getString(path, def)).calculate(variables);
    }

    public Entity spawnEntity(GameGroup gameGroup, Location location, Variables variables) {
        Entity entity = location.getWorld().spawnEntity(location, type);

        gameGroup.getGame().setupCustomEntity(entity, name, variables);

        if (config.contains("age")) {
            ((Ageable) entity).setAge((int) calculate(variables, config, "age"));
        }

        if (config.contains("baby")) {
            if (entity instanceof Ageable) {
                if (config.getBoolean("baby")) {
                    ((Ageable) entity).setBaby();
                } else {
                    ((Ageable) entity).setAdult();
                }
            } else if (entity instanceof Zombie) {
                ((Zombie) entity).setBaby(config.getBoolean("baby"));
            }
        }

        if (config.contains("effects")) {
            addEntityEffects(variables, (LivingEntity) entity);
        }

        if (config.contains("max_health")) {
            int maxHealth = (int) calculate(variables, config, "max_health");

            ((Damageable) entity).setMaxHealth(maxHealth);
            ((Damageable) entity).setHealth(maxHealth);
        }

        if (config.contains("name")) {
            entity.setCustomName(config.getString("name"));
        }

        if (config.contains("equipment")) {
            addEntityEquipment(gameGroup, variables, (LivingEntity) entity);
        }

        return entity;
    }

    private void addEntityEquipment(GameGroup gameGroup, Variables variables, LivingEntity entity) {
        Config equipConfig = config.getConfigOrEmpty("equipment");

        if (equipConfig.contains("hand")) {
            String hand = equipConfig.getString("hand");
            CustomItem handCustom = gameGroup.getCustomItem(hand);

            EntityEquipment equipment = entity.getEquipment();
            ItemStack handItem = handCustom.createWithVariables(gameGroup.getLanguageLookup(), variables);
            equipment.setItemInHand(handItem);
        }
    }

    private void addEntityEffects(Variables variables, LivingEntity entity) {
        for (Config effectConfig : config.getConfigList("effects")) {
            PotionEffectType potionEffectType = PotionEffectType.getByName(effectConfig.getString("name"));
            int amp = 0;

            if (effectConfig.contains("level")) {
                amp = (int) calculate(variables, effectConfig, "level") - 1;
                if (amp < 0) continue;
            }

            PotionEffect effect = new PotionEffect(potionEffectType, Integer.MAX_VALUE, amp);

            entity.addPotionEffect(effect);
        }
    }

    public EntityType getType() {
        return type;
    }

    @CustomEventHandler
    public void onMapEntityDeath(MapEntityDeathEvent event) {
        //TODO call listeners

        loadDrops(event.getGameGroup());

        if(!doDefaultDrops) {
            event.getDrops().clear();
        }

        Variables variables = EntityUtils.getCustomEntityVariables(event.getEntity());

        if(expDrop != null) {
            event.setDroppedExp((int) expDrop.calculate(variables));
        }

        for (Map.Entry<WeightedInventory, Calculator> entry : customDrops.entrySet()) {
            int amount = (int) entry.getValue().calculate(variables);

            if(amount <= 0) continue;

            event.getDrops().addAll(entry.getKey().generateStacks(amount, false, variables));
        }

    }

    private void loadDrops(GameGroup gameGroup) {
        if(loadedDrops) return;
        loadedDrops = true;

        Config dropConfig = config.getConfigOrEmpty("drops");

        for(Config inventoryConfig : dropConfig.getConfigList("inventories")) {
            WeightedInventory inventory = new WeightedInventory(gameGroup, inventoryConfig);

            Calculator count = new ExpressionCalculator(inventoryConfig.getString("count", "1"));

            customDrops.put(inventory, count);
        }
    }
}
