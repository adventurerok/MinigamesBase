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
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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

        skull.setOwner(score.getPlayerName());

        skull.update();
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
