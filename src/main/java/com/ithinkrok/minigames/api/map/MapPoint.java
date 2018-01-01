package com.ithinkrok.minigames.api.map;

import com.ithinkrok.util.config.Config;
import org.bukkit.util.Vector;

public class MapPoint {

    /**
     * The default world name is 'map'
     */
    private final String world;

    private final double x, y, z;
    private final float yaw, pitch;


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
        this.world = config.getString("world", "map");
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

        String name = "map";
        double[] doubles = new double[5];
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

    public static MapPoint fromConfig(Config config, String path) {
        if (config.isString(path)) {
            return new MapPoint(config.getString(path));
        } else if (config.isConfig(path)) {
            return new MapPoint(config.getConfigOrNull(path));
        } else throw new IllegalArgumentException("There is no MapPoint at location " + path + " in config " +
                                                  config.toString());
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

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
