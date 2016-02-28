package com.ithinkrok.minigames.util.map;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.InfoSignEvent;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.map.MapBlockBreakNaturallyEvent;
import com.ithinkrok.minigames.api.event.user.world.UserBreakBlockEvent;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractWorldEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.sign.InfoSign;
import com.ithinkrok.minigames.api.sign.InfoSigns;
import com.ithinkrok.minigames.api.sign.SignController;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.config.YamlConfigIO;
import com.ithinkrok.util.event.CustomEventExecutor;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 28/02/16.
 */
public class SignListener implements CustomListener {

    protected GameGroup gameGroup;

    protected final Map<Location, InfoSign> signs = new HashMap<>();

    protected final ClassToInstanceMap<SignController> signControllers = MutableClassToInstanceMap.create();

    private Path configPath;

    private Config signsConfig;

    protected GameMap map;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, ?> event) {
        this.gameGroup = event.getCreator();

        Config config = event.getConfigOrEmpty();

        if(config.contains("signs_config_path")) {
            configPath = Paths.get(config.getString("signs_config_path"));
        } else if(config.contains("signs")) {
            signsConfig = config.getConfigOrNull("signs");
        }

        gameGroup.getRequestProtocol().enableControllerInfo();

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

    public Config toConfig() {
        List<Config> signConfigs = new ArrayList<>();

        for(InfoSign sign : signs.values()) {
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

            InfoSign sign = InfoSigns.loadInfoSign(gameGroup, signConfig, signControllers);
            if(sign == null) continue;

            signs.put(sign.getLocation(), sign);

            gameGroup.doInFuture(task -> {
                sign.update();
            });

            startSignTask(sign);
        }
    }

    private void startSignTask(InfoSign sign) {
        if(sign.getUpdateFrequency() <= 0) return;

        GameTask task = gameGroup.repeatInFuture(task1 -> {
            if(sign.update()) return;

            signs.remove(sign.getLocation());
        }, sign.getUpdateFrequency(), sign.getUpdateFrequency());

        gameGroup.bindTaskToCurrentMap(task);
    }

    @CustomEventHandler
    public void onMapChange(MapChangedEvent event) {
        if(this.map != null && this.map != event.getNewMap()){
            this.signs.clear();
            return;
        }

        loadConfig(event.getNewMap());
    }

    @CustomEventHandler
    public void onUserBreakBlock(UserBreakBlockEvent event) {
        if(signs.remove(event.getBlock().getLocation()) != null) saveConfig();
    }

    @CustomEventHandler
    public void onMapBlockBreak(MapBlockBreakNaturallyEvent event) {
        signs.remove(event.getBlock().getLocation());
    }

    @CustomEventHandler
    public void onSignChange(UserEditSignEvent event) {
        InfoSign sign = InfoSigns.createInfoSign(event, signControllers);
        if(sign == null) return;

        signs.put(sign.getLocation(), sign);

        gameGroup.doInFuture(task -> {
            sign.update();
        });

        saveConfig();

        startSignTask(sign);
    }

    @CustomEventHandler
    public void onUserInteractWorld(UserInteractWorldEvent event) {
        if(!event.hasBlock() || event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;

        InfoSign sign = signs.get(event.getClickedBlock().getLocation());
        if(sign == null) return;

        sign.onRightClick(event.getUser());
    }

    @CustomEventHandler
    public void onInfoSignEvent(InfoSignEvent event) {
        for(InfoSign sign : signs.values()) {
            CustomEventExecutor.executeEvent(event, sign);
        }
    }
}
