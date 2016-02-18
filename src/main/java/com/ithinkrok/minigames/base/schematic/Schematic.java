package com.ithinkrok.minigames.base.schematic;

import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.ithinkrok.minigames.base.Nameable;
import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 07/01/16.
 */
public class Schematic implements Nameable {

    private final String name;
    private final String formattedName;

    private final int baseRotation;
    private final Vector offset;
    private final Vector size;

    private final byte[] blocks;
    private final byte[] data;

    private final Config config;

    private final List<String> upgradesTo;

    private final boolean allowOverlap;

    public Schematic(String name, Path dataFolder, Config config) {
        this.name = name;
        this.formattedName = config.getString("formatted_name", name);
        this.config = config.getConfigOrEmpty("config");
        this.baseRotation = config.getInt("rotation", 0);

        this.allowOverlap = config.getBoolean("allow_overlap");

        List<String> upgradesToTemp;
        upgradesToTemp = config.getStringList("upgrades");
        if (upgradesToTemp == null) upgradesToTemp = Collections.emptyList();

        this.upgradesTo = upgradesToTemp;
        Vector baseOffset = BukkitConfigUtils.getVector(config, "offset");

        String schematicFile = config.getString("file");

        Path schemFile = dataFolder.resolve(schematicFile);

        try (NBTInputStream in = new NBTInputStream(Files.newInputStream(schemFile))) {
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

    @Override
    public String getFormattedName() {
        return formattedName;
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

    public Config getConfig() {
        return config;
    }
}
