package com.ithinkrok.minigames.api.database;

import com.avaje.ebean.Query;
import com.ithinkrok.minigames.api.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/**
 * Created by paul on 20/02/16.
 */
public class Database implements DatabaseTaskRunner {

    private final DatabaseTaskRunner taskRunner;

    public Database(DatabaseTaskRunner taskRunner) {
        this.taskRunner = taskRunner;
    }

    public void getIntUserValue(User user, String name, IntConsumer consumer, int def) {
        getIntUserValue(user.getUuid(), name, consumer, def);
    }

    public void getIntUserValue(UUID user, String name, IntConsumer consumer, int def) {
        getUserValue(user, name, "mg_user_ints", o -> {
            if(o != null) {
                consumer.accept((Integer) o);
            } else {
                consumer.accept(def);
            }
        });
    }

    @Override
    public void doDatabaseTask(DatabaseTask task) {
        taskRunner.doDatabaseTask(task);
    }

    public void setIntUserValue(User user, String name, int value) {
        setIntUserValue(user.getUuid(), name, value);
    }

    public void setIntUserValue(UUID user, String name, int value) {
        setUserValue(user, name, value);
    }

    private void getUserValue(UUID user, String property, String table, Consumer<Object> consumer) {
        doDatabaseTask(accessor -> {

            try (Connection connection = accessor.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT value FROM " + table + " WHERE player_uuid=? AND property=?"
                 )) {

                statement.setString(1, user.toString());
                statement.setString(2, property);

                try (ResultSet results = statement.executeQuery()) {
                    if (results.next()) {
                        consumer.accept(results.getObject("value"));
                    } else {
                        consumer.accept(null);
                    }
                }
            }
        });
    }

    private void setUserValue(UUID user, String property, Object value) {
        String type;

        if (value instanceof Integer) {
            type = "mg_user_ints";
        } else if (value instanceof String) {
            type = "mg_user_strings";
        } else if (value instanceof Double) {
            type = "mg_user_doubles";
        } else if (value instanceof Boolean) {
            type = "mg_user_bools";
        } else {
            throw new IllegalArgumentException("value of unsupported type: " + value.getClass().getName());
        }

        doDatabaseTask(accessor -> {
            try (Connection connection = accessor.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO " + type + " " +
                                 "(player_uuid, property, value, version) VALUES (?,?,?, NOW()) " +
                                 "ON DUPLICATE KEY UPDATE " +
                                 "value=?, version=NOW();"
                 )) {


                statement.setString(1, user.toString());
                statement.setString(2, property);

                for (int n = 3; n <= 4; ++n) {
                    statement.setObject(n, value);
                }

                statement.executeUpdate();
            }
        });
    }

    public void getDoubleUserValue(User user, String name, DoubleConsumer consumer, double def) {
        getDoubleUserValue(user.getUuid(), name, consumer, def);
    }

    public void getDoubleUserValue(UUID user, String name, DoubleConsumer consumer, double def) {
        getUserValue(user, name, "mg_user_doubles", o -> {
            if(o != null) {
                consumer.accept((Double) o);
            } else {
                consumer.accept(def);
            }
        });
    }

    public void setDoubleUserValue(User user, String name, double value) {
        setDoubleUserValue(user.getUuid(), name, value);
    }

    public void setDoubleUserValue(UUID user, String name, double value) {
        setUserValue(user, name, value);
    }

    public void getBooleanUserValue(User user, String name, Consumer<Boolean> consumer, Boolean def) {
        getBooleanUserValue(user.getUuid(), name, consumer, def);
    }

    public void getBooleanUserValue(UUID user, String name, Consumer<Boolean> consumer, Boolean def) {
        getUserValue(user, name, "mg_user_bools", o -> {
            if(o != null) {
                consumer.accept((Boolean) o);
            } else {
                consumer.accept(def);
            }
        });
    }

    public void setBooleanUserValue(User user, String name, boolean value) {
        setBooleanUserValue(user.getUuid(), name, value);
    }

    public void setBooleanUserValue(UUID user, String name, boolean value) {
        setUserValue(user, name, value);
    }

    public void getStringUserValue(User user, String name, Consumer<String> consumer, String def) {
        getStringUserValue(user.getUuid(), name, consumer, def);
    }

    public void getStringUserValue(UUID user, String name, Consumer<String> consumer, String def) {
        getUserValue(user, name, "mg_user_strings", o -> {
            if(o != null) {
                consumer.accept(o.toString());
            } else {
                consumer.accept(def);
            }
        });
    }

    public void setStringUserValue(User user, String name, String value) {
        setStringUserValue(user.getUuid(), name, value);
    }

    public void setStringUserValue(UUID user, String name, String value) {
        setUserValue(user, name, value);
    }

    public void getUserScore(User user, String gameType, Consumer<UserScore> consumer) {
        getUserScore(user.getUuid(), gameType, consumer);
    }

    public void getUserScore(UUID user, String gameType, Consumer<UserScore> consumer) {
        doDatabaseTask(accessor -> {
            Query<UserScore> query = accessor.find(UserScore.class);

            query.where().eq("player_uuid", user.toString()).eq("game", gameType);

            UserScore result = query.findUnique();
            consumer.accept(result);
        });
    }

    public void getHighScores(String gameType, int count, boolean ascending, Consumer<List<UserScore>> consumer) {
        doDatabaseTask(accessor -> {
            Query<UserScore> query = accessor.find(UserScore.class);

            query.where().eq("game", gameType);
            if (ascending) query.orderBy("value asc");
            else query.orderBy("value desc");

            query.setMaxRows(count);
            consumer.accept(query.findList());
        });
    }

    public void setUserScore(User user, String gameType, double value) {

        setUserScore(user.getUuid(), user.getName(), gameType, value);
    }

    public void setUserScore(UUID user, String userName, String gameType, double value) {
        doDatabaseTask(accessor -> {
            Query<UserScore> query = accessor.find(UserScore.class);

            query.where().eq("player_uuid", user.toString()).eq("game", gameType);

            UserScore result = query.findUnique();
            if (result == null) {
                result = accessor.createEntityBean(UserScore.class);

                result.setPlayerUUID(user.toString());
                result.setGame(gameType);
            }

            result.setPlayerName(userName);
            result.setValue(value);

            accessor.save(result);
        });
    }
}
