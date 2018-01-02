package com.ithinkrok.minigames.api.map;

import com.ithinkrok.util.config.Config;
import org.bukkit.util.Vector;

import java.util.Objects;

public class MapPoint {

    /**
     * The default world name is 'map'
     */
    private final String world;

    private final double x, y, z;
    private final float yaw, pitch;


    public MapPoint(String world, double x, double y, double z) {
        this(world, x, y, z, Float.NaN, Float.NaN);
    }

    public MapPoint(String world, double x, double y, double z, float yaw, float pitch) {
        if (world == null) {
            throw new NullPointerException("World cannot be null");
        }

        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public MapPoint(Config config) {
        this.world = config.getString("world", GameMap.DEFAULT_WORLD_NAME);
        this.x = config.getDouble("x");
        this.y = config.getDouble("y");
        this.z = config.getDouble("z");
        this.yaw = (float) config.getDouble("yaw", 0);
        this.pitch = (float) config.getDouble("pitch", 0);
    }

    /**
     * Loads a MapPoint from a string representation.
     * The representation can have the world name at any position, or none for the default world name 'map'.
     * Numbers in order are: x, y, z, yaw, pitch
     * <p>
     * e.g. 100,54,100,nether,45,45
     */
    public MapPoint(String stringRep) {
        String[] parts = stringRep.split(",");
        if (parts.length < 3 || parts.length > 6) {
            throw new IllegalArgumentException("Expected a string with 3-6 comma separated parts");
        }

        String name = GameMap.DEFAULT_WORLD_NAME;
        double[] doubles = new double[5];
        doubles[4] = doubles[3] = Double.NaN;
        int doublesIndex = 0;

        //This way the name can be in whatever position we want
        for (String part : parts) {
            try {
                double d = Double.parseDouble(part);
                doubles[doublesIndex++] = d;
            } catch (NumberFormatException ignored) {
                name = part;
            }
        }

        if (doublesIndex < 3) {
            throw new IllegalArgumentException("x, y and z were not specified");
        }

        this.world = name;
        this.x = doubles[0];
        this.y = doubles[1];
        this.z = doubles[2];
        this.yaw = (float) doubles[3];
        this.pitch = (float) doubles[4];
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getBlockX() {
        return (int) Math.floor(x);
    }

    public int getBlockY() {
        return (int) Math.floor(y);
    }

    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    public Vector getXYZ() {
        return new Vector(x, y, z);
    }

    /**
     *
     * @return The yaw of this position. NaN indicates that no yaw is set
     */
    public float getYaw() {
        return yaw;
    }

    /**
     *
     * @return The pitch of this position. NaN indicates that no pitch is set
     */
    public float getPitch() {
        return pitch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapPoint mapPoint = (MapPoint) o;
        return Double.compare(mapPoint.x, x) == 0 &&
               Double.compare(mapPoint.y, y) == 0 &&
               Double.compare(mapPoint.z, z) == 0 &&
               Float.compare(mapPoint.yaw, yaw) == 0 &&
               Float.compare(mapPoint.pitch, pitch) == 0 &&
               Objects.equals(world, mapPoint.world);
    }

    @Override
    public int hashCode() {

        return Objects.hash(world, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
