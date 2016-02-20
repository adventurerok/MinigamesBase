package com.ithinkrok.minigames.api.database;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by paul on 17/01/16.
 */
@Entity
@Table(name = "mg_user_score")
public class UserScore {

    @Id
    private int id;

    @Column
    private String playerUUID;

    @Column
    private String playerName;

    @Column
    private String game;

    @Column
    private double value;

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    @Version
    private Date version;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }
}
