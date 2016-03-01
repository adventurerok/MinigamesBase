package com.ithinkrok.minigames.api.user;

import com.ithinkrok.minigames.api.event.user.game.UserAbilityCooldownEvent;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskList;
import com.ithinkrok.minigames.api.util.NamedSounds;
import com.ithinkrok.minigames.api.util.SoundEffect;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 03/01/16.
 */
public class CooldownHandler {

    private final User user;
    private final Map<String, Long> coolingDown = new HashMap<>();
    private final TaskList coolDownTasks = new TaskList();

    public CooldownHandler(User user) {
        this.user = user;
    }

    public boolean startCoolDown(String ability, double seconds, String coolDownLocale) {
        if (isCoolingDown(ability)) {
            user.sendLocale("cooldowns.default.wait");
            return false;
        }

        if (seconds <= 0) return true;

        coolingDown.put(ability, timeInFuture(seconds));

        GameTask task = user.doInFuture(task1 -> stopCoolDown(ability, coolDownLocale), (int) (seconds * 20));

        coolDownTasks.addTask(task);

        return true;
    }

    public boolean isCoolingDown(String ability) {
        return coolingDown.containsKey(ability);
    }

    private long timeInFuture(double secondsInFuture) {
        return System.nanoTime() + (long) (secondsInFuture * 1000000000);
    }

    @SuppressWarnings("unchecked")
    public void stopCoolDown(String ability, String stopLocale) {
        if (!isCoolingDown(ability)) return;

        coolingDown.remove(ability);

        //if (!isInGame()) return;
        UserAbilityCooldownEvent event = new UserAbilityCooldownEvent(user, ability,
                new SoundEffect(NamedSounds.fromName("ENTITY_ZOMBIE_VILLAGER_CURE"), 1.0f, 2.0f),
                user.getGameGroup().getLocale(stopLocale));

        user.getGameGroup().userEvent(event);

        if (event.getCoolDownMessage() != null) user.sendMessage(ChatColor.GREEN + event.getCoolDownMessage());
        if (event.getSoundEffect() != null) user.playSound(user.getLocation(), event.getSoundEffect());
    }

    public void cancelCoolDowns() {
        coolingDown.clear();
        coolDownTasks.cancelAllTasks();
    }
}
