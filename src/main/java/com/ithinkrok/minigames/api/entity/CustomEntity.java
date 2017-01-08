package com.ithinkrok.minigames.api.entity;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.Nameable;
import com.ithinkrok.minigames.api.event.map.MapEntityDeathEvent;
import com.ithinkrok.minigames.api.inventory.WeightedInventory;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.api.util.io.ListenerLoader;
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
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 24/12/16.
 */
public class CustomEntity implements Nameable, CustomListener {

    private static final double HEALTH_PER_HEART = 2;

    private final String name;

    private final EntityType type;

    private final Config config;

    private final boolean doDefaultDrops;
    private final Calculator expDrop;
    private final Map<WeightedInventory, Calculator> customDrops = new HashMap<>();
    private final List<CustomListener> allListeners = new ArrayList<>();
    private boolean loadedDrops = false;

    public CustomEntity(String name, Config config) {
        this.name = name;

        this.config = config;

        this.type = EntityType.fromName(config.getString("type").toUpperCase());

        Config dropConfig = config.getConfigOrEmpty("drops");
        doDefaultDrops = dropConfig.getBoolean("do_default", true);

        if (dropConfig.contains("exp")) {
            expDrop = new ExpressionCalculator(dropConfig.getString("exp"));
        } else {
            expDrop = null;
        }

        if (config.contains("listeners")) {
            configureListeners(config.getConfigOrEmpty("listeners"));
        }

        allListeners.add(this);
    }

    private void configureListeners(Config config) {
        for (String name : config.getKeys(false)) {
            Config listenerInfo = config.getConfigOrNull(name);
            try {
                CustomListener listener = ListenerLoader.loadListener(this, this, listenerInfo);

                allListeners.add(listener);
            } catch (Exception e) {
                System.out
                        .println("Failed while creating CustomEntity \"" + this.name + "\" listener for key: " + name);
                e.printStackTrace();
            }
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

    public Entity spawnCustomEntity(GameGroup gameGroup, Location location, Variables variables) {
        Entity entity;


        if(type == EntityType.LIGHTNING) {
            entity = location.getWorld().strikeLightning(location);
        } else {
            entity = location.getWorld().spawnEntity(location, type);
        }

        setupCustomEntity(gameGroup, variables, entity);

        return entity;
    }


    public Projectile launchCustomProjectile(GameGroup gameGroup, ProjectileSource source, Variables variables, Vector
            velocity) {

        if(!Projectile.class.isAssignableFrom(type.getEntityClass())) {
            throw new IllegalStateException("The entity type of CustomEntity " + name + " is not a Projectile");
        }

        Projectile projectile;

        if(velocity == null) {
            projectile = source.launchProjectile(type.getEntityClass().asSubclass(Projectile.class));
        } else {
            projectile = source.launchProjectile(type.getEntityClass().asSubclass(Projectile.class), velocity);
        }

        setupCustomEntity(gameGroup, variables, projectile);

        return projectile;
    }

    private void setupCustomEntity(GameGroup gameGroup, Variables variables, Entity entity) {
        gameGroup.getGame().setupCustomEntity(entity, name, variables);

        //Handle entities with ages
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

        //Make a skeleton a wither skeleton
        if (config.contains("wither")) {
            if (Math.floor(calculate(variables, config, "wither")) != 0) {
                ((Skeleton) entity).setSkeletonType(Skeleton.SkeletonType.WITHER);
            }
        }

        //Charge a creeper
        if(config.contains("charged")) {
            if(Math.floor(calculate(variables, config, "charged")) != 0) {
                ((Creeper) entity).setPowered(true);
            }
        }

        //Strike lightning at the new entity
        if(config.contains("strike_lightning")) {
            if(Math.floor(calculate(variables, config, "strike_lightning")) != 0) {
                entity.getWorld().strikeLightningEffect(entity.getLocation());
            }
        }

        //Give potion effects to the entity
        if (config.contains("effects")) {
            addEntityEffects(variables, (LivingEntity) entity);
        }

        //Set the max health and initial health of the entity
        if (config.contains("max_health")) {
            int maxHealth = (int) (calculate(variables, config, "max_health") * HEALTH_PER_HEART);

            ((Damageable) entity).setMaxHealth(maxHealth);
            ((Damageable) entity).setHealth(maxHealth);
        }

        //Show a name above the entity
        if (config.contains("name")) {
            entity.setCustomName(config.getString("name"));
            entity.setCustomNameVisible(true);
        }

        //Give the entity weapons and armor
        if (config.contains("equipment")) {
            addEntityEquipment(gameGroup, variables, (LivingEntity) entity);
        }
    }

    private double calculate(Variables variables, Config config, String path) {
        return calculate(variables, config, path, null);
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

    private void addEntityEquipment(GameGroup gameGroup, Variables variables, LivingEntity entity) {
        Config equipConfig = config.getConfigOrEmpty("equipment");
        EntityEquipment equipment = entity.getEquipment();

        ItemStack hand = equipmentItemStack("hand", equipConfig, gameGroup, variables);
        if (hand != null) equipment.setItemInHand(hand);

        ItemStack helmet = equipmentItemStack("helmet", equipConfig, gameGroup, variables);
        if (helmet != null) equipment.setHelmet(helmet);

        ItemStack chestplate = equipmentItemStack("chestplate", equipConfig, gameGroup, variables);
        if (chestplate != null) equipment.setChestplate(chestplate);

        ItemStack leggings = equipmentItemStack("leggings", equipConfig, gameGroup, variables);
        if (leggings != null) equipment.setLeggings(leggings);

        ItemStack boots = equipmentItemStack("boots", equipConfig, gameGroup, variables);
        if (boots != null) equipment.setBoots(boots);
    }

    private double calculate(Variables variables, Config config, String path, String def) {
        return new ExpressionCalculator(config.getString(path, def)).calculate(variables);
    }

    private ItemStack equipmentItemStack(String path, Config equipConfig, GameGroup gameGroup, Variables variables) {
        if (equipConfig.contains("custom_" + path)) {
            String hand = equipConfig.getString("custom_" + path);
            CustomItem handCustom = gameGroup.getCustomItem(hand);

            return handCustom.createWithVariables(gameGroup.getLanguageLookup(), variables);
        } else if (equipConfig.contains(path)) {
            return MinigamesConfigs.getItemStack(equipConfig, "hand");
        }

        return null;
    }

    public EntityType getType() {
        return type;
    }

    @CustomEventHandler(priority = CustomEventHandler.LOW)
    public void onMapEntityDeath(MapEntityDeathEvent event) {
        //TODO call listeners

        loadDrops(event.getGameGroup());

        if (!doDefaultDrops) {
            event.getDrops().clear();
        }

        Variables variables = EntityUtils.getCustomEntityVariables(event.getEntity());

        if (expDrop != null) {
            event.setDroppedExp((int) expDrop.calculate(variables));
        }

        for (Map.Entry<WeightedInventory, Calculator> entry : customDrops.entrySet()) {
            int amount = (int) entry.getValue().calculate(variables);

            if (amount <= 0) continue;

            event.getDrops().addAll(entry.getKey().generateStacks(amount, false, variables));
        }

    }

    public List<CustomListener> getListeners() {
        return allListeners;
    }

    private void loadDrops(GameGroup gameGroup) {
        if (loadedDrops) return;
        loadedDrops = true;

        Config dropConfig = config.getConfigOrEmpty("drops");

        for (Config inventoryConfig : dropConfig.getConfigList("inventories")) {
            WeightedInventory inventory = new WeightedInventory(gameGroup, inventoryConfig);

            Calculator count = new ExpressionCalculator(inventoryConfig.getString("count", "1"));

            customDrops.put(inventory, count);
        }
    }
}
