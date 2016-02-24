package com.ithinkrok.minigames.api.schematic;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.ithinkrok.minigames.api.Nameable;
import com.ithinkrok.minigames.api.schematic.blockentity.BlockEntity;
import com.ithinkrok.minigames.api.schematic.blockentity.SignBlockEntity;
import com.ithinkrok.minigames.api.schematic.blockentity.SkullBlockEntity;
import com.ithinkrok.minigames.api.util.NBTConfigIO;
import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final Map<Vector, BlockEntity> blockEntityMap = new HashMap<>();

    private final Config config;

    private final List<String> upgradesTo;

    private final boolean allowOverlap;

    public BlockEntity getBlockEntity(Vector location) {
        return blockEntityMap.get(location);
    }

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
        if(baseOffset == null) baseOffset = new Vector();

        String schematicFile = config.getString("file");

        Path schemFile = dataFolder.resolve(schematicFile);

        try (NBTInputStream in = new NBTInputStream(Files.newInputStream(schemFile))) {
            CompoundMap nbt = ((CompoundTag) in.readTag()).getValue();
            Config nbtConfig = NBTConfigIO.loadToConfig(nbt, new MemoryConfig());

            short width = nbtConfig.getShort("Width");
            short height = nbtConfig.getShort("Height");
            short length = nbtConfig.getShort("Length");

            int offsetX = nbtConfig.getInt("WEOffsetX") + baseOffset.getBlockX();
            int offsetY = nbtConfig.getInt("WEOffsetY") + baseOffset.getBlockY();
            int offsetZ = nbtConfig.getInt("WEOffsetZ") + baseOffset.getBlockZ();

            byte[] blocks = nbtConfig.getByteArray("Blocks");
            byte[] data = nbtConfig.getByteArray("Data");

            List<Config> tileEntities = nbtConfig.getConfigList("TileEntities");

            loadBlockEntities(tileEntities);

            this.size = new Vector(width, height, length);
            this.offset = new Vector(offsetX, offsetY, offsetZ);

            this.blocks = blocks;
            this.data = data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schematic: " + schematicFile, e);
        }
    }

    private void loadBlockEntities(List<Config> tileEntities) {
        for(Config config : tileEntities) {
            BlockEntity entity = createBlockEntity(config);
            if(entity == null) continue;

            int x = config.getInt("x");
            int y = config.getInt("y");
            int z = config.getInt("z");
            Vector pos = new Vector(x, y, z);
            blockEntityMap.put(pos, entity);
        }
    }

    private BlockEntity createBlockEntity(Config config) {
        String id = config.getString("id");
        if(id == null) return null;

        switch(id) {
            case "Sign":
                return new SignBlockEntity(config);
            case "Skull":
                return new SkullBlockEntity(config);
            default:
                return null;
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
