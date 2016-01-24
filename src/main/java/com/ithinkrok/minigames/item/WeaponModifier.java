package com.ithinkrok.minigames.item;

import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.user.world.UserAttackEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.item.event.CustomItemLoreCalculateEvent;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.util.math.Calculator;
import com.ithinkrok.minigames.util.math.ExpressionCalculator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 03/01/16.
 */
public class WeaponModifier implements Listener {

    private static final double HEALTH_PER_HEART = 2;
    private static final double TICKS_PER_SECOND = 20;

    /**
     * Calculates damage in hearts
     */
    private Calculator damageCalculator;

    private Calculator fireCalculator;

    private List<EffectModifier> enemyEffects = new ArrayList<>();
    private List<EffectModifier> selfEffects = new ArrayList<>();

    @MinigamesEventHandler
    public void onListenerEnable(ListenerLoadedEvent event) {
        if (!event.hasConfig()) throw new RuntimeException("A WeaponModifier requires a config");

        load(event.getConfig());
    }

    private void load(ConfigurationSection config) {
        if (config.contains("damage")) damageCalculator = new ExpressionCalculator(config.getString("damage"));
        if (config.contains("fire")) fireCalculator = new ExpressionCalculator(config.getString("fire"));

        if (config.contains("effects")) {
            configureEffects(config.getConfigurationSection("effects"), enemyEffects);
        }

        if (config.contains("self_effects")) {
            configureEffects(config.getConfigurationSection("self_effects"), selfEffects);
        }
    }

    private void configureEffects(ConfigurationSection effects, List<EffectModifier> list) {
        for (String effectName : effects.getKeys(false)) {
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            list.add(new EffectModifier(effectType, effects.getConfigurationSection(effectName)));
        }
    }

    @MinigamesEventHandler
    public void onLoreItemsCalculate(CustomItemLoreCalculateEvent event) {
        LanguageLookup lang = event.getLanguageLookup();
        List<String> lore = event.getLore();

        if (damageCalculator != null)
            lore.add(lang.getLocale("weapon_modifier.damage", damageCalculator.calculate(event.getVariables())));
        if (fireCalculator != null)
            lore.add(lang.getLocale("weapon_modifier.fire", fireCalculator.calculate(event.getVariables())));

        for (EffectModifier modifier : enemyEffects) {
            if(!modifier.showInLore.calculateBoolean(event.getVariables())) continue;

            double duration = ((int) modifier.durationCalculator.calculate(event.getVariables()) * TICKS_PER_SECOND) /
                    TICKS_PER_SECOND;
            int level = (int) modifier.levelCalculator.calculate(event.getVariables());
            if (duration < 0.05d || level < 1) continue;

            String langName = "weapon_modifier." + modifier.effectType.getName().toLowerCase() + "." + level;
            lore.add(lang.getLocale(langName, duration));
        }
    }

    @SuppressWarnings("unchecked")
    @MinigamesEventHandler
    public void onUserAttack(UserAttackEvent attack) {
        if (attack.getInteractType() == UserInteractEvent.InteractType.REPRESENTING) return;
        if (attack.getDamageCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        if (damageCalculator != null) {
            attack.setDamage(damageCalculator.calculate(attack.getUser().getUpgradeLevels()) * HEALTH_PER_HEART);
        }

        if (fireCalculator != null) {
            int fireTicks = (int) (fireCalculator.calculate(attack.getUser().getUpgradeLevels()) * TICKS_PER_SECOND);

            if (fireTicks > 0) {
                if (attack.isAttackingUser()) attack.getTargetUser().setFireTicks(attack.getUser(), fireTicks);
                else attack.getClickedEntity().setFireTicks(fireTicks);
            }
        }

        for (EffectModifier effectModifier : enemyEffects) {
            effectModifier.modifyAttack(attack, false);
        }

        for(EffectModifier effectModifier : selfEffects) {
            effectModifier.modifyAttack(attack, true);
        }
    }

    private class EffectModifier {
        private PotionEffectType effectType;
        private Calculator durationCalculator;
        private Calculator levelCalculator;
        private Calculator showInLore;

        public EffectModifier(PotionEffectType effectType, ConfigurationSection config) {
            this.effectType = effectType;
            durationCalculator = new ExpressionCalculator(config.getString("duration"));

            if (!config.contains("level")) levelCalculator = new ExpressionCalculator("1");
            else levelCalculator = new ExpressionCalculator(config.getString("level"));

            showInLore = new ExpressionCalculator(config.getString("show_in_lore", "true"));
        }


        @SuppressWarnings("unchecked")
        public void modifyAttack(UserAttackEvent attack, boolean addToAttacker) {
            int duration = (int) (durationCalculator.calculate(attack.getUser().getUpgradeLevels()) * TICKS_PER_SECOND);
            int amp = (int) (levelCalculator.calculate(attack.getUser().getUpgradeLevels()) - 1);

            if (duration <= 0 || amp < 0) return;

            if(addToAttacker) {
                attack.getUser().addPotionEffect(new PotionEffect(effectType, duration, amp));
            } else if (attack.isAttackingUser() && effectType == PotionEffectType.WITHER) {
                attack.getTargetUser().setWitherTicks(attack.getUser(), duration, amp);
            } else {
                ((LivingEntity) attack.getClickedEntity()).addPotionEffect(new PotionEffect(effectType, duration, amp));
            }
        }
    }

}
