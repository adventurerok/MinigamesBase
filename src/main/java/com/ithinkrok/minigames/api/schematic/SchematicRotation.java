package com.ithinkrok.minigames.api.schematic;

import com.ithinkrok.minigames.api.schematic.blockentity.BlockEntity;
import com.ithinkrok.minigames.api.util.BoundingBox;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by paul on 07/01/16.
 */
public class SchematicRotation {

    byte[] blocks;
    byte[] data;
    boolean xzSwap = false;
    boolean xFlip = false;
    boolean zFlip = false;
    private final int rotation;
    private final int width;
    private final int height;
    private final int length;
    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;
    private final Schematic schematic;

    public SchematicRotation(Schematic schematic, int rotation) {
        this.schematic = schematic;
        this.width = schematic.getSize().getBlockX();
        this.height = schematic.getSize().getBlockY();
        this.length = schematic.getSize().getBlockZ();
        this.offsetX = schematic.getOffset().getBlockX();
        this.offsetY = schematic.getOffset().getBlockY();
        this.offsetZ = schematic.getOffset().getBlockZ();
        this.blocks = schematic.getBlocks();
        this.data = schematic.getData();

        rotation = (rotation + schematic.getBaseRotation()) & 3;
        this.rotation = rotation;

        if (rotation == 1 || rotation == 3) xzSwap = true;
        if (rotation == 3 || rotation == 2) xFlip = true;
        if (rotation == 2 || rotation == 1) zFlip = true;
    }

    public int getRotation() {
        return rotation;
    }

    public int getBlock(int x, int y, int z) {
        return blocks[calcIndex(x, y, z)] & 0xFF;
    }

    private int calcIndex(int x, int y, int z) {
        if (xzSwap) {
            int i = x;
            x = z;
            z = i;
        }

        if (xFlip) x = width - x - 1;
        if (zFlip) z = length - z - 1;

        return width * (y * length + z) + x;
    }

    public byte getData(int x, int y, int z) {
        return data[calcIndex(x, y, z)];
    }

    private BoundingBox calcBoundsNoUpgrades(Location loc) {
        Vector minBB = new Vector(loc.getX() + getOffsetX(), loc.getY() + getOffsetY(), loc.getZ() + getOffsetZ());
        Vector maxBB = new Vector(minBB.getX() + getWidth() - 1, minBB.getY() + getHeight() - 1,
                minBB.getZ() + getLength() - 1);

        return new BoundingBox(minBB, maxBB);
    }

    public BoundingBox calcBounds(SchematicResolver resolver, Location loc) {
        BoundingBox bounds = calcBoundsNoUpgrades(loc);


        int modRotation = (rotation - schematic.getBaseRotation()) % 4;

        for(String schemName : schematic.getUpgradesTo()){
            Schematic upgrade = resolver.getSchematic(schemName);
            if(upgrade == null) continue;


            BoundingBox other = upgrade.getSchematicRotation(modRotation).calcBoundsNoUpgrades(loc);
            bounds = bounds.include(other);
        }

        return bounds;
    }

    public int getOffsetX() {
        int base = xzSwap ? offsetZ : offsetX;

        boolean flip = xzSwap ? zFlip : xFlip;

        if (flip) base = 1 - base - getWidth();
        return base;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getOffsetZ() {
        int base = xzSwap ? offsetX : offsetZ;

        boolean flip = xzSwap ? xFlip : zFlip;

        if (flip) base = 1 - base - getLength();
        return base;
    }

    public int getWidth() {
        return xzSwap ? length : width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return xzSwap ? width : length;
    }

    public BlockEntity getBlockEntity(int x, int y, int z) {
        if (xzSwap) {
            int i = x;
            x = z;
            z = i;
        }

        if (xFlip) x = width - x - 1;
        if (zFlip) z = length - z - 1;

        return schematic.getBlockEntity(new Vector(x, y, z));
    }
}
