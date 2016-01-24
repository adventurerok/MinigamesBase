package com.ithinkrok.minigames.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by paul on 13/11/15.
 * <p>
 * A bounding box made up of a min and a max vector
 */
public class BoundingBox {

    public final Vector min;
    public final Vector max;

    public BoundingBox(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    public boolean intercepts(BoundingBox other) {
        return !(max.getX() < other.min.getX() || min.getX() > other.max.getX()) &&
                !(max.getY() < other.min.getY() || min.getY() > other.max.getY()) &&
                !(max.getZ() < other.min.getZ() || min.getZ() > other.max.getZ());

    }

    public boolean interceptsXZ(BoundingBox other) {
        return !(max.getX() < other.min.getX() || min.getX() > other.max.getX()) &&
                !(max.getZ() < other.min.getZ() || min.getZ() > other.max.getZ());
    }

    public boolean containsLocation(Location loc) {
        return loc.getX() >= min.getX() && loc.getX() <= max.getX() && loc.getY() >= min.getY() &&
                loc.getY() <= max.getY() && loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
    }
}
