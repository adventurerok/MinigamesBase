package com.ithinkrok.minigames.base.hub;

import com.ithinkrok.minigames.base.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.base.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.base.protocol.data.GameGroupInfo;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Collection;

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

    public void update(ControllerInfo controller) {
        if(spectatorSign) updateSpectatorSign(controller);
        else updateLobbySign(controller);
    }

    private void updateSpectatorSign(ControllerInfo controller) {
        Sign sign = (Sign) location.getBlock().getState();

        sign.setLine(0, ChatColor.GRAY + "[" + ChatColor.DARK_AQUA +
                WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')) + ChatColor.GRAY + "]");

        Collection<GameGroupInfo> all = controller.getGameGroups(gameGroupType);

        if(all.isEmpty()) {
            sign.setLine(1, "");
            sign.setLine(2, ChatColor.RED + "No Games");
            sign.setLine(3, "");
        } else {
            sign.setLine(1, ChatColor.RED + "Spectate Games");
            sign.setLine(2, ChatColor.DARK_GRAY.toString() + all.size() + ChatColor.BLACK + " games available");
            sign.setLine(3, ChatColor.BLUE + "Right click to chose");
        }

        sign.update();
    }

    public void updateLobbySign(ControllerInfo controller) {
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

    public void onRightClick(ClientMinigamesRequestProtocol requestProtocol, Player player) {
        Collection<GameGroupInfo> accepting = requestProtocol.getControllerInfo().getAcceptingGameGroups(gameGroupType);

        GameGroupInfo bestMatch = null;

        for(GameGroupInfo gameGroupInfo : accepting) {
            if(bestMatch != null && gameGroupInfo.getPlayerCount() < bestMatch.getPlayerCount()) continue;

            bestMatch = gameGroupInfo;
        }

        String gameGroupName = bestMatch != null ? bestMatch.getName() : null;

        if(gameGroupName != null) player.sendMessage("Sending you to gamegroup: " + gameGroupName);
        else player.sendMessage("Creating a new " + gameGroupType + " gamegroup for you");

        requestProtocol.sendJoinGameGroupPacket(player.getUniqueId(), gameGroupType, gameGroupName);
    }

    public Location getLocation() {
        return location;
    }
}
