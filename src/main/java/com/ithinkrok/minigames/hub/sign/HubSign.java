package com.ithinkrok.minigames.hub.sign;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.controller.ControllerGameGroupEvent;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.sign.InfoSign;
import com.ithinkrok.minigames.api.sign.SignController;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;

/**
 * Created by paul on 16/02/16.
 */
public abstract class HubSign extends InfoSign {

    protected final String gameGroupType;

    public HubSign(UserEditSignEvent event, SignController signController) {
        super(event, signController);

        gameGroupType = event.getLine(1);
    }

    public HubSign(GameGroup gameGroup, Config config, SignController signController) {
        super(gameGroup, config, signController);
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
