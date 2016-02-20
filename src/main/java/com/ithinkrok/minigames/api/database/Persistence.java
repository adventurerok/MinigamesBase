package com.ithinkrok.minigames.api.database;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by paul on 17/01/16.
 */
public class Persistence extends Thread {

    private final Plugin plugin;

    private boolean stop = false;

    private final ConcurrentLinkedQueue<DatabaseTask> threadTasks = new ConcurrentLinkedQueue<>();

    private final DatabaseAccessor accessor = new PersistenceDatabaseAccessor();

    public Persistence(Plugin plugin) {
        this.plugin = plugin;

        checkDDL();

        start();
    }

    private void checkDDL() {
        try {
            SpiEbeanServer serv = (SpiEbeanServer) plugin.getDatabase();
            DdlGenerator gen = serv.getDdlGenerator();

            gen.runScript(false, gen.generateCreateDdl().replace("create table", "create table if not exists"));
        } catch(PersistenceException e) {
            System.out.println("Error creating database tables");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(!stop) {
            DatabaseTask task;

            while((task = threadTasks.poll()) != null) {
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
            if(pluginNames.containsKey(databaseClass)) {
                return Bukkit.getPluginManager().getPlugin(pluginNames.get(databaseClass)).getDatabase();
            }

            for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                EbeanServer database = plugin.getDatabase();
                if(database == null) continue;

                try {
                    database.find(databaseClass);

                    pluginNames.put(databaseClass, plugin.getName());
                    return database;
                } catch (PersistenceException ignored) {}
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
