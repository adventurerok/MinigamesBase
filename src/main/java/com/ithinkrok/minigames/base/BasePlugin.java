package com.ithinkrok.minigames.base;

import com.ithinkrok.minigames.api.Game;
import com.ithinkrok.minigames.api.SpecificPlugin;
import com.ithinkrok.minigames.api.database.BooleanUserValue;
import com.ithinkrok.minigames.api.database.DoubleUserValue;
import com.ithinkrok.minigames.api.database.IntUserValue;
import com.ithinkrok.minigames.api.database.StringUserValue;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.base.hub.Hub;
import com.ithinkrok.msm.bukkit.util.BukkitConfig;
import com.ithinkrok.msm.client.impl.MSMClient;
import com.ithinkrok.util.config.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 26/01/16.
 */
public class BasePlugin extends SpecificPlugin {

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

        super.onEnable();
    }

    private void loadHubModule() {
        requestProtocol = new ClientMinigamesRequestProtocol(this);

        MSMClient.addProtocol("MinigamesRequest", requestProtocol);

        hub = new Hub(this, requestProtocol);

        hub.registerListeners();
    }

    private void loadGameModule(Config config) {
        game = new BaseGame(this, config);

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
