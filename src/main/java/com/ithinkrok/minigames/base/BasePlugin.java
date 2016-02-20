package com.ithinkrok.minigames.base;

import com.ithinkrok.minigames.api.Game;
import com.ithinkrok.minigames.api.SpecificPlugin;
import com.ithinkrok.minigames.api.database.BooleanUserValue;
import com.ithinkrok.minigames.api.database.DoubleUserValue;
import com.ithinkrok.minigames.api.database.IntUserValue;
import com.ithinkrok.minigames.api.database.StringUserValue;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.msm.bukkit.util.BukkitConfig;
import com.ithinkrok.msm.client.impl.MSMClient;
import com.ithinkrok.util.config.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 26/01/16.
 */
public class BasePlugin extends SpecificPlugin {

    private static Game game;

    private static ClientMinigamesRequestProtocol requestProtocol;

    public static Game getGame() {
        return game;
    }

    public static ClientMinigamesRequestProtocol getRequestProtocol() {
        return requestProtocol;
    }

    @Override
    public void onEnable() {
        Config config = new BukkitConfig(getConfig());

        requestProtocol = new ClientMinigamesRequestProtocol(this);

        MSMClient.addProtocol("MinigamesRequest", requestProtocol);

        if(config.getBoolean("modules.game", true)) {
            loadGameModule(config);
        }

        super.onEnable();
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
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> result = new ArrayList<>();

        result.add(IntUserValue.class);
        result.add(DoubleUserValue.class);
        result.add(BooleanUserValue.class);
        result.add(StringUserValue.class);

        return result;
    }
}
