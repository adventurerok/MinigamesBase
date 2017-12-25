package com.ithinkrok.minigames.util.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Created by paul on 15/01/16.
 */
public class PlayerCompass implements CustomListener {

    private String nameLocale, locatingLocale, noPlayerLocale, orientedLocale;
    private PlayerCompassTarget target;
    private int locatingTime;

    private SoundEffect locateSound;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfigOrEmpty();

        nameLocale = config.getString("display_name_locale", "player_compass.name");
        locatingLocale = config.getString("locating_locale", "player_compass.locating");
        noPlayerLocale = config.getString("no_player_locale", "player_compass.no_player");
        orientedLocale = config.getString("oriented_locale", "player_compass.oriented");

        locateSound = MinigamesConfigs.getSoundEffect(config, "locate_sound");

        target = PlayerCompassTarget.valueOf(config.getString("target", "enemies").toUpperCase());

        locatingTime = (int) (config.getDouble("locating_time", 3) * 20);
    }

    @CustomEventHandler
    public void onInteract(UserInteractEvent event) {
        InventoryUtils.setItemName(event.getItem(), event.getUser().getLanguageLookup().getLocale(locatingLocale));

        event.getUser().doInFuture(task -> {
            Location closest = null;
            double minDist = 9999999999999d;
            String closestName = null;

            for (User user : event.getGameGroup().getUsers()) {
                if (!user.isInGame()) continue;

                boolean sameTeam = Objects.equals(event.getUser().getTeamIdentifier(), user.getTeamIdentifier());

                if ((target == PlayerCompassTarget.ENEMIES && sameTeam) ||
                        (target == PlayerCompassTarget.ALLIES && !sameTeam)) {
                    continue;
                }

                double dist = event.getUser().getLocation().distanceSquared(user.getLocation());

                if (dist > minDist) continue;
                minDist = dist;
                closest = user.getLocation();
                closestName = user.getFormattedName();
            }

            LanguageLookup lookup = event.getUser().getLanguageLookup();

            if (closest != null) event.getUser().setCompassTarget(closest);
            else closestName = lookup.getLocale(noPlayerLocale);

            ItemStack item = event.getItem().clone();

            InventoryUtils.setItemNameAndLore(item, lookup.getLocale(nameLocale),
                    lookup.getLocale(orientedLocale, closestName));

            InventoryUtils.replaceItem(event.getUser().getInventory(), item);

            event.getUser().showAboveHotbarLocale(orientedLocale, closestName);

            if(locateSound != null) {
                locateSound.playToAll(event.getUser().getGameGroup(), event.getUser().getLocation());
            }
        }, locatingTime);

        event.setStartCooldownAfterAction(true);
    }


    enum PlayerCompassTarget {
        ENEMIES,
        ALLIES,
        //This is used. These are selected from a string in a config. Lets the player compass target everyone in game.
        @SuppressWarnings("unused")ALL
    }
}
