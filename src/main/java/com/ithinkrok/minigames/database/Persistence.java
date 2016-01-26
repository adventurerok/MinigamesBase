package com.ithinkrok.minigames.database;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.PersistenceException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by paul on 17/01/16.
 */
public class Persistence extends Thread {

    private final Plugin plugin;

    private boolean stop = false;

    private final ConcurrentLinkedQueue<DatabaseTask> threadTasks = new ConcurrentLinkedQueue<>();

    public Persistence(Plugin plugin) {
        this.plugin = plugin;

        checkDDL();

        start();
    }

    private void checkDDL() {
        try {
            plugin.getDatabase().find(IntUserValue.class).findRowCount();
        } catch(PersistenceException e) {
            try {
                Method method = JavaPlugin.class.getDeclaredMethod("installDDL");
                method.setAccessible(true);
                method.invoke(plugin);
            } catch (ReflectiveOperationException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while(!stop) {
            DatabaseTask task;

            while((task = threadTasks.poll()) != null) {
                try {
                    task.run(plugin.getDatabase());
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
}
