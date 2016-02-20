package com.ithinkrok.minigames.base.hub;

import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.api.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.api.protocol.event.GameGroupKilledEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupSpawnedEvent;
import com.ithinkrok.minigames.api.protocol.event.GameGroupUpdateEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.config.YamlConfigIO;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by paul on 16/02/16.
 */
public class Hub implements Listener {

    private final Plugin plugin;
    private final ClientMinigamesRequestProtocol requestProtocol;

    private final Map<Location, HubSign> signs = new HashMap<>();

    private final Map<UUID, Location> openSpectatorInventories = new HashMap<>();
    private final Path configPath;

    public Hub(Plugin plugin, ClientMinigamesRequestProtocol requestProtocol) {
        this.plugin = plugin;
        this.requestProtocol = requestProtocol;

        configPath = plugin.getDataFolder().toPath().resolve("signs.yml");

        requestProtocol.enableGameGroupInfo();

        loadConfig();
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unload() {
        saveConfig();
    }

    public void saveConfig() {
        try {
            YamlConfigIO.saveConfig(configPath, toConfig());
        } catch (IOException e) {
            System.out.println("Error while saving signs.yml");
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        try {
            Config config = YamlConfigIO.loadToConfig(configPath, new MemoryConfig());
            fromConfig(config);
        } catch (IOException e) {
            System.out.println("Error while loading signs.yml");
            e.printStackTrace();
        }
    }

    public void addOpenSpectatorInventory(User user, Location location) {
        openSpectatorInventories.put(user.getUuid(), location);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        openSpectatorInventories.remove(event.getPlayer().getUniqueId());
    }

    public Config toConfig() {
        List<Config> signConfigs = new ArrayList<>();

        for(HubSign sign : signs.values()) {
            signConfigs.add(sign.toConfig());
        }

        Config result = new MemoryConfig();
        result.set("signs", signConfigs);

        return result;
    }

    public void fromConfig(Config config) {
        List<Config> signConfigs = config.getConfigList("signs");

        for(Config signConfig : signConfigs) {
            String worldName = signConfig.getString("world");
            if(plugin.getServer().getWorld(worldName) == null) continue;

            HubSign sign = new HubSign(plugin.getServer(), signConfig);

            signs.put(sign.getLocation(), sign);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if(signs.remove(event.getBlock().getLocation()) != null) saveConfig();

        List<UUID> removeKeys = new ArrayList<>();

        for(Map.Entry<UUID, Location> entry : openSpectatorInventories.entrySet()) {
            if(!entry.getValue().equals(event.getBlock().getLocation())) return;

            removeKeys.add(entry.getKey());

            Player player = plugin.getServer().getPlayer(entry.getKey());
            if(player == null) return;

            player.closeInventory();
        }

        for(UUID key : removeKeys) {
            openSpectatorInventories.remove(key);
        }
    }

    public ClientMinigamesRequestProtocol getRequestProtocol() {
        return requestProtocol;
    }

    public ControllerInfo getControllerInfo() {
        return requestProtocol.getControllerInfo();
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if(!signs.containsKey(event.getBlock().getLocation())) return;

        event.setCancelled(true);
    }

}
