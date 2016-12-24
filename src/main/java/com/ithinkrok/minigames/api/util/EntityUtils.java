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

    public static String getCustomEntityName(Entity entity) {
        List<MetadataValue> customNames = entity.getMetadata("custom_name");

        if(customNames == null || customNames.isEmpty()) return null;

        return customNames.get(0).asString();
    }

    public static Sound getDeathSound(EntityType entityType) {
        switch(entityType) {
            case BAT:
                return NamedSounds.fromName("ENTITY_BAT_DEATH");
            case BLAZE:
                return NamedSounds.fromName("ENTITY_BLAZE_DEATH");
            case OCELOT:
                return NamedSounds.fromName("ENTITY_CAT_DEATH");
            case CHICKEN:
                return NamedSounds.fromName("ENTITY_CHICKEN_DEATH");
            case COW:
                return NamedSounds.fromName("ENTITY_COW_DEATH");
            case CREEPER:
                return NamedSounds.fromName("ENTITY_CREEPER_DEATH");
            case ENDER_DRAGON:
                return NamedSounds.fromName("ENTITY_ENDERDRAGON_DEATH");
            case ENDERMAN:
                return NamedSounds.fromName("ENTITY_ENDERMEN_DEATH");
            case GHAST:
                return NamedSounds.fromName("ENTITY_GHAST_DEATH");
            case IRON_GOLEM:
                return NamedSounds.fromName("ENTITY_IRONGOLEM_DEATH");
            case MAGMA_CUBE:
                return NamedSounds.fromName("ENTITY_MAGMACUBE_DEATH");
            case PIG:
                return NamedSounds.fromName("ENTITY_PIG_DEATH");
            case SHEEP:
                return NamedSounds.fromName("ENTITY_SHEEP_DEATH");
            case SILVERFISH:
                return NamedSounds.fromName("ENTITY_SILVERFISH_DEATH");
            case SKELETON:
                return NamedSounds.fromName("ENTITY_SKELETON_DEATH");
            case SLIME:
                return NamedSounds.fromName("ENTITY_SLIME_DEATH");
            case SPIDER:
                return NamedSounds.fromName("ENTITY_SPIDER_DEATH");
            case WITHER:
                return NamedSounds.fromName("ENTITY_WITHER_DEATH");
            case WOLF:
                return NamedSounds.fromName("ENTITY_WOLF_DEATH");
            case ZOMBIE:
                return NamedSounds.fromName("ENTITY_ZOMBIE_DEATH");
            case PIG_ZOMBIE:
                return NamedSounds.fromName("ENTITY_ZOMBIE_PIG_DEATH");
            case HORSE:
                return NamedSounds.fromName("ENTITY_HORSE_DEATH");
            case VILLAGER:
                return NamedSounds.fromName("ENTITY_VILLAGER_DEATH");
            default:
                return NamedSounds.fromName("ENTITY_PLAYER_DEATH");
        }
    }

}
