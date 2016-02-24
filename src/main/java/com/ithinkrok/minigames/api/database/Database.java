package com.ithinkrok.minigames.api.database;

import com.avaje.ebean.Query;
import com.ithinkrok.minigames.api.user.User;

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

    @Override
    public void doDatabaseTask(DatabaseTask task) {
        taskRunner.doDatabaseTask(task);
    }

    public void getIntUserValue(User user, String name, IntConsumer consumer, int def) {
        getIntUserValue(user.getUuid(), name, consumer, def);
    }

    public void getIntUserValue(UUID user, String name, IntConsumer consumer, int def) {
        doDatabaseTask(accessor -> {
            Query<IntUserValue> query = accessor.find(IntUserValue.class);

            query.where().eq("player_uuid", user.toString()).eq("property", name);

            IntUserValue result = query.findUnique();
            if(result == null) {
                consumer.accept(def);
            } else {
                consumer.accept(result.getValue());
            }
        });
    }

    public void setIntUserValue(User user, String name, int value) {
        setIntUserValue(user.getUuid(), name, value);
    }

    public void setIntUserValue(UUID user, String name, int value) {
        doDatabaseTask(accessor -> {
            Query<IntUserValue> query = accessor.find(IntUserValue.class);

            query.where().eq("player_uuid", user.toString()).eq("property", name);

            IntUserValue result = query.findUnique();
            if(result == null) {
                result = accessor.createEntityBean(IntUserValue.class);

                result.setPlayerUUID(user.toString());
                result.setProperty(name);
            }

            result.setValue(value);

            accessor.save(result);
        });
    }

    public void getDoubleUserValue(User user, String name, DoubleConsumer consumer, double def) {
        getDoubleUserValue(user.getUuid(), name, consumer, def);
    }

    public void getDoubleUserValue(UUID user, String name, DoubleConsumer consumer, double def) {
        doDatabaseTask(accessor -> {
            Query<DoubleUserValue> query = accessor.find(DoubleUserValue.class);

            query.where().eq("player_uuid", user.toString()).eq("property", name);

            DoubleUserValue result = query.findUnique();
            if(result == null) {
                consumer.accept(def);
            } else {
                consumer.accept(result.getValue());
            }
        });
    }

    public void setDoubleUserValue(User user, String name, double value) {
        setDoubleUserValue(user.getUuid(), name, value);
    }

    public void setDoubleUserValue(UUID user, String name, double value) {
        doDatabaseTask(accessor -> {
            Query<DoubleUserValue> query = accessor.find(DoubleUserValue.class);

            query.where().eq("player_uuid", user.toString()).eq("property", name);

            DoubleUserValue result = query.findUnique();
            if(result == null) {
                result = accessor.createEntityBean(DoubleUserValue.class);

                result.setPlayerUUID(user.toString());
                result.setProperty(name);
            }

            result.setValue(value);

            accessor.save(result);
        });
    }

    public void getBooleanUserValue(User user, String name, Consumer<Boolean> consumer, Boolean def) {
        getBooleanUserValue(user.getUuid(), name, consumer, def);
    }

    public void getBooleanUserValue(UUID user, String name, Consumer<Boolean> consumer, Boolean def) {
        doDatabaseTask(accessor -> {
            Query<BooleanUserValue> query = accessor.find(BooleanUserValue.class);

            query.where().eq("player_uuid", user.toString()).eq("property", name);

            BooleanUserValue result = query.findUnique();
            if(result == null) {
                consumer.accept(def);
            } else {
                consumer.accept(result.isValue());
            }
        });
    }

    public void setBooleanUserValue(User user, String name, boolean value) {
        setBooleanUserValue(user.getUuid(), name, value);
    }

    public void setBooleanUserValue(UUID user, String name, boolean value) {
        doDatabaseTask(accessor -> {
            Query<BooleanUserValue> query = accessor.find(BooleanUserValue.class);

            query.where().eq("player_uuid", user.toString()).eq("property", name);

            BooleanUserValue result = query.findUnique();
            if(result == null) {
                result = accessor.createEntityBean(BooleanUserValue.class);

                result.setPlayerUUID(user.toString());
                result.setProperty(name);
            }

            result.setValue(value);

            accessor.save(result);
        });
    }

    public void getStringUserValue(User user, String name, Consumer<String> consumer, String def) {
        getStringUserValue(user.getUuid(), name, consumer, def);
    }

    public void getStringUserValue(UUID user, String name, Consumer<String> consumer, String def) {
        doDatabaseTask(accessor -> {
            Query<StringUserValue> query = accessor.find(StringUserValue.class);

            query.where().eq("player_uuid", user.toString()).eq("property", name);

            StringUserValue result = query.findUnique();
            if(result == null) {
                consumer.accept(def);
            } else {
                consumer.accept(result.getValue());
            }
        });
    }

    public void setStringUserValue(User user, String name, String value) {
        setStringUserValue(user.getUuid(), name, value);
    }

    public void setStringUserValue(UUID user, String name, String value) {
        doDatabaseTask(accessor -> {
            Query<StringUserValue> query = accessor.find(StringUserValue.class);

            query.where().eq("player_uuid", user.toString()).eq("property", name);

            StringUserValue result = query.findUnique();
            if(result == null) {
                result = accessor.createEntityBean(StringUserValue.class);

                result.setPlayerUUID(user.toString());
                result.setProperty(name);
            }

            result.setValue(value);

            accessor.save(result);
        });
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
            if(ascending) query.orderBy("value asc");
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
            if(result == null) {
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
