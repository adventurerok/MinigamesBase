package com.ithinkrok.minigames.api.database;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.ithinkrok.minigames.api.SpecificPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.Map;
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

    public Persistence(Plugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::checkDDL);

        start();
    }

    private void checkDDL() {
        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            if (!SpecificPlugin.class.isInstance(plugin)) continue;

            if (!plugin.getDescription().isDatabaseEnabled()) continue;

            System.out.println("Ensuring tables exist for plugin database: " + plugin.getName());

            try {
                SpiEbeanServer serv = (SpiEbeanServer) plugin.getDatabase();
                DdlGenerator gen = serv.getDdlGenerator();

                gen.runScript(false, gen.generateCreateDdl().replace("create table", "create table if not exists"));
            } catch (PersistenceException e) {
                System.out.println("Error creating database tables for plugin: " + plugin.getName());
                e.printStackTrace();

            }
        }

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

    private static class PersistenceDatabaseAccessor implements DatabaseAccessor {

        private final Map<Class<?>, String> pluginNames = new HashMap<>();

        @Override
        public EbeanServer getDatabase(Class<?> databaseClass) {
            if (pluginNames.containsKey(databaseClass)) {
                return Bukkit.getPluginManager().getPlugin(pluginNames.get(databaseClass)).getDatabase();
            }

            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                try {

                    EbeanServer database = plugin.getDatabase();
                    if (database == null) continue;
                    database.find(databaseClass);

                    pluginNames.put(databaseClass, plugin.getName());
                    return database;
                } catch (PersistenceException | IllegalStateException ignored) {
                }
            }

            throw new RuntimeException("No plugin has registered database class: " + databaseClass);
        }

        @Override
        public <T> Query<T> find(Class<T> beanType) {
            return getDatabase(beanType).find(beanType);
        }

        @Override
        public <T> T createEntityBean(Class<T> type) {
            return getDatabase(type).createEntityBean(type);
        }

        @Override
        public void save(Object bean) throws OptimisticLockException {
            getDatabase(bean.getClass()).save(bean);
        }
    }
}
