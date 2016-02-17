package com.ithinkrok.minigames.base.hub;

import com.ithinkrok.minigames.base.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.base.protocol.event.GameGroupKilledEvent;
import com.ithinkrok.minigames.base.protocol.event.GameGroupSpawnedEvent;
import com.ithinkrok.minigames.base.protocol.event.GameGroupUpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

/**
 * Created by paul on 16/02/16.
 */
public class Hub implements Listener {

    private final Plugin plugin;
    private final ClientMinigamesRequestProtocol requestProtocol;

    public Hub(Plugin plugin, ClientMinigamesRequestProtocol requestProtocol) {
        this.plugin = plugin;
        this.requestProtocol = requestProtocol;

        requestProtocol.enableGameGroupInfo();
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unload() {

    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if(event.getLine(0).equalsIgnoreCase("[MG_SIGN]")) event.getPlayer().sendMessage("Hi there");
    }

    @EventHandler
    public void onGameGroupSpawned(GameGroupSpawnedEvent event) {
        System.out.println("GameGroup spawned: " + event.getGameGroup().getName());
    }

    @EventHandler
    public void onGameGroupUpdate(GameGroupUpdateEvent event) {
        System.out.println("GameGroup update: " + event.getGameGroup().getName());
    }

    @EventHandler
    public void onGameGroupKilled(GameGroupKilledEvent event) {
        System.out.println("GameGroup killed: " + event.getGameGroup());
    }
}
