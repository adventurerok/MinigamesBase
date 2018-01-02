package com.ithinkrok.minigames.api.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by paul on 13/11/15.
 * <p>
 * A bounding box made up of a min and a max vector
 */
public class BoundingBox {

    private final Vector min;
    private final Vector max;

    public BoundingBox(Vector min, Vector max) {
        this.min = min;
        this.max = max;
    }

    public boolean intercepts(BoundingBox other) {
        return !(getMax().getX() < other.getMin().getX() || getMin().getX() > other.getMax().getX()) &&
               !(getMax().getY() < other.getMin().getY() || getMin().getY() > other.getMax().getY()) &&
               !(getMax().getZ() < other.getMin().getZ() || getMin().getZ() > other.getMax().getZ());

    }

    public boolean interceptsXZ(BoundingBox other) {
        return !(getMax().getX() < other.getMin().getX() || getMin().getX() > other.getMax().getX()) &&
               !(getMax().getZ() < other.getMin().getZ() || getMin().getZ() > other.getMax().getZ());
    }

    public boolean containsLocation(Location loc) {
        return loc.getX() >= getMin().getX() && loc.getX() <= getMax().getX() && loc.getY() >= getMin().getY() &&
               loc.getY() <= getMax().getY() && loc.getZ() >= getMin().getZ() && loc.getZ() <= getMax().getZ();
    }


    public Vector getMin() {
        return min;
    }


    public Vector getMax() {
        return max;
    }
}
