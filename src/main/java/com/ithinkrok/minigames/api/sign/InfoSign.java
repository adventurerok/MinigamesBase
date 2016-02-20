package com.ithinkrok.minigames.api.sign;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;

/**
 * Created by paul on 20/02/16.
 */
public abstract class InfoSign implements CustomListener {

    protected final GameGroup gameGroup;
    protected final Location location;

    protected int updateFrequency = 0;

    public InfoSign(UserEditSignEvent event) {
        location = event.getBlock().getLocation();
        gameGroup = event.getUserGameGroup();
    }

    public InfoSign(GameGroup gameGroup, Config config) {
        this.gameGroup = gameGroup;

        int x = config.getInt("x");
        int y = config.getInt("y");
        int z = config.getInt("z");

        location = new Location(gameGroup.getCurrentMap().getWorld(), x, y, z);

        updateFrequency = config.getInt("update_freq");
    }

    public final boolean update() {
        Material mat = location.getBlock().getType();
        if (mat != Material.SIGN_POST && mat != Material.WALL_SIGN) return false;

        updateSign();

        return true;
    }

    public int getUpdateFrequency() {
        return updateFrequency;
    }

    protected abstract void updateSign();

    public abstract void onRightClick(User user);

    public Location getLocation() {
        return location;
    }

    protected void updateSignFromFormat(String[] format, Config config) {
        Sign sign = (Sign) location.getBlock().getState();

        for(int index = 0; index < 4; ++index) {
            String formatted = ConfigUtils.formatString(format[index], config);

            sign.setLine(index, formatted);
        }

        sign.update();
    }

    public Config toConfig() {
        Config config = new MemoryConfig();

        config.set("x", location.getBlockX());
        config.set("y", location.getBlockY());
        config.set("z", location.getBlockZ());
        config.set("world", gameGroup.getCurrentMap().getInfo().getName());
        config.set("class", getClass().getName());
        config.set("update_freq", updateFrequency);

        return config;
    }
}
