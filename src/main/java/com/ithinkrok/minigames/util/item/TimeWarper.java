package com.ithinkrok.minigames.util.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.util.metadata.Museum;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

public class TimeWarper implements CustomListener {

    private boolean isToTheFuture;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?,?> event) {
        isToTheFuture = event.getConfigOrEmpty().getBoolean("to_the_future");
    }

    @CustomEventHandler
    public void onRightClick(UserInteractEvent event) {
        Museum mus = Museum.getOrCreate(event.getGameGroup());

        Museum.MuseumLocation loc = mus.getLocation(event.getUser().getMapLocation());

        if(loc == null) {
            event.getUser().sendLocale("museum.no_location");
            return;
        }

        Museum.MuseumLocation target;
        if(isToTheFuture) {
            target = loc.getFuture();
            if(target == null) {
                event.getUser().sendLocale("museum.no_future");
                return;
            }
        } else {
            target = loc.getPast();
            if(target == null) {
                event.getUser().sendLocale("museum.no_past");
                return;
            }
        }

        double dx = target.getPos().getX() - loc.getPos().getX();
        double dy = target.getPos().getY() - loc.getPos().getY();
        double dz = target.getPos().getZ() - loc.getPos().getZ();

        MapPoint targetPos = event.getUser().getMapLocation().add(dx, dy, dz);
        event.getUser().teleport(targetPos);

        if(isToTheFuture) {
            event.getUser().sendLocale("museum.to_the_future");
        } else {
            event.getUser().sendLocale("museum.to_the_past");
        }
    }

}
