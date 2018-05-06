package com.ithinkrok.minigames.util.listener;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.controller.ControllerSpawnGameGroupEvent;
import com.ithinkrok.minigames.api.event.controller.ControllerUpdateGameGroupEvent;
import com.ithinkrok.msm.common.message.ConfigMessageBuilder;
import com.ithinkrok.msm.common.message.ConfigMessageFactory;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GamesJoinListener implements CustomListener {

    private String lobbyCreatedLocale;
    private ConfigMessageFactory clickJoinMessageFactory;
    private String playerJoinClickLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, ?> event) {
        Config config = event.getConfig();

        lobbyCreatedLocale = config.getString("lobby_created_locale");

        String clickJoinLocale = event.getCreator().getLocale(config.getString("click_join_locale"));
        clickJoinMessageFactory = new ConfigMessageFactory(clickJoinLocale);

        playerJoinClickLocale = config.getString("player_join_locale", "hub.player_join_click");

        event.getCreator().getRequestProtocol().enableControllerInfo();
    }

    @CustomEventHandler
    public void onGameGroupCreated(ControllerSpawnGameGroupEvent event) {
        String type = event.getControllerGameGroup().getType();
        if (type.equals("hub")) return;

        event.getGameGroup().sendLocale(lobbyCreatedLocale, type);

        ConfigMessageBuilder builder = clickJoinMessageFactory.newBuilder();
        builder.setClickAction("join", ConfigMessageBuilder.CLICK_RUN_COMMAND, "/join " + event
                .getControllerGameGroup().getName());

        event.getGameGroup().sendMessageNoPrefix(builder.getResult());
    }


    @CustomEventHandler
    public void onGameGroupUpdated(ControllerUpdateGameGroupEvent event) {
        Set<UUID> currentPlayers = event.getControllerGameGroup().getPlayers();
        Set<UUID> oldPlayers = event.getOldControllerGameGroup().getPlayers();

        if (currentPlayers.size() <= oldPlayers.size() || !event.getControllerGameGroup().isAcceptingPlayers()) return;

        //prevent hub messages
        if (event.getControllerGameGroup().getType().equals("hub")) return;

        Set<UUID> newPlayers = new HashSet<>(currentPlayers);
        newPlayers.removeAll(oldPlayers);

        UUID joined = newPlayers.iterator().next();

        event.getGameGroup().getDatabase().lookupName(joined, nameResult -> {
            String name = nameResult.name;
            if (name == null) return;

            String type = event.getControllerGameGroup().getType();
            String message = event.getGameGroup().getLocale(playerJoinClickLocale, name, type);
            ConfigMessageFactory factory = new ConfigMessageFactory(message);
            ConfigMessageBuilder builder = factory.newBuilder();
            String command = "/join " + event.getControllerGameGroup().getName();
            builder.setClickAction("join", ConfigMessageBuilder.CLICK_RUN_COMMAND, command);

            event.getGameGroup().sendMessageNoPrefix(builder.getResult());
        });
    }

}
