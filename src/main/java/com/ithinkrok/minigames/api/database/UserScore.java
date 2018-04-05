package com.ithinkrok.minigames.api.database;

import com.ithinkrok.util.UUIDUtils;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by paul on 17/01/16.
 */
public class UserScore implements DatabaseObject, NameHolder {

    private UUID playerUUID;

    private String playerName;

    private String game;

    private double value;

    private Instant lastUpdated;

    /**
     * The last time when there name was known to be that name.
     */
    private Instant nameKnownAt;


    public UserScore(UUID playerUUID, String playerName, String game, double value) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.game = game;
        this.value = value;
    }


    public static UserScore get(DatabaseAccessor accessor, UUID playerUUID, String game) throws SQLException {
        List<UserScore> result =
                query(
                        accessor,
                        "WHERE uuid=UNHEX(REPLACE(?,'-','')) AND game=?",
                        playerUUID.toString(),
                        game
                );

        if (!result.isEmpty()) {
            return result.get(0);
        } else return null;
    }


    public static List<UserScore> query(DatabaseAccessor accessor, String sql, Object... params) throws SQLException {
        sql = sql.replace("uuid", "mg_user_score.uuid");
        try (Connection conn = accessor.getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT HEX(mg_user_score.uuid) as uuid, name, game, value," +
                     " mg_user_score.version as version, mg_name_cache.timestamp as name_version" +
                     " from mg_user_score " +
                     "LEFT JOIN mg_name_cache ON mg_user_score.uuid = mg_name_cache.uuid " + sql + ";"
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
        UserScore score = new UserScore(
                UUIDUtils.fromStringWithoutDashes(results.getString("uuid")),
                results.getString("name"),
                results.getString("game"),
                results.getDouble("value")
        );

        score.lastUpdated = results.getTimestamp("version").toInstant();
        Timestamp name_version = results.getTimestamp("name_version");

        if (name_version != null) {
            score.nameKnownAt = name_version.toInstant();
        }

        return score;
    }


    public Instant getLastUpdated() {
        return lastUpdated;
    }


    @Override
    public UUID getPlayerUUID() {
        return playerUUID;
    }


    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }


    @Override
    public String getPlayerName() {
        return playerName;
    }


    @Override
    public Instant getNameKnownAt() {
        return nameKnownAt;
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
                     "(uuid, game, value) " +
                     "VALUES (UNHEX(REPLACE(?,'-','')), ?, ?) ON DUPLICATE KEY UPDATE " +
                     "value=?;"
             )) {

            statement.setString(1, playerUUID.toString());
            statement.setString(2, game);

            for (int n = 0; n <= 1; ++n) {
                statement.setDouble(3 + n, value);
            }

            statement.executeUpdate();
        }
    }
}
