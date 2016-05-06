package com.ithinkrok.minigames.api.schematic;

import org.bukkit.block.BlockFace;

/**
 * Created by paul on 07/11/15.
 * <p>
 * Handles building facing and block rotation
 */
public class Facing {

    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;

    /**
     * Returns the facing direction from player yaw for use in building rotations.
     * NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3
     *
     * @param yaw The yaw of the player
     * @return The rotation for use in SchematicBuilder.buildSchematic()
     */
    public static int getFacing(float yaw) {
        //Use (& 3) instead of (% 4) to prevent returning a negative number
        return (floor((yaw + 45f) / 90f) + 2) & 3;
    }

    private static int floor(float f) {
        int i = (int) f;

        if (i > f) return i - 1;
        return i;
    }

    private static final int STAIR_EAST = 0;
    private static final int STAIR_WEST = 1;

    private static final int STAIR_SOUTH = 2;
    private static final int STAIR_NORTH = 3;

    public static int rotateStairs(int stairRot, int inputRot) {
        int vcRot;

        switch (stairRot) {
            case STAIR_NORTH:
                vcRot = NORTH;
                break;
            case STAIR_EAST:
                vcRot = EAST;
                break;
            case STAIR_SOUTH:
                vcRot = SOUTH;
                break;
            case STAIR_WEST:
                vcRot = WEST;
                break;
            default:
                return stairRot;
        }

        vcRot = (vcRot + inputRot) & 3;

        switch (vcRot) {
            case NORTH:
                return STAIR_NORTH;
            case EAST:
                return STAIR_EAST;
            case SOUTH:
                return STAIR_SOUTH;
            case WEST:
                return STAIR_WEST;
            default:
                return stairRot;
        }
    }

    public static int rotateLadderFurnaceChest(int data, int rot) {
        int readDir;

        switch (data) {
            case 0:
            case 1:
            case 2:
                readDir = 2;
                break;
            case 3:
                readDir = 0;
                break;
            case 4:
                readDir = 3;
                break;
            case 5:
                readDir = 1;
                break;
            default:
                return data;
        }

        readDir = (readDir + rot) % 4;

        switch (readDir) {
            case 0:
                return 2;
            case 1:
                return 5;
            case 2:
                return 3;
            case 3:
                return 4;
            default:
                return data;
        }
    }

    public static int rotateSign(int data, int rot) {
        rot *= 4;

        return (data + rot) & 0xF;
    }

    public static BlockFace signRotationToFacing(int data) {
        switch (data) {
            case 0:
                return BlockFace.SOUTH;
            case 1:
                return BlockFace.SOUTH_SOUTH_WEST;
            case 2:
                return BlockFace.SOUTH_WEST;
            case 3:
                return BlockFace.WEST_SOUTH_WEST;
            case 4:
                return BlockFace.WEST;
            case 5:
                return BlockFace.WEST_NORTH_WEST;
            case 6:
                return BlockFace.NORTH_WEST;
            case 7:
                return BlockFace.NORTH_NORTH_WEST;
            case 8:
                return BlockFace.NORTH;
            case 9:
                return BlockFace.NORTH_NORTH_EAST;
            case 10:
                return BlockFace.NORTH_EAST;
            case 11:
                return BlockFace.EAST_NORTH_EAST;
            case 12:
                return BlockFace.EAST;
            case 13:
                return BlockFace.EAST_SOUTH_EAST;
            case 14:
                return BlockFace.SOUTH_EAST;
            case 15:
                return BlockFace.SOUTH_SOUTH_EAST;
            default:
                throw new IllegalArgumentException("data must be 0-15");
        }
    }

    public static int rotateLogs(int data, int rotation) {
        if (data == 0 || data == 12 || (rotation % 2) == 0) return data;

        if (data == 4) return 8;
        else return 4;
    }
}
