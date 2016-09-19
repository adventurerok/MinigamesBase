package com.ithinkrok.minigames.hub;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.state.UserAttackedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDeathEvent;
import com.ithinkrok.minigames.api.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.api.event.user.world.UserDropItemEvent;
import com.ithinkrok.minigames.api.event.user.world.UserPickupItemEvent;
import com.ithinkrok.minigames.api.sign.InfoSigns;
import com.ithinkrok.minigames.api.task.GameRunnable;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.minigames.hub.data.JumpPad;
import com.ithinkrok.minigames.hub.scoreboard.HubScoreboardHandler;
import com.ithinkrok.minigames.hub.sign.GameChooseSign;
import com.ithinkrok.minigames.hub.sign.HighScoreSign;
import com.ithinkrok.minigames.hub.sign.JoinLobbySign;
import com.ithinkrok.minigames.hub.task.JumpPadTask;
import com.ithinkrok.minigames.util.ItemGiver;
import com.ithinkrok.minigames.util.map.SignListener;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.ConfigSerializable;
import com.ithinkrok.util.config.ConfigUtils;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 20/02/16.
 */
public class HubListener extends SignListener {

    static {
        InfoSigns.registerSignType("%lobby_sign%", JoinLobbySign.class, JoinLobbySign::new);
        InfoSigns.registerSignType("%choose_sign%", GameChooseSign.class, GameChooseSign::new);
        InfoSigns.registerSignType("%high_score%", HighScoreSign.class, HighScoreSign::new);
    }


    private ItemGiver itemGiver;

    private final Map<Material, JumpPad> jumpPadMap = new EnumMap<>(Material.class);

    private double superPopperPower;
    private String superPopperVictimLocale;
    private String superPopperAttackerLocale;

    private SoundEffect superPopperVictimSound;
    private SoundEffect superPopperAttackerSound;

    private String welcomeTitleLocale;
    private String welcomeSubtitleLocale;

    private Config scoreboardConfig;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, ?> event) {
        super.onListenerLoaded(event);

        Config config = event.getConfigOrEmpty();

        if(config.contains("items")) {
            itemGiver = new ItemGiver(config.getConfigOrNull("items"));
        }

        if(config.contains("jump_pads")) {
            List<Config> jumpPadList = config.getConfigList("jump_pads");

            for(Config jumpConfig : jumpPadList) {
                JumpPad pad = new JumpPad(jumpConfig);

                jumpPadMap.put(pad.getMaterial(), pad);
            }
        }

        if(config.contains("super_popper")) {
            Config superPopperConfig = config.getConfigOrEmpty("super_popper");

            superPopperPower = superPopperConfig.getDouble("power");

            superPopperVictimLocale = superPopperConfig.getString("victim_locale");
            superPopperAttackerLocale = superPopperConfig.getString("attacker_locale");

            superPopperVictimSound = MinigamesConfigs.getSoundEffect(superPopperConfig, "victim_sound");
            superPopperAttackerSound = MinigamesConfigs.getSoundEffect(superPopperConfig, "attacker_sound");
        }

        if(config.contains("welcome")) {
            welcomeTitleLocale = config.getString("welcome.title_locale");
            welcomeSubtitleLocale = config.getString("welcome.subtitle_locale");
        }

        scoreboardConfig = config.getConfigOrNull("scoreboard");
    }

    @CustomEventHandler
    public void onUserJoin(UserJoinEvent event) {
        event.getUser().teleport(event.getUserGameGroup().getCurrentMap().getSpawn());

        event.getUser().setGameMode(GameMode.ADVENTURE);
        event.getUser().setHealth(20);
        event.getUser().setMaxHealth(20);
        event.getUser().setFoodLevel(20);

        if(itemGiver != null) {
            itemGiver.giveToUser(event.getUser());
        }

        if(!jumpPadMap.isEmpty()) {
            event.getUser().repeatInFuture(new JumpPadTask(event.getUser(), jumpPadMap), 2, 2);
        }

        if(welcomeTitleLocale != null) {
            LanguageLookup lookup = event.getUser().getLanguageLookup();
            String welcomeTitle = lookup.getLocale(welcomeTitleLocale);
            String welcomeSubtitle = lookup.getLocale(welcomeSubtitleLocale, event.getUser().getDisplayName());

            event.getUser().showTitle(welcomeTitle, welcomeSubtitle);
        }

        if(scoreboardConfig != null) {
            HubScoreboardHandler scoreboardHandler = new HubScoreboardHandler(scoreboardConfig);

            event.getUser().setScoreboardHandler(scoreboardHandler);

            //Update the scoreboard once per second
            event.getUser().repeatInFuture(task -> {
                event.getUser().updateScoreboard();
            }, 20, 20);
        }
    }



    @CustomEventHandler(ignoreCancelled = true)
    public void onUserAttackedByUser(UserAttackedEvent event) {
        if(!event.wasAttackedByUser()) return;

        if(event.getDamageCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            Vector velocity = event.getUser().getLocation().getDirection();
            velocity.setY(0.5f);
            velocity.multiply(superPopperPower);

            event.getUser().setVelocity(velocity);

            event.getUser().sendLocale(superPopperVictimLocale, event.getAttackerUser().getDisplayName());
            event.getAttackerUser().sendLocale(superPopperAttackerLocale, event.getUser().getDisplayName());

            if(superPopperVictimSound != null) {
                event.getUser().playSound(event.getUser().getLocation(), superPopperVictimSound);
            }

            if(superPopperAttackerSound != null) {
                event.getAttackerUser().playSound(event.getAttackerUser().getLocation(), superPopperAttackerSound);
            }

            event.setCancelled(true);
        }
    }

    @CustomEventHandler(priority = CustomEventHandler.HIGH)
    public void onUserDamaged(UserDamagedEvent event) {
        event.setCancelled(true);
    }

    @CustomEventHandler
    public void onUserHunger(UserFoodLevelChangeEvent event) {
        //Restore food level to max
        event.setFoodLevel(20);
        event.getUser().setSaturation(20);
    }

    @CustomEventHandler
    public void onUserDropItem(UserDropItemEvent event) {
        event.setCancelled(true);
    }

    @CustomEventHandler
    public void onUserPickupItem(UserPickupItemEvent event) {
        event.setCancelled(true);
    }
}
