package com.ithinkrok.minigames.hub;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.controller.ControllerGameGroupEvent;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.event.CustomEventHandler;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
