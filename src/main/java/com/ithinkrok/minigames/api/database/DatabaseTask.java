package com.ithinkrok.minigames.api.database;

import java.sql.SQLException;

/**
 * Created by paul on 17/01/16.
 */
public interface DatabaseTask {
    void run(DatabaseAccessor accessor) throws SQLException;
}
