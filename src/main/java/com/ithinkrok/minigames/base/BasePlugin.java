package com.ithinkrok.minigames.base;

import com.ithinkrok.minigames.base.database.BooleanUserValue;
import com.ithinkrok.minigames.base.database.DoubleUserValue;
import com.ithinkrok.minigames.base.database.IntUserValue;
import com.ithinkrok.minigames.base.database.StringUserValue;
import com.ithinkrok.minigames.base.hub.Hub;
import com.ithinkrok.minigames.base.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.msm.client.impl.MSMClient;
import com.ithinkrok.msm.bukkit.util.BukkitConfig;
import com.ithinkrok.util.config.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 26/01/16.
 */
public class BasePlugin extends JavaPlugin {

    private static Game game;
    private static Hub hub;

    private ClientMinigamesRequestProtocol requestProtocol;

    public static Game getGame() {
        return game;
    }

    @Override
    public void onEnable() {
        Config config = new BukkitConfig(getConfig());

        if(config.getBoolean("modules.game", true)) {
            loadGameModule(config);
        }

        if(config.getBoolean("modules.hub", false)) {
            loadHubModule();
        }
    }

    private void loadHubModule() {
        requestProtocol = new ClientMinigamesRequestProtocol(this);

        MSMClient.addProtocol("MinigamesRequest", requestProtocol);

        hub = new Hub(this, requestProtocol);

        hub.registerListeners();
    }

    private void loadGameModule(Config config) {
        game = new Game(this, config);

        game.registerListeners();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }

    @Override
    public void onDisable() {
        if(game != null) game.unload();
        game = null;

        if(hub != null) hub.unload();
        hub = null;
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> result = new ArrayList<>();

        result.add(IntUserValue.class);
        result.add(DoubleUserValue.class);
        result.add(BooleanUserValue.class);
        result.add(StringUserValue.class);

        return result;
    }
}
