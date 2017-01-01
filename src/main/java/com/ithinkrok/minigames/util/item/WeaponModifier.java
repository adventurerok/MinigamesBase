package com.ithinkrok.minigames.util.item;

import com.ithinkrok.minigames.api.event.DamageEvent;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.map.MapEntityAttackedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserAttackEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.item.event.CustomItemLoreCalculateEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.util.math.*;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 03/01/16.
 */
public class WeaponModifier implements CustomListener {

    private static final double HEALTH_PER_HEART = 2;
    private static final double TICKS_PER_SECOND = 20;
    private final List<EffectModifier> enemyEffects = new ArrayList<>();
    private final List<EffectModifier> selfEffects = new ArrayList<>();
    /**
     * Calculates damage in hearts
     */
    private Calculator damageCalculator;
    private Calculator fireCalculator;

    @CustomEventHandler
    public void onListenerEnable(ListenerLoadedEvent<?, ?> event) {
        if (!event.hasConfig()) throw new RuntimeException("A WeaponModifier requires a config");

        load(event.getConfig());
    }

    private void load(Config config) {
        if (config.contains("damage")) damageCalculator = new ExpressionCalculator(config.getString("damage"));
        if (config.contains("fire")) fireCalculator = new ExpressionCalculator(config.getString("fire"));

        if (config.contains("effects")) {
            configureEffects(config.getConfigOrNull("effects"), enemyEffects);
        }

        if (config.contains("self_effects")) {
            configureEffects(config.getConfigOrNull("self_effects"), selfEffects);
        }
    }

    private void configureEffects(Config effects, List<EffectModifier> list) {
        for (String effectName : effects.getKeys(false)) {
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            list.add(new EffectModifier(effectType, effects.getConfigOrNull(effectName)));
        }
    }

    @CustomEventHandler
    public void onLoreItemsCalculate(CustomItemLoreCalculateEvent event) {
        LanguageLookup lang = event.getLanguageLookup();
        List<String> lore = event.getLore();

        if (damageCalculator != null)
            lore.add(lang.getLocale("weapon_modifier.damage", damageCalculator.calculate(event.getVariables())));
        if (fireCalculator != null)
            lore.add(lang.getLocale("weapon_modifier.fire", fireCalculator.calculate(event.getVariables())));

        for (EffectModifier modifier : enemyEffects) {
            if (!modifier.showInLore.calculateBoolean(event.getVariables())) continue;

            double duration = ((int) modifier.durationCalculator.calculate(event.getVariables()) * TICKS_PER_SECOND) /
                    TICKS_PER_SECOND;
            int level = (int) modifier.levelCalculator.calculate(event.getVariables());
            if (duration < 0.05d || level < 1) continue;

            String langName = "weapon_modifier." + modifier.effectType.getName().toLowerCase() + "." + level;
            lore.add(lang.getLocale(langName, duration));
        }
    }

    @CustomEventHandler
    public void onUserAttack(UserAttackEvent attack) {
        if (attack.getInteractType() == UserInteractEvent.InteractType.REPRESENTING) return;
        if (attack.getDamageCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        attack(attack, attack.getUser().getEntity(), attack.getUser(), attack
                       .getClickedEntity(), attack.getTargetUser(), attack.getUser().getUserVariables());
    }

    @CustomEventHandler
    public void onMapEntityAttacked(MapEntityAttackedEvent event) {
        //Dealt with in onUserAttack
        if(event.getAttackerUser() != null) return;

        User targetUser = EntityUtils.getActualUser(event.getGameGroup(), event.getEntity());
        Variables variables = EntityUtils.getCustomEntityVariables(event.getAttacker());

        attack(event, event.getAttacker(), event.getAttackerUser(), event.getEntity(), targetUser, variables);
    }

    private void attack(DamageEvent attack, Entity attacker, User attackUser, Entity target, User targetUser, Variables variables) {
        if (damageCalculator != null) {
            attack.setDamage(damageCalculator.calculate(variables) * HEALTH_PER_HEART);
        }

        if (fireCalculator != null) {
            int fireTicks = (int) (fireCalculator.calculate(variables) * TICKS_PER_SECOND);

            if (fireTicks > 0) {
                if (targetUser != null) targetUser.setFireTicks(attackUser, fireTicks);
                else target.setFireTicks(fireTicks);
            }
        }

        for (EffectModifier effectModifier : enemyEffects) {
            effectModifier.modifyAttack(attacker, attackUser, target, targetUser, variables, false);
        }

        for (EffectModifier effectModifier : selfEffects) {
            effectModifier.modifyAttack(attacker, attackUser, target, targetUser, variables, true);
        }
    }

    private static class EffectModifier {
        private final PotionEffectType effectType;
        private final Calculator durationCalculator;
        private final Calculator levelCalculator;
        private final Calculator showInLore;

        public EffectModifier(PotionEffectType effectType, Config config) {
            this.effectType = effectType;
            durationCalculator = new ExpressionCalculator(config.getString("duration"));

            if (!config.contains("level")) levelCalculator = new ExpressionCalculator("1");
            else levelCalculator = new ExpressionCalculator(config.getString("level"));

            showInLore = new ExpressionCalculator(config.getString("show_in_lore", "true"));
        }


        @SuppressWarnings("unchecked")
        public void modifyAttack(Entity attacker, User attackerUser, Entity target, User targetUser, Variables variables, boolean addToAttacker) {
            int duration = (int) (durationCalculator.calculate(variables) * TICKS_PER_SECOND);
            int amp = (int) (levelCalculator.calculate(variables) - 1);

            if (duration <= 0 || amp < 0) return;

            if (addToAttacker) {
                if(attacker instanceof LivingEntity) {
                    ((LivingEntity)attacker).addPotionEffect(new PotionEffect(effectType, duration, amp));
                }
            } else if (targetUser != null && effectType == PotionEffectType.WITHER) {
                targetUser.setWitherTicks(attackerUser, duration, amp);
            } else if(target instanceof LivingEntity){
                ((LivingEntity) target).addPotionEffect(new PotionEffect(effectType, duration, amp));
            }
        }
    }

}
