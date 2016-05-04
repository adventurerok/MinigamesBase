package com.ithinkrok.minigames.util.map;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.BoundingBox;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.api.util.math.Calculator;
import com.ithinkrok.minigames.api.util.math.ExpressionCalculator;
import com.ithinkrok.minigames.api.util.math.MapVariables;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by paul on 04/05/16.
 */
public class TransportListener implements CustomListener {

    private final Collection<Transport> transports = new ArrayList<>();

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameMap> event) {
        Config config = event.getConfigOrEmpty();

        List<Config> transportConfigs = config.getConfigList("transports");

        for(Config transportConfig : transportConfigs) {
            transports.add(new Transport(transportConfig));
        }

        int tickInterval = config.getInt("tick_interval", 1);

        GameGroup gameGroup = event.getCreator();
        GameTask task = gameGroup.repeatInFuture(t -> {

            for(User user : gameGroup.getUsers()) {
                for(Transport transport : transports) {
                    if(!transport.bounds.containsLocation(user.getLocation())) continue;

                    transport.moveUser(user);
                }
            }

        }, tickInterval, tickInterval);

        event.getRepresenting().bindTaskToMap(task);
    }

    private static class Transport {

        BoundingBox bounds;

        Calculator xCalc, yCalc, zCalc;

        /**
         * Additional acception function for positions before modifying them.
         */
        Calculator accept;

        /**
         * Set to true to alter velocity instead of position for this axis.
         */
        boolean velocity;

        public Transport(Config config) {
            bounds = MinigamesConfigs.getBounds(config, "bounds");

            velocity = config.getBoolean("velocity");

            xCalc = new ExpressionCalculator(config.getString("func.x", velocity ? "dx" : "x"));
            yCalc = new ExpressionCalculator(config.getString("func.x", velocity ? "dy" : "y"));
            zCalc = new ExpressionCalculator(config.getString("func.x", velocity ? "dz" : "z"));

            accept = new ExpressionCalculator(config.getString("accept", "true"));
        }

        public void moveUser(User user) {
            Location loc = user.getLocation();

            MapVariables variables = new MapVariables();

            variables.setVariable("x", loc.getX());
            variables.setVariable("y", loc.getY());
            variables.setVariable("z", loc.getZ());
            variables.setVariable("yaw", loc.getYaw());
            variables.setVariable("pitch", loc.getPitch());

            Vector velocity = user.getVelocity();

            variables.setVariable("dx", velocity.getX());
            variables.setVariable("dy", velocity.getY());
            variables.setVariable("dz", velocity.getZ());

            if(!accept.calculateBoolean(variables)) return;

            if(this.velocity) {
                velocity.setX(xCalc.calculate(variables));
                velocity.setY(yCalc.calculate(variables));
                velocity.setZ(zCalc.calculate(variables));

                user.setVelocity(velocity);
            } else {
                Vector position = new Vector();

                position.setX(xCalc.calculate(variables));
                position.setY(yCalc.calculate(variables));
                position.setZ(zCalc.calculate(variables));

                user.teleport(position);
            }
        }

    }
}
