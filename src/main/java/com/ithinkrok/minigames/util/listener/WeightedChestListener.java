package com.ithinkrok.minigames.util.listener;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.Nameable;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.EntityUtils;
import com.ithinkrok.minigames.api.inventory.WeightedInventory;
import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by paul on 22/12/16.
 */
public class WeightedChestListener implements CustomListener {

    private final Set<Location> openedChests = new HashSet<>();

    protected WeightedInventory chestInventory;
    protected WeightedInventory enderInventory;

    /**
     * Chests within this distance from the map spawn will have the ender inventory.
     */
    protected double cornucopiaRange;

    /**
     * Seconds before reset
     */
    protected int chestDuration;

    private String ourGameState = null;
    private String ourMap = null;

    private Vector mapCenter;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, ?> event) {

        Config config = event.getConfigOrEmpty();

        chestDuration = config.getInt("chest_duration", 240) * 20;

        chestInventory = new WeightedInventory(event.getCreator(), config.getConfigOrNull("inventories.chest"));

        Calculator enderBalance = new ExpressionCalculator(config.getString("inventories.ender_chest.adjust_balance"));
        Calculator enderChance = new ExpressionCalculator(config.getString("inventories.ender_chest.base_chance", "1"));
        Calculator enderExtra = new ExpressionCalculator(config.getString("inventories.ender_chest.extra_mod", "0"));

        enderInventory = chestInventory.adjust(enderChance, enderBalance, enderExtra);

        cornucopiaRange = config.getInt("cornucopia_radius", 0);

        if (event.getRepresenting() instanceof GameState) {
            ourGameState = ((Nameable) event.getRepresenting()).getName();
        } else if (event.getRepresenting() instanceof GameMap) {
            ourMap = ((GameMap) event.getRepresenting()).getInfo().getName();
        }
    }

    @CustomEventHandler(priority = CustomEventHandler.HIGH)
    public void onGameStateChanged(GameStateChangedEvent event) {
        if (ourGameState == null || !event.getNewGameState().getName().equals(ourGameState)) {
            return;
        }

        resetEnderInventories(event.getGameGroup());
    }

    private void resetEnderInventories(GameGroup gameGroup) {
        for (User user : gameGroup.getUsers()) {
            Inventory ender = user.getEnderInventory();

            ender.clear();
            enderInventory.populateInventory(ender);
        }
    }

    @CustomEventHandler
    public void onGameMapChanged(MapChangedEvent event) {
        Config spawns = event.getGameGroup().getSharedObject("spawn_info");
        if (spawns != null) {
            mapCenter = BukkitConfigUtils.getVector(spawns, "map_center");
        } else {
            mapCenter = event.getNewMap().getSpawn().toVector();
        }

        if (ourMap == null || !event.getNewMap().getInfo().getName().equals(ourMap)) {
            return;
        }

        resetEnderInventories(event.getGameGroup());
    }

    @CustomEventHandler(ignoreCancelled = true)
    public void onInteract(UserInteractEvent event) {
        if (!event.getUser().isInGame()) {
            event.setCancelled(true);
            return;
        }

        boolean rightClick = event.getInteractType() == UserInteractEvent.InteractType.RIGHT_CLICK;

        if (rightClick && event.hasBlock() && event.getClickedBlock().getType() == Material.CHEST) {
            handleChestOpen(event);
        } else if (rightClick && event.hasEntity() && event.hasItem() &&
                event.getItem().getType() == Material.FLINT_AND_STEEL) {
            User user = EntityUtils.getActualUser(event.getGameGroup(), event.getClickedEntity());

            if (user != null) {
                user.setFireTicks(event.getUser(), 100);
            } else {
                event.getClickedEntity().setFireTicks(100);
            }

            //Damage flint and steel when used
            int newDurability = event.getItem().getDurability() + 10;

            if (newDurability > event.getItem().getType().getMaxDurability()) {
                event.getUser().getInventory().setItemInHand(null);
            } else {
                event.getItem().setDurability((short) newDurability);
            }
        }
    }

    public void handleChestOpen(UserInteractEvent event) {
        if (openedChests.contains(event.getClickedBlock().getLocation())) return;

        Chest chest = (Chest) event.getClickedBlock().getState();
        Inventory inventory = chest.getSnapshotInventory();

        WeightedInventory weightedInventory = getInventoryForBlock(event.getClickedBlock());

        inventory.clear();
        weightedInventory.populateInventory(inventory);

        chest.update();

        Collection<Location> removeLocations = new HashSet<>();

        openedChests.add(event.getClickedBlock().getLocation());

        if(inventory instanceof DoubleChestInventory) {
            BlockState leftSide = (BlockState) ((DoubleChestInventory) inventory).getLeftSide().getHolder();
            BlockState rightSide = (BlockState) ((DoubleChestInventory) inventory).getRightSide().getHolder();

            openedChests.add(leftSide.getLocation());
            openedChests.add(rightSide.getLocation());

            removeLocations.add(leftSide.getLocation());
            removeLocations.add(rightSide.getLocation());
        }

        if (chestDuration > 0) {
            GameTask task = event.getGameGroup().doInFuture(task1 -> {
                openedChests.removeAll(removeLocations);
            }, chestDuration);

            event.getGameGroup().bindTaskToCurrentGameState(task);
        }
    }

    protected WeightedInventory getInventoryForBlock(Block block) {
        if (mapCenter != null && block.getLocation().toVector().distance(mapCenter) < cornucopiaRange) {
            return enderInventory;
        } else {
            return chestInventory;
        }
    }


}
