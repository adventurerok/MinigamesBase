package com.ithinkrok.minigames.util.lobbygames;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Random;

/**
 * Created by paul on 23/01/16.
 */
public class WoolMinigame implements CustomListener {

    private final Random random = new Random();

    private User woolUser;

    @CustomEventHandler
    public void onUserJoin(UserJoinEvent event) {
        if (woolUser != null) return;

        woolUser = event.getUser();
        giveInitialWool();
    }

    public void giveInitialWool() {
        giveWoolHelmet();

        woolUser.getGameGroup().sendLocale("wool_head.initial", woolUser.getFormattedName());
        woolUser.sendLocale("wool_head.given");
    }

    private void giveWoolHelmet() {
        woolUser.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, DyeColor.PINK.getWoolData()));
    }

    @CustomEventHandler
    public void onUserQuit(UserQuitEvent event) {
        if (!Objects.equals(woolUser, event.getUser())) return;

        GameGroup gameGroup = event.getUserGameGroup();

        if (gameGroup.getUserCount() <= 1) {
            woolUser = null;
            return;
        }

        while (Objects.equals(woolUser, event.getUser())) {
            int index = random.nextInt(gameGroup.getUserCount());

            for (User next : gameGroup.getUsers()) {
                if (index == 0) {
                    woolUser = next;
                    break;
                }
                --index;
            }
        }

        giveInitialWool();
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        if (!event.hasEntity()) return;

        if (!Objects.equals(woolUser, event.getUser()) || !(event.getClickedEntity() instanceof Player)) return;

        User newWool = EntityUtils.getActualUser(woolUser, event.getClickedEntity());
        if (newWool == null) return;

        User oldWool = woolUser;
        woolUser = newWool;

        oldWool.getInventory().setHelmet(null);
        giveWoolHelmet();

        woolUser.getGameGroup()
                .sendLocale("wool_head.transfer", oldWool.getFormattedName(), woolUser.getFormattedName());
    }
}
