package com.ithinkrok.minigames.util.playerstate;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Created by paul on 01/01/16.
 */
public class ModeCapture {

    float exp;
    float flySpeed;
    float walkSpeed;
    int level;
    boolean allowFlight;
    GameMode gameMode;

    public ModeCapture(Player capture){
        exp = capture.getExp();
        level = capture.getLevel();
        flySpeed = capture.getFlySpeed();
        walkSpeed = capture.getWalkSpeed();
        allowFlight = capture.getAllowFlight();
        gameMode = capture.getGameMode();
    }

    public void restore(Player to){
        to.setExp(exp);
        to.setLevel(level);
        to.setFlySpeed(flySpeed);
        to.setWalkSpeed(walkSpeed);
        to.setAllowFlight(allowFlight);
        to.setGameMode(gameMode);
    }

    public float getExp() {
        return exp;
    }

    public float getFlySpeed() {
        return flySpeed;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public int getLevel() {
        return level;
    }

    public boolean getAllowFlight() {
        return allowFlight;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setExp(float exp) {
        this.exp = exp;
    }

    public void setFlySpeed(float flySpeed) {
        this.flySpeed = flySpeed;
    }

    public void setWalkSpeed(float walkSpeed) {
        this.walkSpeed = walkSpeed;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setAllowFlight(boolean allowFlight) {
        this.allowFlight = allowFlight;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
}
