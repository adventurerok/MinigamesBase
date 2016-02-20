package com.ithinkrok.minigames.api.util.playerstate;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Created by paul on 17/01/16.
 */
public class NameCapture {

    private String customName, tabListName;

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getTabListName() {
        return tabListName;
    }

    public String getCustomName() {
        return customName;
    }

    public void setTabListName(String tabListName) {
        this.tabListName = tabListName;
    }

    public void capture(LivingEntity entity) {
        if(entity instanceof Player) {
            customName = ((Player) entity).getDisplayName();
            tabListName = ((Player) entity).getPlayerListName();
        } else {
            customName = entity.getCustomName();
        }
    }

    public void restore(LivingEntity entity) {
        if(entity instanceof Player) {
            ((Player) entity).setDisplayName(customName);
            ((Player) entity).setPlayerListName(tabListName);
        } else {
            entity.setCustomName(customName);
        }
    }
}
