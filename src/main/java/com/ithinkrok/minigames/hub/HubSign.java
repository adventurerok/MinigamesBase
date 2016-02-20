package com.ithinkrok.minigames.hub;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.controller.ControllerGameGroupEvent;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.sign.InfoSign;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;

/**
 * Created by paul on 16/02/16.
 */
public abstract class HubSign extends InfoSign {

    protected final String gameGroupType;

    public HubSign(UserEditSignEvent event) {
        super(event);

        gameGroupType = event.getLine(1);
    }

    public HubSign(GameGroup gameGroup, Config config) {
        super(gameGroup, config);
        gameGroupType = config.getString("type");
    }

    public String getGameGroupType() {
        return gameGroupType;
    }

    @Override
    public Config toConfig() {
        Config config = super.toConfig();

        config.set("type", gameGroupType);

        return config;
    }

    @CustomEventHandler
    public void onControllerGameGroupEvent(ControllerGameGroupEvent event) {
        if(!gameGroupType.equals(event.getControllerGameGroup().getType())) return;

        update();
    }
}
