package com.ithinkrok.minigames.base.hub;

import com.ithinkrok.minigames.base.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.base.protocol.event.GameGroupKilledEvent;
import com.ithinkrok.minigames.base.protocol.event.GameGroupSpawnedEvent;
import com.ithinkrok.minigames.base.protocol.event.GameGroupUpdateEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 16/02/16.
 */
public class Hub implements Listener {

    private final Plugin plugin;
    private final ClientMinigamesRequestProtocol requestProtocol;

    private final Map<Location, HubSign> signs = new HashMap<>();

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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        signs.remove(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if(!signs.containsKey(event.getBlock().getLocation())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if(!event.getLine(0).equalsIgnoreCase("[MG_SIGN]")) return;

        HubSign sign = new HubSign(event);

        signs.put(sign.getLocation(), sign);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            sign.update(requestProtocol.getControllerInfo());
        });
    }

    @EventHandler
    public void onGameGroupSpawned(GameGroupSpawnedEvent event) {
        System.out.println("GameGroup spawned: " + event.getGameGroup().getName());

        updateSigns();
    }

    private void updateSigns() {
        for(HubSign sign : signs.values()) {
            sign.update(requestProtocol.getControllerInfo());
        }
    }

    @EventHandler
    public void onGameGroupUpdate(GameGroupUpdateEvent event) {
        System.out.println("GameGroup update: " + event.getGameGroup().getName());

        updateSigns();
    }

    @EventHandler
    public void onGameGroupKilled(GameGroupKilledEvent event) {
        System.out.println("GameGroup killed: " + event.getGameGroup().getName());

        updateSigns();
    }
}
