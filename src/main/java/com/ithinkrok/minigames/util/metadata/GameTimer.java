package com.ithinkrok.minigames.util.metadata;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.metadata.Metadata;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by paul on 27/02/16.
 */
public class GameTimer extends Metadata {


    private Instant timeStarted = Instant.now();


    public Instant getTimeStarted() {
        return timeStarted;
    }

    public Duration getGameLength() {
        return Duration.between(timeStarted, Instant.now());
    }

    public String getFormattedGameLength() {
        long durationSeconds = getGameLength().getSeconds();

        long seconds = durationSeconds % 60;
        long minutes = (durationSeconds / 60) % 60;
        long hours = durationSeconds / 3600;

        if(hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    public void reset() {
        timeStarted = Instant.now();
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return false;
    }

    public static GameTimer getOrCreate(GameGroup gameGroup) {
        GameTimer metadata = gameGroup.getMetadata(GameTimer.class);

        if(metadata == null) {
            metadata = new GameTimer();

            gameGroup.setMetadata(metadata);
        }

        return metadata;
    }
}
