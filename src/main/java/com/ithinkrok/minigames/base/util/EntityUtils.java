package com.ithinkrok.minigames.base.util;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.team.Team;
import com.ithinkrok.minigames.base.team.TeamUserResolver;
import com.ithinkrok.minigames.base.user.UserResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
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

}
