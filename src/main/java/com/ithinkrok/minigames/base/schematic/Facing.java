package com.ithinkrok.minigames.base.schematic;

/**
 * Created by paul on 07/11/15.
 *
 * Handles building facing and block rotation
 *
 */
public class Facing {

    /**
     * Returns the facing direction from player yaw for use in building rotations.
     * NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3
     *
     * @param yaw The yaw of the player
     * @return The rotation for use in SchematicBuilder.buildSchematic()
     */
    public static int getFacing(float yaw){
        return (floor((yaw + 45f) / 90f) + 2) % 4;
    }

    private static int floor(float f){
        int i = (int) f;

        if(i > f) return i - 1;
        return i;
    }

    public static int rotateStairs(int data, int rot){
        int readDir;

        switch (data){
            case 0:
                readDir = 1;
                break;
            case 1:
                readDir = 3;
                break;
            case 2:
                readDir = 0;
                break;
            case 3:
                readDir = 2;
                break;
            default:
                return data;
        }

        readDir = (readDir + rot) % 4;

        switch (readDir){
            case 0:
                return 3;
            case 1:
                return 0;
            case 2:
                return 2;
            case 3:
                return 1;
            default:
                return data;
        }
    }

    public static int rotateLadderFurnaceChest(int data, int rot){
        int readDir;

        switch (data){
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

        switch (readDir){
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

    public static int rotateLogs(int data, int rotation) {
        if(data == 0 || data == 12 || (rotation % 2) == 0) return data;

        if(data == 4) return 8;
        else return 4;
    }
}
