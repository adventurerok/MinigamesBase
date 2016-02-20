package com.ithinkrok.minigames.hub;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.base.BasePlugin;
import com.ithinkrok.minigames.base.hub.HubSign;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.config.YamlConfigIO;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by paul on 20/02/16.
 */
public class HubListener implements CustomListener {

    private ClientMinigamesRequestProtocol requestProtocol;
    private GameGroup gameGroup;

    private final Map<Location, HubSign> signs = new HashMap<>();

    private final Map<UUID, Location> openSpectatorInventories = new HashMap<>();

    private Path configPath;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, ?> event) {
        this.gameGroup = event.getCreator();
        this.requestProtocol = BasePlugin.getRequestProtocol();

        configPath = Paths.get(event.getConfigOrEmpty().getString("signs_config_path"));

        requestProtocol.enableGameGroupInfo();

        loadConfig();
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

    @CustomEventHandler
    public void onUserJoin(UserJoinEvent event) {
        event.getUser().setInGame(true);
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
            if(Bukkit.getServer().getWorld(worldName) == null) continue;

            HubSign sign = new HubSign(Bukkit.getServer(), signConfig);

            signs.put(sign.getLocation(), sign);
        }
    }

    private void updateSigns() {
        for(HubSign sign : signs.values()) {
            sign.update(requestProtocol.getControllerInfo());
        }

        for(Map.Entry<UUID, Location> entry : openSpectatorInventories.entrySet()) {
            HubSign sign = signs.get(entry.getValue());
            if(sign == null) continue;

            User user = gameGroup.getUser(entry.getKey());
            if(user == null) continue;

            //user.upda
        }
    }
}
