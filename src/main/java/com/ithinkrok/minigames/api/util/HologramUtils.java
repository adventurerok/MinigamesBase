package com.ithinkrok.minigames.api.util;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public final class HologramUtils {


    private HologramUtils() {

    }

    public static Hologram createHologram(Location location) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MinigamesBase");

        return HologramsAPI.createHologram(plugin, location);
    }

}
