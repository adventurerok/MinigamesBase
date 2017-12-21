package com.ithinkrok.minigames.api.database;

import com.ithinkrok.util.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by paul on 17/01/16.
 */
public class Persistence extends Thread {

    private final Plugin plugin;
    private final ConcurrentLinkedQueue<DatabaseTask> threadTasks = new ConcurrentLinkedQueue<>();
    private final DatabaseAccessor accessor = new PersistenceDatabaseAccessor();
    private boolean stop = false;

    private boolean checkedDDL = false;

    private HikariDataSource connectionPool;

    public Persistence(Plugin plugin, Config config) {
        this.plugin = plugin;

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(config.getString("url"));
        hikari.setDriverClassName(config.getString("driver", "com.mysql.jdbc.Driver"));
        hikari.setUsername(config.getString("user"));
        hikari.setPassword(config.getString("password"));
        hikari.setMinimumIdle(config.getInt("min_connections", 1));
        hikari.setMaximumPoolSize(config.getInt("max_connections", 10));

        connectionPool = new HikariDataSource(hikari);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::checkDDL);

        start();
    }

    private void checkDDL() {
//        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
//            if (!SpecificPlugin.class.isInstance(plugin)) continue;
//
//            if (!plugin.getDescription().isDatabaseEnabled()) continue;
//
//            System.out.println("Ensuring tables exist for plugin database: " + plugin.getName());
//
//            try {
//                SpiEbeanServer serv = (SpiEbeanServer) plugin.getDatabase();
//                DdlGenerator gen = serv.getDdlGenerator();
//
//                gen.runScript(false, gen.generateCreateDdl().replace("create table", "create table if not exists"));
//            } catch (PersistenceException e) {
//                System.out.println("Error creating database tables for plugin: " + plugin.getName());
//                e.printStackTrace();
//
//            }
//        }

        //TODO create tables if not exists, but do them properly with constraints and things now

        checkedDDL = true;
    }

    @Override
    public void run() {
        while (!stop) {
            if (!checkedDDL) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }

            DatabaseTask task;

            while ((task = threadTasks.poll()) != null) {
                try {
                    task.run(accessor);
                } catch (Exception e) {
                    System.out.println("Exception while doing DatabaseTask");
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void onPluginDisabled() {
        stop = true;

        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doTask(DatabaseTask task) {
        threadTasks.add(task);
    }

    private class PersistenceDatabaseAccessor implements DatabaseAccessor {

        @Override
        public Connection getConnection() throws SQLException {
            return connectionPool.getConnection();
        }
    }
}
