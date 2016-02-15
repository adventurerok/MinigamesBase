package com.ithinkrok.minigames.base;

import com.ithinkrok.minigames.base.database.BooleanUserValue;
import com.ithinkrok.minigames.base.database.DoubleUserValue;
import com.ithinkrok.minigames.base.database.IntUserValue;
import com.ithinkrok.minigames.base.database.StringUserValue;
import com.ithinkrok.minigames.base.protocol.ClientMinigamesProtocol;
import com.ithinkrok.minigames.base.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.msm.client.impl.MSMClient;
import com.ithinkrok.util.config.BukkitConfig;
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

    private ClientMinigamesRequestProtocol requestProtocol;

    public static Game getGame() {
        return game;
    }

    @Override
    public void onEnable() {
        Config config = new BukkitConfig(getConfig());

        game = new Game(this, config);

        game.registerListeners();

        requestProtocol = new ClientMinigamesRequestProtocol();

        MSMClient.addProtocol("MinigamesRequest", requestProtocol);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }

    @Override
    public void onDisable() {
        game.unload();
        game = null;
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
