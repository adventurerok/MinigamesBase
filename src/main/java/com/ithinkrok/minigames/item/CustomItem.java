package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.user.game.UserAbilityCooldownEvent;
import com.ithinkrok.minigames.event.user.world.UserAttackEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.item.event.CustomItemLoreCalculateEvent;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.util.EventExecutor;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.io.ListenerLoader;
import com.ithinkrok.minigames.util.math.Calculator;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.util.math.Variables;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 02/01/16.
 * <p>
 * An item with custom use or inventory click listeners
 */
public class CustomItem implements Identifiable, Listener {

    private static int customItemCount = 0;

    private int customItemId = customItemCount++;

    private List<Listener> rightClickActions = new ArrayList<>();
    private List<Listener> leftClickActions = new ArrayList<>();
    private List<Listener> timeoutActions = new ArrayList<>();
    private List<Listener> attackActions = new ArrayList<>();
    private List<Listener> allListeners = new ArrayList<>();

    private String name;
    private String itemDisplayLocale;
    private Material itemMaterial;
    private int durability;
    private boolean unbreakable;

    private String rightClickCooldownFinishedLocale;
    private Calculator rightClickCooldown;
    private String timeoutAbility;
    private String timeoutDescriptionLocale;
    private String timeoutFinishedLocale;
    private Calculator timeoutCalculator;
    private String rightClickCooldownAbility;
    private String descriptionLocale;

    private boolean replaceOnUpgrade;

    private List<EnchantmentEffect> enchantmentEffects = new ArrayList<>();

    public CustomItem(String name, ConfigurationSection config) {
        this.name = name;

        this.itemDisplayLocale = config.getString("display_name_locale", null);
        this.descriptionLocale = config.getString("description_locale", null);
        this.itemMaterial = Material.matchMaterial(config.getString("material"));
        this.durability = config.getInt("durability", 0);
        this.unbreakable = config.getBoolean("unbreakable", itemMaterial.getMaxDurability() != 0);
        this.replaceOnUpgrade = config.getBoolean("upgradable", false);

        if (config.contains("right_cooldown")) configureCooldown(config.getConfigurationSection("right_cooldown"));
        if (config.contains("right_timeout")) configureTimeout(config.getConfigurationSection("right_timeout"));
        if( config.contains("enchantments")) configureEnchantments(config.getConfigurationSection("enchantments"));
        if (config.contains("listeners")) configureListeners(config.getConfigurationSection("listeners"));
    }

    private void configureEnchantments(ConfigurationSection enchantments) {
        for(String enchantmentName : enchantments.getKeys(false)) {
            Enchantment enchantment = Enchantment.getByName(enchantmentName);
            Calculator calc = new ExpressionCalculator(enchantments.getString(enchantmentName));

            enchantmentEffects.add(new EnchantmentEffect(enchantment, calc));
        }
    }

    private void configureCooldown(ConfigurationSection config) {
        rightClickCooldown = new ExpressionCalculator(config.getString("timer"));
        rightClickCooldownAbility = config.getString("ability", UUID.randomUUID().toString());
        rightClickCooldownFinishedLocale = config.getString("finished_locale");
    }

    private void configureTimeout(ConfigurationSection config) {
        timeoutCalculator = new ExpressionCalculator(config.getString("timer"));
        timeoutAbility = config.getString("ability", UUID.randomUUID().toString());
        timeoutDescriptionLocale = config.getString("description_locale");
        timeoutFinishedLocale = config.getString("finished_locale");
    }

    private void configureListeners(ConfigurationSection config) {
        for (String name : config.getKeys(false)) {
            ConfigurationSection listenerInfo = config.getConfigurationSection(name);
            try {
                Listener listener = ListenerLoader.loadListener(this, this, listenerInfo);

                List<String> events = null;
                if (listenerInfo.contains("events")) events = listenerInfo.getStringList("events");

                if (events == null || events.contains("rightClick")) rightClickActions.add(listener);
                if (events == null || events.contains("leftClick")) leftClickActions.add(listener);
                if (events == null || events.contains("timeout")) timeoutActions.add(listener);
                if (events == null || events.contains("attack")) attackActions.add(listener);

                allListeners.add(listener);
            } catch (Exception e) {
                System.out.println("Failed while creating CustomItem \"" + this.name + "\" listener for key: " + name);
                e.printStackTrace();
            }
        }
    }

    @MinigamesEventHandler
    public void onUserAttack(UserAttackEvent event) {
        EventExecutor.executeEvent(event, attackActions);
    }

    @MinigamesEventHandler
    public void onAbilityCooldown(UserAbilityCooldownEvent event) {
        if(!event.getAbility().equals(timeoutAbility)) return;

        EventExecutor.executeEvent(event, timeoutActions);
        startRightClickCooldown(event.getUser());
    }

    @MinigamesEventHandler
    public void onInteract(UserInteractEvent event) {
        switch(event.getInteractType()) {
            case PHYSICAL:
            case REPRESENTING:
                return;
        }

        if(event.getInteractType() == UserInteractEvent.InteractType.LEFT_CLICK) {
            EventExecutor.executeEvent(event, leftClickActions);
            return;
        }

        if(isTimingOut(event.getUser())){
            event.getUser().sendLocale("timeouts.default.wait");
            return;
        }
        if(isCoolingDown(event.getUser())) {
            event.getUser().sendLocale("cooldowns.default.wait");
            return;
        }

        EventExecutor.executeEvent(event, rightClickActions);
        if(!event.getStartCooldownAfterAction()) return;

        if(timeoutCalculator != null) {
            int timeout = (int) timeoutCalculator.calculate(event.getUser().getUpgradeLevels());
            event.getUser().startCoolDown(timeoutAbility, timeout, timeoutFinishedLocale);
        } else {
            startRightClickCooldown(event.getUser());
        }
    }

    public double calculateRightClickCooldown(Variables variables) {
        if(rightClickCooldown == null) return 0;

        return rightClickCooldown.calculate(variables);
    }

    public double calculateRightClickTimeout(Variables variables) {
        if(timeoutCalculator == null) return 0;

        return timeoutCalculator.calculate(variables);
    }

    private void startRightClickCooldown(User user) {
        if(rightClickCooldown == null) return;

        int cooldown = (int) rightClickCooldown.calculate(user.getUpgradeLevels());
        user.startCoolDown(rightClickCooldownAbility, cooldown, rightClickCooldownFinishedLocale);
    }

    private boolean isTimingOut(User user) {
        return timeoutCalculator != null && user.isCoolingDown(timeoutAbility);
    }

    private boolean isCoolingDown(User user) {
        return rightClickCooldown != null && user.isCoolingDown(rightClickCooldownAbility);

    }

    public ItemStack createForUser(User user) {
        return createWithVariables(user.getLanguageLookup(), user.getUpgradeLevels());
    }

    public boolean replaceOnUpgrade() {
        return replaceOnUpgrade;
    }

    public ItemStack createWithVariables(LanguageLookup languageLookup, Variables variables) {
        List<String> lore = new ArrayList<>();

        if (descriptionLocale != null) lore.add(languageLookup.getLocale(descriptionLocale));

        CustomItemLoreCalculateEvent event = new CustomItemLoreCalculateEvent(this, lore, languageLookup, variables);

        EventExecutor.executeEvent(event, allListeners);

        if (rightClickCooldown != null) {
            double seconds = rightClickCooldown.calculate(variables);
            lore.add(languageLookup.getLocale("lore.cooldown", seconds));
        }

        if (timeoutCalculator != null) {
            double seconds = timeoutCalculator.calculate(variables);
            lore.add(languageLookup.getLocale(timeoutDescriptionLocale, seconds));
        }

        String[] loreArray = new String[lore.size()];
        lore.toArray(loreArray);

        String itemDisplayName = null;
        if(itemDisplayLocale != null) itemDisplayName = languageLookup.getLocale(itemDisplayLocale);

        ItemStack item =
                InventoryUtils.createItemWithNameAndLore(itemMaterial, 1, durability, itemDisplayName, loreArray);

        if (enchantmentEffects != null) {
            for (EnchantmentEffect enchantmentEffect : enchantmentEffects) {
                int level = (int) enchantmentEffect.levelCalculator.calculate(variables);
                if (level <= 0) continue;

                item.addUnsafeEnchantment(enchantmentEffect.enchantment, level);
            }
        }

        item.getItemMeta().spigot().setUnbreakable(unbreakable);

        return InventoryUtils.addIdentifier(item, customItemId);
    }

    public String getName() {
        return name;
    }

    @Override
    public int getIdentifier() {
        return customItemId;
    }

    private static class EnchantmentEffect {
        private Enchantment enchantment;
        private Calculator levelCalculator;

        public EnchantmentEffect(Enchantment enchantment, Calculator levelCalculator) {
            this.enchantment = enchantment;
            this.levelCalculator = levelCalculator;
        }
    }

}
