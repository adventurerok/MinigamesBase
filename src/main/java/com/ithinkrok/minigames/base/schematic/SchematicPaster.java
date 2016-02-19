package com.ithinkrok.minigames.base.schematic;

import com.ithinkrok.minigames.base.map.GameMap;
import com.ithinkrok.minigames.base.schematic.blockentity.BlockEntity;
import com.ithinkrok.minigames.base.task.GameRunnable;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.task.TaskScheduler;
import com.ithinkrok.minigames.base.util.BoundingBox;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by paul on 07/01/16.
 */
public class SchematicPaster {

    private static final DecimalFormat percentFormat = new DecimalFormat("00%");

    public static PastedSchematic pasteSchematic(Schematic schemData, GameMap map, Location loc,
                                                 BoundsChecker boundsChecker, SchematicResolver schematicResolver,
                                                 int rotation, SchematicOptions options) {
        return buildSchematic(schemData, map, loc, boundsChecker, null, schematicResolver, rotation, options);
    }

    public static PastedSchematic buildSchematic(Schematic schemData, GameMap map, Location loc,
                                                 BoundsChecker boundsChecker, TaskScheduler taskScheduler,
                                                 SchematicResolver schematicResolver, int rotation,
                                                 SchematicOptions options) {
        SchematicRotation schem = schemData.getSchematicRotation(rotation);

        BoundingBox bounds = schem.calcBounds(schematicResolver, loc);

        if (!schemData.getAllowOverlap()) {
            if (boundsChecker != null && !boundsChecker.canPaste(bounds)) return null;
            if (options.doMapBoundsCheck() && !map.canPaste(bounds)) return null;
        }

        List<Location> locations = new ArrayList<>();
        Location centerBlock = null;
        HashMap<Location, BlockState> oldBlocks = new HashMap<>();

        BlockState oldState;

        for (int x = 0; x < schem.getWidth(); ++x) {
            for (int y = 0; y < schem.getHeight(); ++y) {
                for (int z = 0; z < schem.getLength(); ++z) {
                    Location l = new Location(loc.getWorld(), x + loc.getX() + schem.getOffsetX(),
                            y + loc.getY() + schem.getOffsetY(), z + loc.getZ() + schem.getOffsetZ());

                    oldState = l.getBlock().getState();

                    int bId = schem.getBlock(x, y, z);
                    if (bId == 0) continue;

                    if (bId == options.getCenterBlockType().getId()) centerBlock = l;

                    locations.add(l);

                    oldBlocks.put(l, oldState);
                }
            }
        }

        Collections.sort(locations, (o1, o2) -> {
            if (o1.getY() != o2.getY()) return Double.compare(o1.getY(), o2.getY());
            if (o1.getX() != o2.getX()) return Double.compare(o1.getX(), o2.getX());

            return Double.compare(o1.getZ(), o2.getZ());
        });

        PastedSchematic result = new PastedSchematic(schemData.getName(), schemData, map, centerBlock, bounds, rotation,
                schemData.getAllowOverlap(), locations, oldBlocks);
        result.addListeners(options.getDefaultListeners());

        SchematicBuilderTask builderTask = new SchematicBuilderTask(loc, result, schem, options);

        if (taskScheduler != null) {
            result.setBuildTask(builderTask.schedule(taskScheduler));
        } else {
            int oldBuildSpeed = options.getBuildSpeed();
            options.withBuildSpeed(-1);
            builderTask.run(null);
            options.withBuildSpeed(oldBuildSpeed);
        }

        return result;
    }

    public static byte rotateData(Material type, int rotation, byte data) {
        switch (type) {
            case ACACIA_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case BRICK_STAIRS:
            case COBBLESTONE_STAIRS:
            case DARK_OAK_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case NETHER_BRICK_STAIRS:
            case QUARTZ_STAIRS:
            case RED_SANDSTONE_STAIRS:
            case SANDSTONE_STAIRS:
            case SMOOTH_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case WOOD_STAIRS:
                return (byte) ((data & 0x4) | Facing.rotateStairs(data & 3, rotation));
            case LADDER:
            case CHEST:
            case TRAPPED_CHEST:
            case FURNACE:
            case WALL_SIGN:
                return (byte) Facing.rotateLadderFurnaceChest(data, rotation);
            case LOG:
            case LOG_2:
                return (byte) ((data & 3) | Facing.rotateLogs(data & 12, rotation));
            default:
                return data;
        }

    }

    public interface BoundsChecker {
        boolean canPaste(BoundingBox bounds);
    }

    private static class SchematicBuilderTask implements GameRunnable {

        int index = 0;

        Location origin;
        Hologram hologram;
        private PastedSchematic building;
        private final SchematicRotation schem;
        private final SchematicOptions options;

        private boolean clearedOrigin = false;

        public SchematicBuilderTask(Location origin, PastedSchematic building, SchematicRotation schem,
                                    SchematicOptions options) {
            this.origin = origin;
            this.building = building;
            this.schem = schem;
            this.options = options;

            if (options.getProgressHologram()) {
                Location holoLoc;
                if (building.getCenterBlock() != null)
                    holoLoc = building.getCenterBlock().clone().add(0.5d, 1.5d, 0.5d);
                else holoLoc = origin.clone().add(0.5d, 1.5d, 0.5d);

                hologram = HologramAPI.createHologram(holoLoc, "Building: 0%");

                hologram.spawn();

                building.addHologram(hologram);
            }
        }

        @Override
        public void run(GameTask task) {
            int count = 0;

            if (!clearedOrigin) {
                origin.getBlock().setType(Material.AIR);
                clearedOrigin = true;
            }

            List<Location> locations = building.getBuildingBlocks();

            while (index < locations.size()) {
                Location loc = locations.get(index);

                int x = loc.getBlockX() - origin.getBlockX() - schem.getOffsetX();
                int y = loc.getBlockY() - origin.getBlockY() - schem.getOffsetY();
                int z = loc.getBlockZ() - origin.getBlockZ() - schem.getOffsetZ();


                Material mat = Material.getMaterial(schem.getBlock(x, y, z));
                byte bData = schem.getData(x, y, z);

                Block block = loc.getBlock();


                Material replaceWith = options.getReplaceMaterials().get(mat);
                if (replaceWith != null) mat = replaceWith;

                if (options.getOverrideDyeColor() != null) {
                    if (mat == Material.WOOL || mat == Material.STAINED_CLAY || mat == Material.STAINED_GLASS ||
                            mat == Material.STAINED_GLASS_PANE) {
                        bData = options.getOverrideDyeColor().getWoolData();
                    }
                }

                block.setTypeIdAndData(mat.getId(), rotateData(mat, schem.getRotation(), bData), false);

                BlockEntity blockEntity = schem.getBlockEntity(x, y, z);
                if(blockEntity != null) {
                    blockEntity.paste(block, schem.getRotation());
                }

                ++index;

                ++count;
                if (options.getBuildSpeed() != -1 && count > options.getBuildSpeed()) {
                    loc.getWorld().playEffect(loc, Effect.STEP_SOUND, mat);
                    if (options.getProgressHologram()) {
                        hologram.setText(
                                "Building: " + percentFormat.format((double) index / (double) locations.size()));
                    }
                    return;
                }
            }

            building.setBuildTask(null);
            if (task != null) task.finish();

            if (options.getProgressHologram()) {
                HologramAPI.removeHologram(hologram);
                building.removeHologram(hologram);
            }

            if (building.getCenterBlock() != null) {
                building.getCenterBlock().getWorld().playSound(building.getCenterBlock(), Sound.LEVEL_UP, 1.0f, 1.0f);
            }

            building.setFinished();

            building = null;
        }

        public GameTask schedule(TaskScheduler scheduler) {
            return scheduler.repeatInFuture(this, 1, 1);
        }
    }
}
