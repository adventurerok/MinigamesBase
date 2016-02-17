package com.ithinkrok.minigames.base.hub;

import com.ithinkrok.minigames.base.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.base.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.base.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 16/02/16.
 */
public class HubSign {

    private final Location location;

    private final String gameGroupType;

    private boolean spectatorSign = false;

    public HubSign(SignChangeEvent event) {
        location = event.getBlock().getLocation();

        gameGroupType = event.getLine(1);

        spectatorSign = event.getLine(2).equalsIgnoreCase("spectators");
    }

    public HubSign(Server server, Config config) {
        gameGroupType = config.getString("type");
        spectatorSign = config.getBoolean("spectators");

        int x = config.getInt("x");
        int y = config.getInt("y");
        int z = config.getInt("z");
        String worldName = config.getString("world");

        World world = server.getWorld(worldName);
        if(world == null) throw new RuntimeException("Unknown world " + worldName);

        location = new Location(world, x, y, z);
    }

    public void update(ControllerInfo controller) {
        if (spectatorSign) updateSpectatorSign(controller);
        else updateLobbySign(controller);
    }

    public String getGameGroupType() {
        return gameGroupType;
    }

    private void updateSpectatorSign(ControllerInfo controller) {
        Sign sign = (Sign) location.getBlock().getState();

        sign.setLine(0, ChatColor.GRAY + "[" + ChatColor.DARK_AQUA +
                WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')) + ChatColor.GRAY + "]");

        Collection<GameGroupInfo> all = controller.getGameGroups(gameGroupType);

        if (all.isEmpty()) {
            sign.setLine(1, "");
            sign.setLine(2, ChatColor.RED + "No Games");
            sign.setLine(3, "");
        } else {
            sign.setLine(1, ChatColor.RED + "Spectate Games");
            sign.setLine(2, ChatColor.DARK_GRAY.toString() + all.size() + ChatColor.BLACK + " games available");
            sign.setLine(3, ChatColor.BLUE + "Right click chose");
        }

        sign.update();
    }

    private void updateLobbySign(ControllerInfo controller) {
        Sign sign = (Sign) location.getBlock().getState();

        sign.setLine(0, ChatColor.GRAY + "[" + ChatColor.DARK_AQUA +
                WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')) + ChatColor.GRAY + "]");

        Collection<GameGroupInfo> accepting = controller.getAcceptingGameGroups(gameGroupType);

        if (accepting.isEmpty()) {
            sign.setLine(1, "");
            sign.setLine(2, ChatColor.RED + "Create new Lobby");
            sign.setLine(3, "");
        } else {
            sign.setLine(1, ChatColor.RED + "Join Lobby");

            GameGroupInfo bestMatch = null;

            for (GameGroupInfo gameGroupInfo : accepting) {
                if (bestMatch != null && gameGroupInfo.getPlayerCount() <= bestMatch.getPlayerCount()) continue;

                bestMatch = gameGroupInfo;
            }

            //IntelliJ wanted me to add this to stop a warning. This will never be true
            if (bestMatch == null) return;

            sign.setLine(2,
                    ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + bestMatch.getPlayerCount() + ChatColor.DARK_GRAY +
                            "/" + ChatColor.GRAY +
                            bestMatch.getMaxPlayerCount() + ChatColor.DARK_GRAY + "]");
            sign.setLine(3, bestMatch.getMotd());
        }

        sign.update();
    }

    public void onRightClick(Hub hub, Player player) {
        if (spectatorSign) onRightClickSpectator(hub, player);
        else onRightClickLobby(hub, player);
    }

    private void onRightClickSpectator(Hub hub, Player player) {
        Inventory inventory = Bukkit.createInventory(player, 36,
                WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')) + " Games");

        updateSpectatorInventory(hub, inventory);

        player.openInventory(inventory);

        hub.addOpenSpectatorInventory(player, location);
    }

    public void updateSpectatorInventory(Hub hub, Inventory inventory) {
        inventory.clear();

        Collection<GameGroupInfo> allCollection = hub.getControllerInfo().getGameGroups(gameGroupType);

        List<GameGroupInfo> all = new ArrayList<>(allCollection);

        Collections.sort(all, (o1, o2) -> {
            if (o1.isAcceptingPlayers() && !o2.isAcceptingPlayers()) return 1;
            else if (o2.isAcceptingPlayers() && !o1.isAcceptingPlayers()) return -1;
            return o1.getName().compareTo(o2.getName());
        });

        for (GameGroupInfo gameGroup : all) {
            Material mat = gameGroup.isAcceptingPlayers() ? Material.GOLD_BLOCK : Material.IRON_BLOCK;

            List<String> lore = new ArrayList<>();

            if (gameGroup.isAcceptingPlayers()) {
                lore.add("Join Lobby");
            } else {
                lore.add("Spectate Game");
            }

            lore.add(gameGroup.getMotd());

            lore.add("Type: " + gameGroupType);
            lore.add("Players: [" + gameGroup.getPlayerCount() + "/" + gameGroup.getMaxPlayerCount() + "]");

            ItemStack item = InventoryUtils
                    .createItemWithNameAndLore(mat, 1, 0, gameGroup.getName(), lore.toArray(new String[lore.size()]));

            inventory.addItem(item);
        }
    }

    private void onRightClickLobby(Hub hub, Player player) {
        Collection<GameGroupInfo> accepting = hub.getControllerInfo().getAcceptingGameGroups(gameGroupType);

        GameGroupInfo bestMatch = null;

        for (GameGroupInfo gameGroupInfo : accepting) {
            if (bestMatch != null && gameGroupInfo.getPlayerCount() < bestMatch.getPlayerCount()) continue;

            bestMatch = gameGroupInfo;
        }

        String gameGroupName = bestMatch != null ? bestMatch.getName() : null;

        if (gameGroupName != null) player.sendMessage("Sending you to gamegroup: " + gameGroupName);
        else player.sendMessage("Creating a new " + gameGroupType + " gamegroup for you");

        hub.getRequestProtocol().sendJoinGameGroupPacket(player.getUniqueId(), gameGroupType, gameGroupName);
    }

    public Location getLocation() {
        return location;
    }

    public Config toConfig() {
        Config config = new MemoryConfig();

        config.set("type", gameGroupType);
        config.set("spectators", spectatorSign);
        config.set("x", location.getBlockX());
        config.set("y", location.getBlockY());
        config.set("z", location.getBlockZ());
        config.set("world", location.getWorld().getName());

        return config;
    }
}
