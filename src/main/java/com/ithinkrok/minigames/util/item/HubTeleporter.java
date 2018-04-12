package com.ithinkrok.minigames.util.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;

import java.util.Collections;

public class HubTeleporter {


    private String hubType;
    private String hubReturnLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfigOrEmpty();

        hubType = config.getString("hub_type", "hub");
        hubReturnLocale = config.getString("hub_return_locale", "hub_returner.return");
    }

    @CustomEventHandler
    public void onRightClick(UserInteractEvent event) {
        ClientMinigamesRequestProtocol requestProtocol = event.getGameGroup().getRequestProtocol();
        requestProtocol.sendJoinGameGroupPacket(event.getUser().getUuid(), hubType, null, Collections.emptyList());

        event.getUser().sendLocale(hubReturnLocale);
    }

}
