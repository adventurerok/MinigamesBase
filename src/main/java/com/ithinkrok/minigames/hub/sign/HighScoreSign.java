package com.ithinkrok.minigames.hub.sign;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.database.UserScore;
import com.ithinkrok.minigames.api.event.controller.ControllerKillGameGroupEvent;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.sign.InfoSign;
import com.ithinkrok.minigames.api.sign.SignController;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.event.CustomEventHandler;
import com.mojang.authlib.GameProfile;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_12_R1.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by paul on 20/02/16.
 */
public class HighScoreSign extends InfoSign {

    protected final String gameType;
    protected final int position;
    protected final String mode;
    protected final String[] format;


    public HighScoreSign(UserEditSignEvent event, SignController signController) {
        super(event, signController);

        gameType = event.getLine(1);
        position = Integer.parseInt(event.getLine(2));
        mode = event.getLine(3);

        format = defaultFormat();
    }

    @SuppressWarnings("unused")
    public HighScoreSign(GameGroup gameGroup, Config config, SignController signController) {
        super(gameGroup, config, signController);

        gameType = config.getString("game_type");
        position = config.getInt("position");
        mode = config.getString("mode");

        format = loadFormatFromConfig(config, "format", defaultFormat());
    }

    @Override
    public Config toConfig() {
        Config config = super.toConfig();

        config.set("game_type", gameType);
        config.set("position", position);
        config.set("mode", mode);

        saveFormatToConfig(config, "format", format);

        return config;
    }

    private boolean isAscending() {
        switch (mode) {
            case "time":
            case "lowint":
            case "lowfloat":
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void updateSign() {
        gameGroup.getDatabase().getHighScores(gameType, position, isAscending(), userScores -> {
            if(userScores.size() < position) return;

            gameGroup.doInFuture(task -> {
               updateSign(userScores.get(position - 1));
            });
        });
    }

    private String[] defaultFormat() {
        return new String[] {
          "#{position}", "{name}", "", "{score}"
        };
    }

    private void updateSign(UserScore score) {
        String scoreText;

        switch (mode) {
            case "float":
            case "lowfloat":
                scoreText = Double.toString(score.getValue());
                break;
            case "time":
            case "hightime":
                Duration duration = Duration.ofMillis((long) score.getValue());
                LocalTime time = LocalTime.MIDNIGHT.plus(duration);

                scoreText = DateTimeFormatter.ofPattern("HH:mm:ss:SSS").format(time);
                break;
            case "int":
            case "lowint":
            default:
                scoreText = Integer.toString((int) score.getValue());
        }

        Config config = new MemoryConfig();

        config.set("type", gameType);
        config.set("formatted_type", WordUtils.capitalizeFully(gameType.replace('_', ' ')));
        config.set("position", position);
        config.set("score", scoreText);
        config.set("name", score.getPlayerName());

        updateSignFromFormat(format, config);

        updateHead(score, location.getBlock().getRelative(0, -1, 0));
        updateHead(score, location.getBlock().getRelative(0, 1, 0));
    }

    private void updateHead(UserScore score, Block head) {
        if(head.getType() != Material.SKULL) return;

        Skull skull = (Skull) head.getState();

        //only update the head if the player has changed
        if(skull.getOwningPlayer() == null || !skull.getOwningPlayer().getUniqueId().equals(score.getPlayerUUID())) {
            OfflinePlayer offline = getOfflinePlayer(score.getPlayerUUID(), score.getPlayerName());

            skull.setOwningPlayer(offline);
            skull.update();
        }
    }


    private OfflinePlayer getOfflinePlayer(UUID uuid, String name) {
        //this method ensures the offline player returned has the correct name for the skin cache (which uses names wtf Mojang)

        OfflinePlayer bukkitPlayer = Bukkit.getOfflinePlayer(uuid);
        if(Objects.equals(bukkitPlayer.getName(), name)) {
            return bukkitPlayer;
        }

        try {
            Constructor<CraftOfflinePlayer> cons =
                    CraftOfflinePlayer.class.getDeclaredConstructor(CraftServer.class, GameProfile.class);
            cons.setAccessible(true);

            GameProfile profile = new GameProfile(uuid, name);
            //noinspection JavaReflectionInvocation
            return cons.newInstance(Bukkit.getServer(), profile);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            System.err.println("Failed to create our custom OfflinePlayer");
            e.printStackTrace();
            return bukkitPlayer;
        }
    }


    @Override
    public void onRightClick(User user) {

    }

    @CustomEventHandler
    public void onControllerGameGroupKilledEvent(ControllerKillGameGroupEvent event) {
        if(!(Objects.equals(event.getControllerGameGroup().getType(), gameType))) return;

        gameGroup.doInFuture(task -> {
            update();
        }, 100);
    }
}
