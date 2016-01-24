package com.ithinkrok.minigames.schematic;

import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.ithinkrok.minigames.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 07/01/16.
 */
public class Schematic {

    private String name;
    private int baseRotation;
    private Vector offset;
    private Vector size;

    private byte[] blocks;
    private byte[] data;

    private ConfigurationSection config;

    private List<String> upgradesTo;

    private boolean allowOverlap;

    public Schematic(String name, File dataFolder, ConfigurationSection config) {
        this.name = name;
        this.config = config.getConfigurationSection("config");
        this.baseRotation = config.getInt("rotation", 0);
        this.upgradesTo = config.getStringList("upgrades");
        this.allowOverlap = config.getBoolean("allow_overlap");
        if(this.upgradesTo == null) this.upgradesTo = Collections.emptyList();

        Vector baseOffset = ConfigUtils.getVector(config, "offset");

        String schematicFile = config.getString("file");

        File schemFile = new File(dataFolder, schematicFile);

        try (NBTInputStream in = new NBTInputStream(new FileInputStream(schemFile))) {
            CompoundMap nbt = ((CompoundTag) in.readTag()).getValue();

            short width = ((ShortTag) nbt.get("Width")).getValue();
            short height = ((ShortTag) nbt.get("Height")).getValue();
            short length = ((ShortTag) nbt.get("Length")).getValue();

            int offsetX = ((IntTag) nbt.get("WEOffsetX")).getValue() + baseOffset.getBlockX();
            int offsetY = ((IntTag) nbt.get("WEOffsetY")).getValue() + baseOffset.getBlockY();
            int offsetZ = ((IntTag) nbt.get("WEOffsetZ")).getValue() + baseOffset.getBlockZ();

            byte[] blocks = ((ByteArrayTag) nbt.get("Blocks")).getValue();
            byte[] data = ((ByteArrayTag) nbt.get("Data")).getValue();

            this.size = new Vector(width, height, length);
            this.offset = new Vector(offsetX, offsetY, offsetZ);

            this.blocks = blocks;
            this.data = data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schematic: " + schematicFile, e);
        }
    }

    public boolean getAllowOverlap() {
        return allowOverlap;
    }

    public SchematicRotation getSchematicRotation(int rotation) {
        return new SchematicRotation(this, rotation);
    }

    public List<String> getUpgradesTo() {
        return upgradesTo;
    }

    public String getName() {
        return name;
    }

    public int getBaseRotation() {
        return baseRotation;
    }

    public Vector getOffset() {
        return offset;
    }

    public byte[] getBlocks() {
        return blocks;
    }

    public byte[] getData() {
        return data;
    }

    public Vector getSize() {
        return size;
    }

    public ConfigurationSection getConfig() {
        return config;
    }
}
