package com.ithinkrok.minigames.base.hub;

import com.ithinkrok.minigames.base.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.base.protocol.data.GameGroupInfo;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;
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
        Sign sign = (Sign) location.getBlock().getState();

        sign.setLine(0, WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')));

        Collection<GameGroupInfo> accepting = controller.getAcceptingGameGroups(gameGroupType);

        if(accepting.isEmpty()) {
            sign.setLine(1, "");
            sign.setLine(2, "Create new Lobby");
            sign.setLine(3, "");
        } else {
            sign.setLine(1, "Join Lobby");

            GameGroupInfo bestMatch = null;

            for (GameGroupInfo gameGroupInfo : accepting) {
                if (bestMatch != null && gameGroupInfo.getPlayerCount() <= bestMatch.getPlayerCount()) continue;

                bestMatch = gameGroupInfo;
            }

            //IntelliJ wanted me to add this to stop a warning. This will never be true
            if (bestMatch == null) return;

            sign.setLine(2, bestMatch.getPlayerCount() + "/" + bestMatch.getMaxPlayerCount());
            sign.setLine(3, bestMatch.getMotd());
        }

        sign.update();
    }
}
