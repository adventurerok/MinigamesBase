package com.ithinkrok.minigames.api.schematic;

import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.schematic.event.SchematicDestroyedEvent;
import com.ithinkrok.minigames.api.schematic.event.SchematicFinishedEvent;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.util.BoundingBox;
import com.ithinkrok.minigames.api.util.NamedSounds;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventExecutor;
import com.ithinkrok.util.event.CustomListener;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by paul on 07/01/16.
 */
public class PastedSchematic implements SchematicPaster.BoundsChecker {

    private static final Random random = new Random();
    private static long nextIdentifier = 0;
    private final long identifier = nextIdentifier++;

    private final String name;
    private final List<Location> buildingBlocks;
    private final Map<Location, BlockState> oldBlocks;
    private final Location centerBlock;
    private final BoundingBox bounds;
    private final int rotation;
    private final GameMap map;
    private final List<CustomListener> listeners = new ArrayList<>();
    private final List<Hologram> holograms = new ArrayList<>();
    private final Schematic schematic;
    private boolean finished;
    private boolean removed;
    private GameTask buildTask;
    private boolean allowOverlap = false;

    public PastedSchematic(String name, Schematic schematic, GameMap map, Location centerBlock, BoundingBox bounds,
                           int rotation, boolean allowOverlap, List<Location> buildingBlocks,
                           Map<Location, BlockState> oldBlocks) {
        this.name = name;
        this.schematic = schematic;
        this.map = map;
        this.centerBlock = centerBlock;
        this.bounds = bounds;
        this.rotation = rotation;
        this.allowOverlap = allowOverlap;
        this.buildingBlocks = buildingBlocks;
        this.oldBlocks = oldBlocks;

        map.addPastedSchematic(this);
    }

    public Schematic getSchematic() {
        return schematic;
    }

    public Config getConfig() {
        return schematic.getConfig();
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public String getName() {
        return name;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getRotation() {
        return rotation;
    }

    public void addListeners(Collection<? extends CustomListener> listeners) {
        this.listeners.addAll(listeners);
    }

    public void setFinished() {
        this.finished = true;

        SchematicFinishedEvent destroyedEvent = new SchematicFinishedEvent(this);
        CustomEventExecutor.executeEvent(destroyedEvent, listeners);
    }

    public List<Location> getBuildingBlocks() {
        return buildingBlocks;
    }

    public Location getCenterBlock() {
        return centerBlock;
    }

    @Override
    public boolean canPaste(BoundingBox bounds) {
        return allowOverlap || !this.bounds.intercepts(bounds);
    }

    public void addHologram(Hologram hologram) {
        holograms.add(hologram);
    }

    public void removeHologram(Hologram hologram) {
        holograms.remove(hologram);
    }

    public void explode() {
        removed();

        if (centerBlock != null) {
            centerBlock.getWorld().playSound(centerBlock, NamedSounds.fromName("ENTITY_GENERIC_EXPLODE"), 1.0f, 1.0f);
        }

        if(!buildingBlocks.isEmpty()) {
            //To prevent too many falling blocks being created, which causes lag
            double fallingBlockChance = Math.min(1, 30 / (double) buildingBlocks.size());

            for (Location loc : buildingBlocks) {
                if (loc.equals(centerBlock)) continue;

                Block b = loc.getBlock();
                if (b.getType() == Material.AIR) continue;

                Material oldType = b.getType();
                byte oldData = b.getData();

                b.setType(Material.AIR);

                if (!oldType.isSolid()) continue;

                if(random.nextDouble() > fallingBlockChance) continue;

                FallingBlock block = loc.getWorld().spawnFallingBlock(loc, oldType, oldData);
                float xv = -0.3f + (random.nextFloat() * 0.6f);
                float yv = (random.nextFloat() * 0.5f);
                float zv = -0.3f + (random.nextFloat() * 0.6f);

                block.setVelocity(new Vector(xv, yv, zv));
            }
        }

        SchematicDestroyedEvent destroyedEvent = new SchematicDestroyedEvent(this);
        CustomEventExecutor.executeEvent(destroyedEvent, listeners);
    }

    public void removed() {
        map.removePastedSchematic(this);

        if (buildTask != null && buildTask.getTaskState() == GameTask.TaskState.SCHEDULED) buildTask.cancel();

        holograms.forEach(HologramAPI::removeHologram);

        removed = true;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void remove() {
        removed();

        for (Location loc : buildingBlocks) {
            if (loc.equals(centerBlock)) continue;

            oldBlocks.get(loc).update(true, false);
        }

        SchematicDestroyedEvent destroyedEvent = new SchematicDestroyedEvent(this);
        CustomEventExecutor.executeEvent(destroyedEvent, listeners);
        if (centerBlock != null) oldBlocks.get(centerBlock).update(true, false);
    }

    public GameTask getBuildTask() {
        return buildTask;
    }

    public void setBuildTask(GameTask buildTask) {
        this.buildTask = buildTask;
    }

    @Override
    public int hashCode() {
        int result = (int) (identifier ^ (identifier >>> 32));
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PastedSchematic that = (PastedSchematic) o;

        if (identifier != that.identifier) return false;
        return name.equals(that.name);

    }

    @Override
    public String toString() {
        return "PastedSchematic{" +
                "name='" + name + '\'' +
                ", identifier=" + identifier +
                ", finished=" + finished +
                '}';
    }
}
