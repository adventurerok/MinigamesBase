package com.ithinkrok.minigames.hub;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.controller.ControllerGameGroupEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.map.MapBlockBreakNaturallyEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.api.event.user.world.UserBreakBlockEvent;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractWorldEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.api.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.base.BasePlugin;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.config.YamlConfigIO;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;

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

    private Config signsConfig;

    private GameMap map;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, ?> event) {
        this.gameGroup = event.getCreator();
        this.requestProtocol = BasePlugin.getRequestProtocol();

        Config config = event.getConfigOrEmpty();

        if(config.contains("signs_config_path")) {
            configPath = Paths.get(config.getString("signs_config_path"));
        } else if(config.contains("signs")) {
            signsConfig = config.getConfigOrNull("signs");
        }

        requestProtocol.enableGameGroupInfo();

        if(event.getRepresenting() instanceof GameMap) {
            this.map = (GameMap) event.getRepresenting();
        }
    }

    public void saveConfig() {
        if(configPath == null) return;

        try {
            YamlConfigIO.saveConfig(configPath, toConfig());
        } catch (IOException e) {
            System.out.println("Error while saving signs.yml");
            e.printStackTrace();
        }
    }

    public void loadConfig(GameMap map) {
        if(configPath != null) {
            try {
                Config config = YamlConfigIO.loadToConfig(configPath, new MemoryConfig());
                fromConfig(config, map);
            } catch (IOException e) {
                System.out.println("Error while loading signs.yml");
                e.printStackTrace();
            }
        }

        if(signsConfig != null) {
            fromConfig(signsConfig, map);
        }
    }

    @CustomEventHandler
    public void onUserJoin(UserJoinEvent event) {
        event.getUser().setInGame(true);
    }

    @CustomEventHandler
    public void onMapChange(MapChangedEvent event) {
        if(this.map != null && this.map != event.getNewMap()){
            this.signs.clear();
            return;
        }

        loadConfig(event.getNewMap());
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

    public void fromConfig(Config config, GameMap map) {
        List<Config> signConfigs = config.getConfigList("signs");

        for(Config signConfig : signConfigs) {
            String mapName = signConfig.getString("world");
            if(mapName != null && !mapName.equals(map.getInfo().getName())) {
                continue;
            }

            HubSign sign = new HubSign(map, signConfig);

            signs.put(sign.getLocation(), sign);
        }
    }

    public void addOpenSpectatorInventory(User user, Location location) {
        openSpectatorInventories.put(user.getUuid(), location);
    }

    private void updateSigns() {
        Iterator<HubSign> iterator = signs.values().iterator();

        while(iterator.hasNext()) {
            HubSign sign = iterator.next();

            if(!sign.update(requestProtocol.getControllerInfo())) {
                iterator.remove();
            }
        }

        for(Map.Entry<UUID, Location> entry : openSpectatorInventories.entrySet()) {
            HubSign sign = signs.get(entry.getValue());
            if(sign == null) continue;

            User user = gameGroup.getUser(entry.getKey());
            if(user == null) continue;

            sign.updateSpectatorInventory(this, user);
        }
    }

    @CustomEventHandler
    public void onInventoryClose(UserInventoryCloseEvent event) {
        openSpectatorInventories.remove(event.getUser().getUuid());
    }

    @CustomEventHandler
    public void onUserBreakBlock(UserBreakBlockEvent event) {
        if(signs.remove(event.getBlock().getLocation()) != null) saveConfig();

        List<UUID> removeKeys = new ArrayList<>();

        for(Map.Entry<UUID, Location> entry : openSpectatorInventories.entrySet()) {
            if(!entry.getValue().equals(event.getBlock().getLocation())) return;

            removeKeys.add(entry.getKey());

            User user = gameGroup.getUser(entry.getKey());
            if(user == null) return;

            user.closeInventory();
        }

        for(UUID key : removeKeys) {
            openSpectatorInventories.remove(key);
        }
    }

    @CustomEventHandler
    public void onMapBlockBreak(MapBlockBreakNaturallyEvent event) {
        signs.remove(event.getBlock().getLocation());
    }

    @CustomEventHandler
    public void onSignChange(UserEditSignEvent event) {
        if(!event.getLine(0).equalsIgnoreCase("[MG_SIGN]")) return;

        HubSign sign = new HubSign(event);

        signs.put(sign.getLocation(), sign);

        gameGroup.doInFuture(task -> {
           sign.update(requestProtocol.getControllerInfo());
        });

        saveConfig();
    }

    @CustomEventHandler
    public void onUserInteractWorld(UserInteractWorldEvent event) {
        if(!event.hasBlock() || event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;

        HubSign sign = signs.get(event.getClickedBlock().getLocation());
        if(sign == null) return;

        sign.onRightClick(this, event.getUser());
    }

    @CustomEventHandler
    public void onControllerGameGroupEvent(ControllerGameGroupEvent event) {
        updateSigns();
    }

    public ControllerInfo getControllerInfo() {
        return requestProtocol.getControllerInfo();
    }

    public ClientMinigamesRequestProtocol getRequestProtocol() {
        return requestProtocol;
    }
}
