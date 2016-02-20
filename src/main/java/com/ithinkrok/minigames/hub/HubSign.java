package com.ithinkrok.minigames.hub;

import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.hub.HubListener;
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

    public HubSign(UserEditSignEvent event) {
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
        if (world == null) throw new RuntimeException("Unknown world " + worldName);

        location = new Location(world, x, y, z);
    }

    public boolean update(ControllerInfo controller) {
        Material mat = location.getBlock().getType();
        if(mat != Material.SIGN_POST && mat != Material.WALL_SIGN) return false;

        if (spectatorSign) updateSpectatorSign(controller);
        else updateLobbySign(controller);

        return true;
    }

    private void updateSpectatorSign(ControllerInfo controller) {
        Sign sign = (Sign) location.getBlock().getState();

        sign.setLine(0, ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA +
                WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')) + ChatColor.DARK_GRAY + "]");

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

        sign.setLine(0, ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA +
                WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')) + ChatColor.DARK_GRAY + "]");

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

    public String getGameGroupType() {
        return gameGroupType;
    }

    public void onRightClick(HubListener hub, User user) {
        if (spectatorSign) onRightClickSpectator(hub, user);
        else onRightClickLobby(hub, user);
    }

    private void onRightClickSpectator(HubListener hub, User user) {
        updateSpectatorInventory(hub, user);

        hub.addOpenSpectatorInventory(user, location);
    }

    private void onRightClickLobby(HubListener hub, User user) {
        Collection<GameGroupInfo> accepting = hub.getControllerInfo().getAcceptingGameGroups(gameGroupType);

        GameGroupInfo bestMatch = null;

        for (GameGroupInfo gameGroupInfo : accepting) {
            if (bestMatch != null && gameGroupInfo.getPlayerCount() < bestMatch.getPlayerCount()) continue;

            bestMatch = gameGroupInfo;
        }

        String gameGroupName = bestMatch != null ? bestMatch.getName() : null;

        if (gameGroupName != null) user.sendMessage("Sending you to gamegroup: " + gameGroupName);
        else user.sendMessage("Creating a new " + gameGroupType + " gamegroup for you");

        hub.getRequestProtocol().sendJoinGameGroupPacket(user.getUuid(), gameGroupType, gameGroupName);
    }

    public void updateSpectatorInventory(HubListener hub, User user) {
        ClickableInventory inventory = new ClickableInventory("Title");

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
                lore.add(ChatColor.GREEN + "Join Lobby");
            } else {
                lore.add(ChatColor.RED + "Spectate Game");
            }

            lore.add(ChatColor.WHITE + gameGroup.getMotd());

            lore.add(ChatColor.RED + "Type: " + ChatColor.GRAY + gameGroupType);
            lore.add(ChatColor.RED + "Players: " + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY +
                    gameGroup.getPlayerCount() + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY +
                    gameGroup.getMaxPlayerCount() + ChatColor.DARK_GRAY +
                    "]");

            ItemStack item = InventoryUtils
                    .createItemWithNameAndLore(mat, 1, 0, gameGroup.getName(), lore.toArray(new String[lore.size()]));

            ClickableItem clickableItem = new ClickableItem(item) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    user.sendMessage("Sending you to gamegroup: " + gameGroup.getName());

                    hub.getRequestProtocol().sendJoinGameGroupPacket(user.getUuid(), gameGroup.getType(), gameGroup.getName());
                }
            };

            inventory.addItem(clickableItem);
        }

        user.showInventory(inventory, null);
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
