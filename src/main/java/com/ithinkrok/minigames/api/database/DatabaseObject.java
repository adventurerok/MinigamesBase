package com.ithinkrok.minigames.api.database;

import java.sql.SQLException;

public interface DatabaseObject {


    /**
     * Saves this Object back to the database
     */
    void save(DatabaseAccessor accessor) throws SQLException;

}
