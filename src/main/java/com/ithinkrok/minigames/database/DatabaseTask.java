package com.ithinkrok.minigames.database;

import com.avaje.ebean.EbeanServer;

/**
 * Created by paul on 17/01/16.
 */
public interface DatabaseTask {
    void run(EbeanServer database);
}
