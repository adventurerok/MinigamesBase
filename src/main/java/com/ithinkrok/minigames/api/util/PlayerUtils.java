package com.ithinkrok.minigames.api.util;

import net.minecraft.server.v1_12_R1.EntityTracker;
import net.minecraft.server.v1_12_R1.EntityTrackerEntry;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerUtils {


    @SuppressWarnings("unchecked")
    public static void showPlayer(Player to, Player show) {
        boolean couldSee = to.canSee(show);
        to.showPlayer(show);

        if(couldSee) {
            try{
                CraftPlayer craftTo = (CraftPlayer) to;
                CraftPlayer craftShow = (CraftPlayer) show;
                EntityTracker tracker = ((WorldServer) craftTo.getHandle().getWorld()).tracker;

                EntityTrackerEntry entry = tracker.trackedEntities.get(craftShow.getHandle().getId());
                if(entry != null) {
                    entry.trackedPlayers.remove(craftTo.getHandle());
                    entry.updatePlayer(craftTo.getHandle());
                }
            } catch(Exception ignored) {

            }
        }
    }

}
