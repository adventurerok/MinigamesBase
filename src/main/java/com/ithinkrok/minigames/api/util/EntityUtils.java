package com.ithinkrok.minigames.api.util;

import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamUserResolver;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.user.UserResolver;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 03/01/16.
 */
public class EntityUtils {

    /**
     *
     * Resolves the User being represented by the entity. E.g. an arrow would be representing the User that shot the
     * arrow, or a Wolf would be representing it's owner.
     *
     * @param resolver The resolver to resolve UUIDs to Users
     * @param entity The entity to resolve the User from
     * @return The User that is represented by the entity, or null if there is none
     */
    public static User getRepresentingUser(UserResolver resolver, Entity entity) {
        User actual = getActualUser(resolver, entity);
        if(actual != null) return actual;

        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;

            if (projectile.getShooter() instanceof Player) {
                return getUserFromPlayer(resolver, (Player) projectile.getShooter());
            }
        }

        if(entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if(tameable.getOwner() != null && tameable.getOwner() instanceof Player) {
                return getUserFromPlayer(resolver, (Player) tameable.getOwner());
            }
        }

        List<MetadataValue> values = entity.getMetadata("rep");
        if(values == null || values.isEmpty()) return null;

        UUID uuid = (UUID) values.get(0).value();
        return resolver.getUser(uuid);
    }



    public static User getActualUser(UserResolver resolver, Entity entity) {
        if (entity instanceof Player) return getUserFromPlayer(resolver, (Player) entity);

        List<MetadataValue> values = entity.getMetadata("actual");
        if(values == null || values.isEmpty()) return null;

        UUID uuid = (UUID) values.get(0).value();
        return resolver.getUser(uuid);
    }

    public static Team getRepresentingTeam(TeamUserResolver resolver, Entity entity) {
        User user = getRepresentingUser(resolver, entity);
        if(user != null) return user.getTeam();

        if(!entity.hasMetadata("team")) return null;

        return resolver.getTeam(entity.getMetadata("team").get(0).asString());
    }

    private static User getUserFromPlayer(UserResolver resolver, Player player) {
        return resolver.getUser(player.getUniqueId());
    }

    public static Sound getDeathSound(EntityType entityType) {
        switch(entityType) {
            case BAT:
                return Sound.ENTITY_BAT_DEATH;
            case BLAZE:
                return Sound.ENTITY_BLAZE_DEATH;
            case OCELOT:
                return Sound.ENTITY_CAT_DEATH;
            case CHICKEN:
                return Sound.ENTITY_CHICKEN_DEATH;
            case COW:
                return Sound.ENTITY_COW_DEATH;
            case CREEPER:
                return Sound.ENTITY_CREEPER_DEATH;
            case ENDER_DRAGON:
                return Sound.ENTITY_ENDERDRAGON_DEATH;
            case ENDERMAN:
                return Sound.ENTITY_ENDERMEN_DEATH;
            case GHAST:
                return Sound.ENTITY_GHAST_DEATH;
            case IRON_GOLEM:
                return Sound.ENTITY_IRONGOLEM_DEATH;
            case MAGMA_CUBE:
                return Sound.ENTITY_MAGMACUBE_DEATH;
            case PIG:
                return Sound.ENTITY_PIG_DEATH;
            case SHEEP:
                return Sound.ENTITY_SHEEP_DEATH;
            case SILVERFISH:
                return Sound.ENTITY_SILVERFISH_DEATH;
            case SKELETON:
                return Sound.ENTITY_SKELETON_DEATH;
            case SLIME:
                return Sound.ENTITY_SLIME_DEATH;
            case SPIDER:
                return Sound.ENTITY_SPIDER_DEATH;
            case WITHER:
                return Sound.ENTITY_WITHER_DEATH;
            case WOLF:
                return Sound.ENTITY_WOLF_DEATH;
            case ZOMBIE:
                return Sound.ENTITY_ZOMBIE_DEATH;
            case PIG_ZOMBIE:
                return Sound.ENTITY_ZOMBIE_PIG_DEATH;
            case HORSE:
                return Sound.ENTITY_HORSE_DEATH;
            case VILLAGER:
                return Sound.ENTITY_VILLAGER_DEATH;
            default:
                return Sound.ENTITY_PLAYER_DEATH;
        }
    }

}
