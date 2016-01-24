package com.ithinkrok.minigames.schematic;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 07/01/16.
 */
public class SchematicOptions {
    private Material centerBlockType;
    private boolean progressHologram = false;
    private boolean doMapBoundsCheck = true;
    private int buildSpeed = 2;

    private Map<Material, Material> replaceMaterials = new HashMap<>();
    private DyeColor overrideDyeColor;
    private List<Listener> defaultListeners = new ArrayList<>();

    public SchematicOptions() {

    }

    public SchematicOptions(ConfigurationSection config) {
        progressHologram = config.getBoolean("progress_hologram");
        doMapBoundsCheck = config.getBoolean("do_map_bounds_check", true);
        buildSpeed = config.getInt("build_speed");

        if(config.contains("center_block_material")) {
            centerBlockType = Material.matchMaterial(config.getString("center_block_material"));
        }

        if(config.contains("replace_materials")) {
            ConfigurationSection repMatSection = config.getConfigurationSection("replace_materials");
            for(String oldMatName : repMatSection.getKeys(false)) {
                Material oldMat = Material.matchMaterial(oldMatName);
                Material newMat = Material.matchMaterial(repMatSection.getString(oldMatName));

                replaceMaterials.put(oldMat, newMat);
            }
        }
    }

    public boolean doMapBoundsCheck() {
        return doMapBoundsCheck;
    }

    public SchematicOptions withMapBoundsCheck(boolean useMapBoundsCheck) {
        this.doMapBoundsCheck = useMapBoundsCheck;

        return this;
    }

    public Material getCenterBlockType() {
        return this.centerBlockType;
    }

    public SchematicOptions withCenterBlockType(Material centerBlockType) {
        this.centerBlockType = centerBlockType;
        return this;
    }

    public boolean getProgressHologram() {
        return this.progressHologram;
    }

    public SchematicOptions withProgressHologram(boolean progressHologram) {
        this.progressHologram = progressHologram;
        return this;
    }

    public int getBuildSpeed() {
        return this.buildSpeed;
    }

    public SchematicOptions withBuildSpeed(int buildSpeed) {
        this.buildSpeed = buildSpeed;
        return this;
    }

    public Map<Material, Material> getReplaceMaterials() {
        return this.replaceMaterials;
    }

    public SchematicOptions withReplaceMaterials(Map<Material, Material> replaceMaterials) {
        this.replaceMaterials.putAll(replaceMaterials);
        return this;
    }

    public SchematicOptions withReplaceMaterial(Material from, Material to) {
        this.replaceMaterials.put(from, to);
        return this;
    }

    public DyeColor getOverrideDyeColor() {
        return this.overrideDyeColor;
    }

    public SchematicOptions withOverrideDyeColor(DyeColor overrideDyeColor) {
        this.overrideDyeColor = overrideDyeColor;
        return this;
    }

    public SchematicOptions withDefaultListener(Listener defaultListener) {
        this.defaultListeners.add(defaultListener);
        return this;
    }

    public List<Listener> getDefaultListeners() {
        return defaultListeners;
    }
}
