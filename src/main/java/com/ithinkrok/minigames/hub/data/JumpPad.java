package com.ithinkrok.minigames.hub.data;

import com.ithinkrok.util.config.Config;
import org.bukkit.Material;

/**
 * Created by paul on 17/09/16.
 */
public class JumpPad {

    private final Material material;

    public JumpPad(Config config) {
        this.material = Material.matchMaterial(config.getString("material"));
    }

    public Material getMaterial() {
        return material;
    }
}
