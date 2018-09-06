package com.ithinkrok.minigames.api.map;

import com.ithinkrok.util.config.Config;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * Immutable class representing a point in a world of a Map, with an optional orientation.
 *
 */
public class MapPoint {

    /**
     * The default world name is 'map'
     */
    private final String world;

    private final double x, y, z;
    private final float yaw, pitch;

    public MapPoint(String world, Vector xyz) {
        this(world, xyz.getX(), xyz.getY(), xyz.getZ());
    }

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
        return "MapPoint{" +
               "world='" + world + '\'' +
               ", x=" + x +
               ", y=" + y +
               ", z=" + z +
               ", yaw=" + yaw +
               ", pitch=" + pitch +
               '}';
    }


    /**
     * Gets a new MapPoint with this MapPoint's position added to the provided x,y,z arguments.
     *
     * @param x X coord
     * @param y Y coord
     * @param z Z coord
     * @return A new MapPoint with the modified position
     */
    public MapPoint add(double x, double y, double z) {
        return new MapPoint(world, this.x + x, this.y + y, this.z + z, this.yaw, this.pitch);
    }


    /**
     * Gets a new MapPoint with the world set to the provided world field.
     *
     * @param world The new world name
     * @return A new map point representing this position in a different world.
     */
    public MapPoint setWorld(String world) {
        return new MapPoint(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }


    /**
     * Gets a new MapPoint with this MapPoint's orientation and world and the provided xyz position.
     *
     * @param x X coord
     * @param y Y coord
     * @param z Z coord
     * @return A new MapPoint with the XYZ position replaced with the new one specified
     */
    public MapPoint setXYZ(double x, double y, double z) {
        return new MapPoint(this.world, x, y, z, this.yaw, this.pitch);
    }


    /**
     * Computes the component-wise minimum position of this MapPoint and the provided one, and returns a new MapPoint
     * with that position and the world and orientation of this MapPoint.
     *
     * Does NOT check if the worlds are the same.
     *
     * @param other The other point to check
     * @return A new map point with the component-wise minimum position of this and the provided point, otherwise the
     * same.
     */
    public MapPoint min(MapPoint other) {
        double x = Math.min(this.getX(), other.getX());
        double y = Math.min(this.getY(), other.getY());
        double z = Math.min(this.getZ(), other.getZ());

        return setXYZ(x, y, z);
    }

    /**
     * Computes the component-wise maximum position of this MapPoint and the provided one, and returns a new MapPoint
     * with that position and the world and orientation of this MapPoint.
     *
     * Does NOT check if the worlds are the same.
     *
     * @param other The other point to check
     * @return A new map point with the component-wise maximum position of this and the provided point, otherwise the
     * same.
     */
    public MapPoint max(MapPoint other) {
        double x = Math.max(this.getX(), other.getX());
        double y = Math.max(this.getY(), other.getY());
        double z = Math.max(this.getZ(), other.getZ());

        return setXYZ(x, y, z);
    }


    /**
     * Computes the square of the distance to point other, or returns INFINITY if other is in a different world
     *
     * @param other The point to compute square distance to
     * @return The distance
     */
    public double distanceSquared(MapPoint other) {
        if(!Objects.equals(world, other.world)) return Double.POSITIVE_INFINITY;

        double dx = other.x - x;
        double dy = other.y - y;
        double dz = other.z - z;

        return dx * dx + dy * dy + dz * dz;
    }
}
