package com.ithinkrok.minigames.base;

import com.ithinkrok.minigames.base.database.DoubleUserValue;
import com.ithinkrok.minigames.base.database.IntUserValue;
import com.ithinkrok.minigames.base.database.StringUserValue;
import com.ithinkrok.minigames.base.database.BooleanUserValue;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 26/01/16.
 */
public class BasePlugin extends JavaPlugin {

    private static Game game;

    public static Game getGame() {
        return game;
    }

    @Override
    public void onEnable() {
        ConfigurationSection config = getConfig();

        game = new Game(this, config);

        game.registerListeners();
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