package com.ithinkrok.minigames.api.util;

import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.MapPoint;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by paul on 13/11/15.
 * <p>
 * A bounding box made up of a min and a max vector
 */
public class BoundingBox {

    private final MapPoint min;
    private final MapPoint max;

    public BoundingBox(Vector min, Vector max) {
        String world = GameMap.DEFAULT_WORLD_NAME;

        this.min = new MapPoint(world, min);
        this.max = new MapPoint(world, max);
    }

    public BoundingBox(MapPoint min, MapPoint max) {
        if(!Objects.equals(min.getWorld(), max.getWorld())) {
            throw new IllegalArgumentException("Points specified are in different worlds");
        }

        this.min = min;
        this.max = max;
    }

    public boolean intercepts(BoundingBox other) {
        if(!getWorld().equals(other.getWorld())) return false;

        return !(getMax().getX() < other.getMin().getX() || getMin().getX() > other.getMax().getX()) &&
               !(getMax().getY() < other.getMin().getY() || getMin().getY() > other.getMax().getY()) &&
               !(getMax().getZ() < other.getMin().getZ() || getMin().getZ() > other.getMax().getZ());

    }

    public boolean interceptsXZ(BoundingBox other) {
        if(!getWorld().equals(other.getWorld())) return false;

        return !(getMax().getX() < other.getMin().getX() || getMin().getX() > other.getMax().getX()) &&
               !(getMax().getZ() < other.getMin().getZ() || getMin().getZ() > other.getMax().getZ());
    }

    public boolean containsPoint(MapPoint p) {
        if(!getWorld().equals(p.getWorld())) return false;

        return p.getX() >= getMin().getX() && p.getX() <= getMax().getX() && p.getY() >= getMin().getY() &&
               p.getY() <= getMax().getY() && p.getZ() >= getMin().getZ() && p.getZ() <= getMax().getZ();
    }


    /**
     * Return a new BoundingBox that contains both this BoundingBox and the provided one in its bounds
     * @return The new BoundingBox
     */
    public BoundingBox include(BoundingBox other) {
        MapPoint min = this.getMin().min(other.getMin());
        MapPoint max = this.getMax().max(other.getMax());

        return new BoundingBox(min, max);
    }


    public MapPoint getMin() {
        return min;
    }


    public MapPoint getMax() {
        return max;
    }

    public String getWorld() {
        return min.getWorld();
    }

    public Stream<MapPoint> getBlockPoints() {
        int xSize = getMax().getBlockX() - getMin().getBlockX() + 1;
        int ySize = getMax().getBlockY() - getMin().getBlockY() + 1;
        int zSize = getMax().getBlockZ() - getMin().getBlockZ() + 1;

        return Stream.generate(new BlockPointSupplier()).limit(xSize * ySize * zSize);
    }

    private class BlockPointSupplier implements Supplier<MapPoint> {

        private int x, y, z;


        public BlockPointSupplier() {
            x = getMin().getBlockX();
            y = getMin().getBlockY();
            z = getMin().getBlockZ();
        }


        @Override
        public MapPoint get() {
            MapPoint result = getMin().setXYZ(x, y, z);

            y++;
            if(y > getMax().getBlockY()) {
                y = getMin().getBlockY();
                x++;
                if(x > getMax().getBlockX()) {
                    x = getMin().getBlockX();

                    z++;
                }
            }

            return result;
        }
    }
}
