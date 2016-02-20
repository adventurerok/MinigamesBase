package com.ithinkrok.minigames.api.database;

/**
 * Created by paul on 17/01/16.
 */
public interface DatabaseTask {
    void run(DatabaseAccessor accessor);
}
