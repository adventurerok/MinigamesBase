package com.ithinkrok.minigames.api.item;

import com.ithinkrok.minigames.api.Nameable;
import com.ithinkrok.minigames.api.event.user.game.UserAbilityCooldownEvent;
import com.ithinkrok.minigames.api.event.user.inventory.UserItemHeldEvent;
import com.ithinkrok.minigames.api.event.user.world.UserAttackEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.item.attributes.ItemAttributeModifier;
import com.ithinkrok.minigames.api.item.attributes.ItemAttributes;
import com.ithinkrok.minigames.api.item.event.CustomItemLoreCalculateEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.io.ListenerLoader;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.util.math.Variables;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventExecutor;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by paul on 02/01/16.
 * <p>
 * An item with custom use or inventory click listeners
 */
public class CustomItem implements CustomListener, Nameable {

    private final List<CustomListener> rightClickActions = new ArrayList<>();
    private final List<CustomListener> leftClickActions = new ArrayList<>();
    private final List<CustomListener> timeoutActions = new ArrayList<>();
    private final List<CustomListener> attackActions = new ArrayList<>();
    private final List<CustomListener> allListeners = new ArrayList<>();

    private final String name;
    private final String displayNameLocale;
    private final Material itemMaterial;
    private final int durability;
    private final boolean unbreakable;
    private final boolean blockPlacingDisabled;

    private String rightClickCooldownFinishedLocale;
    private Calculator rightClickCooldown;
    private String timeoutAbility;
    private String timeoutDescriptionLocale;
    private String timeoutFinishedLocale;
    private Calculator timeoutCalculator;
    private String rightClickCooldownAbility;
    private final String descriptionLocale;

    private final boolean replaceOnUpgrade;

    private final CombatMode combatMode;

    private final List<EnchantmentEffect> enchantmentEffects = new ArrayList<>();

    private final List<AttribModifier> attributes = new ArrayList<>();

    public CustomItem(String name, Config config) {
        this.name = name;

        this.displayNameLocale = config.getString("display_name_locale", null);
        this.descriptionLocale = config.getString("description_locale", null);
        this.itemMaterial = Material.matchMaterial(config.getString("material"));
        this.durability = config.getInt("durability", 0);
        this.unbreakable = config.getBoolean("unbreakable", itemMaterial.getMaxDurability() != 0);
        this.replaceOnUpgrade = config.getBoolean("upgradable", false);
        this.combatMode = CombatMode.valueOf(config.getString("combat_mode", "INHERIT").toUpperCase());
        this.blockPlacingDisabled = config.getBoolean("block_placing_disabled", false);

        if (config.contains("right_cooldown")) configureCooldown(config.getConfigOrNull("right_cooldown"));
        if (config.contains("right_timeout")) configureTimeout(config.getConfigOrNull("right_timeout"));
        if( config.contains("enchantments")) configureEnchantments(config.getConfigOrNull("enchantments"));
        if (config.contains("listeners")) configureListeners(config.getConfigOrNull("listeners"));
        if(config.contains("attributes")) configureAttributes(config.getConfigList("attributes"));
    }


    private void configureAttributes(List<Config> configs) {
        attributes.addAll(configs.stream().map(AttribModifier::new).collect(Collectors.toList()));
    }


    public CombatMode getCombatMode() {
        return combatMode;
    }

    private void configureEnchantments(Config enchantments) {
        for(String enchantmentName : enchantments.getKeys(false)) {
            Enchantment enchantment = Enchantment.getByName(enchantmentName);
            Calculator calc = new ExpressionCalculator(enchantments.getString(enchantmentName));

            enchantmentEffects.add(new EnchantmentEffect(enchantment, calc));
        }
    }

    private void configureCooldown(Config config) {
        rightClickCooldown = new ExpressionCalculator(config.getString("timer"));
        rightClickCooldownAbility = config.getString("ability", UUID.randomUUID().toString());
        rightClickCooldownFinishedLocale = config.getString("finished_locale");
    }

    private void configureTimeout(Config config) {
        timeoutCalculator = new ExpressionCalculator(config.getString("timer"));
        timeoutAbility = config.getString("ability", UUID.randomUUID().toString());
        timeoutDescriptionLocale = config.getString("description_locale");
        timeoutFinishedLocale = config.getString("finished_locale");
    }

    private void configureListeners(Config config) {
        for (String name : config.getKeys(false)) {
            Config listenerInfo = config.getConfigOrNull(name);
            try {
                CustomListener listener = ListenerLoader.loadListener(this, this, listenerInfo);

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

    @CustomEventHandler
    public void onUserAttack(UserAttackEvent event) {
        CustomEventExecutor.executeEvent(event, attackActions);
    }

    @CustomEventHandler
    public void onAbilityCooldown(UserAbilityCooldownEvent event) {
        if(!event.getAbility().equals(timeoutAbility)) return;

        CustomEventExecutor.executeEvent(event, timeoutActions);
        startRightClickCooldown(event.getUser());
    }

    @CustomEventHandler
    public void onUserChangeItemHeld(UserItemHeldEvent event) {
        CustomEventExecutor.executeEvent(event, allListeners);
    }

    @CustomEventHandler
    public void onInteract(UserInteractEvent event) {
        //we handle these in onUserAttack
        if(event instanceof UserAttackEvent) return;

        switch(event.getInteractType()) {
            case PHYSICAL:
            case REPRESENTING:
                return;
        }

        if(event.getInteractType() == UserInteractEvent.InteractType.LEFT_CLICK) {
            CustomEventExecutor.executeEvent(event, leftClickActions);
            return;
        }

        if(isTimingOut(event.getUser())){
            event.getUser().showAboveHotbarLocale("timeouts.default.wait", event.getUser().getCooldownSeconds(timeoutAbility));
            return;
        }
        if(isCoolingDown(event.getUser())) {
            event.getUser().showAboveHotbarLocale("cooldowns.default.wait", event.getUser().getCooldownSeconds(rightClickCooldownAbility));
            return;
        }

        CustomEventExecutor.executeEvent(event, rightClickActions);
        if(!event.getStartCooldownAfterAction()) return;

        if(timeoutCalculator != null) {
            double timeout = calculateRightClickTimeout(event.getUser().getUserVariables());
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

        double cooldown = calculateRightClickCooldown(user.getUserVariables());
        user.startCoolDown(rightClickCooldownAbility, cooldown, rightClickCooldownFinishedLocale);
    }

    private boolean isTimingOut(User user) {
        return timeoutCalculator != null && user.isCoolingDown(timeoutAbility);
    }

    private boolean isCoolingDown(User user) {
        return rightClickCooldown != null && user.isCoolingDown(rightClickCooldownAbility);

    }

    public ItemStack createForUser(User user) {
        return createWithVariables(user.getLanguageLookup(), user.getUserVariables());
    }

    public boolean replaceOnUpgrade() {
        return replaceOnUpgrade;
    }

    public ItemStack createWithVariables(LanguageLookup languageLookup, Variables variables) {
        List<String> lore = new ArrayList<>();

        if (descriptionLocale != null) lore.add(languageLookup.getLocale(descriptionLocale));

        CustomItemLoreCalculateEvent event = new CustomItemLoreCalculateEvent(this, lore, languageLookup, variables);

        CustomEventExecutor.executeEvent(event, allListeners);

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
        if(displayNameLocale != null) itemDisplayName = languageLookup.getLocale(displayNameLocale);

        ItemStack item =
                InventoryUtils.createItemWithNameAndLore(itemMaterial, 1, durability, itemDisplayName, loreArray);

        if (enchantmentEffects != null) {
            for (EnchantmentEffect enchantmentEffect : enchantmentEffects) {
                int level = (int) enchantmentEffect.levelCalculator.calculate(variables);
                if (level <= 0) continue;

                item.addUnsafeEnchantment(enchantmentEffect.enchantment, level);
            }
        }

        if(attributes != null) {
            ItemAttributes temp = new ItemAttributes();
            for(AttribModifier attribModifier : attributes) {
                double amount = attribModifier.actualAmount.calculate(variables);
                ItemAttributeModifier toApply = attribModifier.base.cloneWithAmount(amount);
                temp.addModifier(toApply);
            }

            item = temp.apply(item);
        }

        //Make us unbreakable if we are
        if(unbreakable) {
            item = InventoryUtils.setUnbreakable(item, true);
        }

        if(blockPlacingDisabled) {
            item = InventoryUtils.disableBlockPlacing(item);
        }

        return InventoryUtils.addIdentifier(item, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormattedName() {
        return name;
    }

    private static class EnchantmentEffect {
        private final Enchantment enchantment;
        private final Calculator levelCalculator;

        public EnchantmentEffect(Enchantment enchantment, Calculator levelCalculator) {
            this.enchantment = enchantment;
            this.levelCalculator = levelCalculator;
        }
    }

    private static class AttribModifier {
        private ItemAttributeModifier base;
        private Calculator actualAmount;

        public AttribModifier(Config config) {
            base = new ItemAttributeModifier(config);
            actualAmount = new ExpressionCalculator(config.getString("amount"));
        }
    }

    public String getDisplayNameLocale() {
        return displayNameLocale;
    }
}
