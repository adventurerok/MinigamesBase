package com.ithinkrok.minigames.hub;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.config.Config;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

import java.util.Collection;

/**
 * Created by paul on 20/02/16.
 */
public class JoinLobbySign extends HubSign {

    public JoinLobbySign(UserEditSignEvent event) {
        super(event);
    }

    public JoinLobbySign(GameGroup gameGroup, Config config) {
        super(gameGroup, config);
    }

    @Override
    protected void updateSign() {
        Sign sign = (Sign) location.getBlock().getState();

        sign.setLine(0, ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA +
                WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')) + ChatColor.DARK_GRAY + "]");

        Collection<GameGroupInfo> accepting = gameGroup.getControllerInfo().getAcceptingGameGroups(gameGroupType);

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

    @Override
    public void onRightClick(User user) {
        Collection<GameGroupInfo> accepting = user.getGameGroup().getControllerInfo().getAcceptingGameGroups(gameGroupType);

        GameGroupInfo bestMatch = null;

        for (GameGroupInfo gameGroupInfo : accepting) {
            if (bestMatch != null && gameGroupInfo.getPlayerCount() < bestMatch.getPlayerCount()) continue;

            bestMatch = gameGroupInfo;
        }

        String gameGroupName = bestMatch != null ? bestMatch.getName() : null;

        if (gameGroupName != null) user.sendMessage("Sending you to gamegroup: " + gameGroupName);
        else user.sendMessage("Creating a new " + gameGroupType + " gamegroup for you");

        user.getGameGroup().getRequestProtocol().sendJoinGameGroupPacket(user.getUuid(), gameGroupType, gameGroupName);
    }

    @Override
    public Config toConfig() {
        Config config = super.toConfig();

        config.set("spectators", false);

        return config;
    }
}
