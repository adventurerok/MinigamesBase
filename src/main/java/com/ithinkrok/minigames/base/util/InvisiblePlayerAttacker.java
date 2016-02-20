package com.ithinkrok.minigames.base.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.ithinkrok.minigames.api.Game;
import com.ithinkrok.minigames.api.user.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 * Created by paul on 08/11/15.
 * <p>
 * Enables people to attack cloaked cloakers
 */
public class InvisiblePlayerAttacker {

    public static void enablePlayerAttacker(Game game, Plugin plugin, ProtocolManager protocolManager) {


        protocolManager.getAsynchronousManager().registerAsyncHandler(new PacketAdapter(
                new PacketAdapter.AdapterParameteters().plugin(plugin).clientSide()
                        .types(PacketType.Play.Client.ARM_ANIMATION)) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                final int ATTACK_REACH = 4;

                Player observer = event.getPlayer();
                Location observerPos = observer.getEyeLocation();
                Vector3D observerDir = new Vector3D(observerPos.getDirection());

                Vector3D observerStart = new Vector3D(observerPos);
                Vector3D observerEnd = observerStart.add(observerDir.multiply(ATTACK_REACH));

                Player hit = null;

                // Get nearby entities
                for (Player target : protocolManager.getEntityTrackers(observer)) {
                    // No need to simulate an attack if the player is already visible
                    if (!observer.canSee(target)) {
                        User user = game.getUser(target.getUniqueId());
                        if(user == null || !user.isInGame()) continue;

                        // Bounding box of the given player
                        Vector3D targetPos = new Vector3D(target.getLocation());
                        Vector3D minimum = targetPos.add(-0.5, 0, -0.5);
                        Vector3D maximum = targetPos.add(0.5, 1.67, 0.5);

                        if (hasIntersection(observerStart, observerEnd, minimum, maximum)) {
                            if (hit == null || hit.getLocation().distanceSquared(observerPos) >
                                    target.getLocation().distanceSquared(observerPos)) {
                                hit = target;
                            }
                        }
                    }
                }

                // Simulate a hit against the closest player
                if (hit != null) {
                    PacketContainer useEntity = new PacketContainer(PacketType.Play.Client.USE_ENTITY);
                    useEntity.getIntegers().
                            write(0, hit.getEntityId());

                    useEntity.getEntityUseActions().write(0, EnumWrappers.EntityUseAction.ATTACK);


                    try {
                        protocolManager.recieveClientPacket(event.getPlayer(), useEntity);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Get entity trackers is not thread safe
        }).syncStart();
    }

    private static boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max) {
        final double epsilon = 0.0001f;

        Vector3D d = p2.subtract(p1).multiply(0.5);
        Vector3D e = max.subtract(min).multiply(0.5);
        Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
        Vector3D ad = d.abs();

        //IntelliJ wanted me to "simplify" the if statements

        return Math.abs(c.x) <= e.x + ad.x && Math.abs(c.y) <= e.y + ad.y && Math.abs(c.z) <= e.z + ad.z &&
                Math.abs(d.y * c.z - d.z * c.y) <= e.y * ad.z + e.z * ad.y + epsilon &&
                Math.abs(d.z * c.x - d.x * c.z) <= e.z * ad.x + e.x * ad.z + epsilon &&
                Math.abs(d.x * c.y - d.y * c.x) <= e.x * ad.y + e.y * ad.x + epsilon;

    }

    private static class Vector3D {

        // Use protected members, like Bukkit
        public final double x;
        public final double y;
        public final double z;

        /**
         * Construct an immutable 3D vector.
         */
        public Vector3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Construct an immutable floating point 3D vector from a location object.
         *
         * @param location - the location to copy.
         */
        public Vector3D(Location location) {
            this(location.toVector());
        }

        /**
         * Construct an immutable floating point 3D vector from a mutable Bukkit vector.
         *
         * @param vector - the mutable real Bukkit vector to copy.
         */
        public Vector3D(Vector vector) {
            if (vector == null) throw new IllegalArgumentException("Vector cannot be NULL.");
            this.x = vector.getX();
            this.y = vector.getY();
            this.z = vector.getZ();
        }

        /**
         * Adds the current vector and a given position vector, producing a result vector.
         *
         * @param other - the other vector.
         * @return The new result vector.
         */
        public Vector3D add(Vector3D other) {
            if (other == null) throw new IllegalArgumentException("other cannot be NULL");
            return new Vector3D(x + other.x, y + other.y, z + other.z);
        }

        /**
         * Adds the current vector and a given vector together, producing a result vector.
         *
         * @return The new result vector.
         */
        public Vector3D add(double x, double y, double z) {
            return new Vector3D(this.x + x, this.y + y, this.z + z);
        }

        /**
         * Substracts the current vector and a given vector, producing a result position.
         *
         * @param other - the other position.
         * @return The new result position.
         */
        public Vector3D subtract(Vector3D other) {
            if (other == null) throw new IllegalArgumentException("other cannot be NULL");
            return new Vector3D(x - other.x, y - other.y, z - other.z);
        }

        /**
         * Substracts the current vector and a given vector together, producing a result vector.
         *
         * @return The new result vector.
         */
        public Vector3D subtract(double x, double y, double z) {
            return new Vector3D(this.x - x, this.y - y, this.z - z);
        }

        /**
         * Multiply each dimension in the current vector by the given factor.
         *
         * @param factor - multiplier.
         * @return The new result.
         */
        public Vector3D multiply(int factor) {
            return new Vector3D(x * factor, y * factor, z * factor);
        }

        /**
         * Multiply each dimension in the current vector by the given factor.
         *
         * @param factor - multiplier.
         * @return The new result.
         */
        public Vector3D multiply(double factor) {
            return new Vector3D(x * factor, y * factor, z * factor);
        }

        /**
         * Retrieve the absolute value of this vector.
         *
         * @return The new result.
         */
        public Vector3D abs() {
            return new Vector3D(Math.abs(x), Math.abs(y), Math.abs(z));
        }

        @Override
        public String toString() {
            return String.format("[x: %s, y: %s, z: %s]", x, y, z);
        }
    }
}
