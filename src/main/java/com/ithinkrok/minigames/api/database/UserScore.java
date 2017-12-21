package com.ithinkrok.minigames.api.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 17/01/16.
 */
public class UserScore implements DatabaseObject {

    private String playerUUID;

    private String playerName;

    private String game;

    private double value;


    public UserScore(String playerUUID, String playerName, String game, double value) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.game = game;
        this.value = value;
    }

    public static UserScore get(DatabaseAccessor accessor, String playerUUID, String game) throws SQLException {
        List<UserScore> result = query(accessor, "WHERE player_uuid=? AND game=?", playerUUID, game);

        if(!result.isEmpty()) {
            return result.get(0);
        } else return null;
    }

    public static List<UserScore> query(DatabaseAccessor accessor, String sql, Object... params) throws SQLException {
        try (Connection conn = accessor.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT * from mg_user_score " + sql + ";"
             )) {

            for (int index = 0; index < params.length; ++index) {
                statement.setObject(index + 1, params[index]);
            }

            try (ResultSet results = statement.executeQuery()) {
                List<UserScore> output = new ArrayList<>();

                while (results.next()) {
                    output.add(load(results));
                }

                return output;
            }
        }
    }

    private static UserScore load(ResultSet results) throws SQLException {
        return new UserScore(
                results.getString("player_uuid"),
                results.getString("player_name"),
                results.getString("game"),
                results.getDouble("value")
        );
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

    @Override
    public void save(DatabaseAccessor accessor) throws SQLException {
        try (Connection conn = accessor.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "INSERT INTO mg_user_score " +
                             "(player_uuid, player_name, game, value, version) " +
                             "VALUES (?, ?, ?, ?, Now()) ON DUPLICATE KEY UPDATE " +
                             "game=?, value=?, version=NOW();"
             )) {

            statement.setString(1, playerUUID);
            statement.setString(2, playerName);

            for (int n = 0; n <= 1; ++n) {
                statement.setString(3 + 2 * n, game);
                statement.setDouble(4 + 2 * n, value);
            }

            statement.executeUpdate();
        }
    }
}
