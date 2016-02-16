package com.ithinkrok.minigames.base.hub;

import com.ithinkrok.minigames.base.protocol.ClientMinigamesRequestProtocol;
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
}
