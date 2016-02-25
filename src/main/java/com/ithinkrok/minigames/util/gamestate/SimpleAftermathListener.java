package com.ithinkrok.minigames.util.gamestate;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.api.event.game.CountdownMessageEvent;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.event.user.world.UserChatEvent;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.CountdownConfig;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by paul on 25/02/16.
 */
public class SimpleAftermathListener implements CustomListener {

    private CountdownConfig countdown;

    private GameState gameState;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        gameState = event.getRepresenting();

        Config config = event.getConfigOrEmpty();
        countdown = MinigamesConfigs.getCountdown(config, "countdown", "aftermath", 15, "countdowns.aftermath");
    }

    @CustomEventHandler
    public void onGameStateChanged(GameStateChangedEvent event) {
        if (!Objects.equals(event.getNewGameState(), gameState)) return;


        //Remove user scoreboards
        for (User user : event.getGameGroup().getUsers()) {
            user.setScoreboardHandler(null);
        }

        event.getGameGroup().startCountdown(countdown);

        GameTask task = event.getGameGroup().repeatInFuture(t -> {
            if (t.getRunCount() > 5) t.finish();

            for (User user : event.getGameGroup().getUsers()) {
                if (!shouldLaunchFireworksForUser(user)) continue;

                user.launchVictoryFirework();
            }

        }, 20, 20);

        event.getGameGroup().bindTaskToCurrentGameState(task);
    }

    @CustomEventHandler
    public void onCountdownMessage(CountdownMessageEvent event) {
        if(!event.getCountdown().getName().equals(countdown.getName())) return;

        if(event.getCountdown().getSecondsRemaining() != 3) return;

        //Send players to the hub early
        List<User> users = new ArrayList<>(event.getGameGroup().getUsers());
        for(User user : users) {
            if(!user.isPlayer()) continue;

            Player player = user.getPlayer();
            event.getGameGroup().getGame().sendPlayerToHub(player);
        }
    }

    @CustomEventHandler
    public void onCountdownFinished(CountdownFinishedEvent event) {
        if (!event.getCountdown().getName().equals(countdown.getName())) return;

        //event.getGameGroup().changeGameState(lobbyGameState);
        event.getGameGroup().kill();
    }

    @CustomEventHandler
    public void onUserEvent(UserEvent event) {
        if (!(event instanceof Cancellable) || event instanceof UserChatEvent) return;

        ((Cancellable) event).setCancelled(true);
    }

    protected boolean shouldLaunchFireworksForUser(User user) {
        return user.isInGame();
    }

}
