package com.ithinkrok.minigames;

import com.ithinkrok.minigames.database.BooleanUserValue;
import com.ithinkrok.minigames.database.DoubleUserValue;
import com.ithinkrok.minigames.database.IntUserValue;
import com.ithinkrok.minigames.database.StringUserValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Created by paul on 17/01/16.
 */
public class MinigamesPlugin extends JavaPlugin {

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> result = super.getDatabaseClasses();

        result.add(IntUserValue.class);
        result.add(StringUserValue.class);
        result.add(DoubleUserValue.class);
        result.add(BooleanUserValue.class);

        return result;
    }
}
