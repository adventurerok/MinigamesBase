package com.ithinkrok.minigames.api.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by paul on 27/01/16.
 */
public interface DatabaseAccessor {

    Connection getConnection() throws SQLException;
}
