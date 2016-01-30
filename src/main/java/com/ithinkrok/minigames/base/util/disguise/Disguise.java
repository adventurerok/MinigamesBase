package com.ithinkrok.minigames.base.util.disguise;

import org.bukkit.entity.EntityType;

/**
 * Created by paul on 30/01/16.
 */
public class Disguise {

    private EntityType entityType;

    private String playerName;
    private String playerSkin;

    private boolean viewSelfDisguise = true;
    private boolean modifyBoundingBox = false;
    private boolean replaceSounds = true;

    private boolean showName = false;

    public Disguise(EntityType entityType) {
        this.entityType = entityType;
    }

    public Disguise(String playerName) {
        this(playerName, playerName);
    }

    public Disguise(String playerName, String playerSkin) {
        this.entityType = EntityType.PLAYER;
        this.playerName = playerName;
        this.playerSkin = playerSkin;
    }

    public void setShowName(boolean showName) {
        this.showName = showName;
    }

    public boolean isShowName() {
        return showName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerSkin() {
        return playerSkin;
    }

    public void setPlayerSkin(String playerSkin) {
        this.playerSkin = playerSkin;
    }

    public boolean isViewSelfDisguise() {
        return viewSelfDisguise;
    }

    public void setViewSelfDisguise(boolean viewSelfDisguise) {
        this.viewSelfDisguise = viewSelfDisguise;
    }

    public boolean isModifyBoundingBox() {
        return modifyBoundingBox;
    }

    public void setModifyBoundingBox(boolean modifyBoundingBox) {
        this.modifyBoundingBox = modifyBoundingBox;
    }

    public boolean isReplaceSounds() {
        return replaceSounds;
    }

    public void setReplaceSounds(boolean replaceSounds) {
        this.replaceSounds = replaceSounds;
    }
}
