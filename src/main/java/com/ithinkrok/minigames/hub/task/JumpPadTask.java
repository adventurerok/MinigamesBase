package com.ithinkrok.minigames.hub.task;

import com.ithinkrok.minigames.api.task.GameRunnable;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.hub.data.JumpPad;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * Created by paul on 17/09/16.
 */
public class JumpPadTask implements GameRunnable {

    private final User user;

    private final Map<Material, JumpPad> jumpPadMap;

    public JumpPadTask(User user, Map<Material, JumpPad> jumpPadMap) {
        this.user = user;
        this.jumpPadMap = jumpPadMap;
    }


    @Override
    public void run(GameTask task) {
        Location location = user.getLocation();
        Material material = location.getBlock().getType();

        JumpPad pad = jumpPadMap.get(material);
        if(pad == null) return;

        Vector velocity = location.getDirection();

        velocity.setY(0.4d);
        velocity.multiply(pad.getPower());

        user.setVelocity(velocity);

        if(pad.getSound() != null) {
            user.playSound(user.getLocation(), pad.getSound());
        }

    }
}
