package com.ithinkrok.minigames.api.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by paul on 02/01/16.
 */
public class GameTask {

    private GameRunnable runnable;
    private int bukkitTask;
    private Collection<TaskList> taskLists = new ArrayList<>();
    private TaskState taskState = TaskState.CREATED;
    private int runCount = 0;
    private boolean finishAllowed = false;

    public GameTask(GameRunnable runnable) {
        this.runnable = runnable;
    }

    public void schedule(Plugin plugin, int delay) {
        if(taskState != TaskState.CREATED) return;
        taskState = TaskState.SCHEDULED;

        bukkitTask = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            finishAllowed = true;
            runnable.run(this);
            finishAllowed = false;

            ++runCount;

            taskState = TaskState.FINISHED;
            removeFromTaskLists();
        }, delay);
    }

    public void schedule(Plugin plugin, int delay, int period) {
        if(taskState != TaskState.CREATED) return;
        taskState = TaskState.SCHEDULED;

        bukkitTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            finishAllowed = true;
            runnable.run(this);
            finishAllowed = false;

            ++runCount;

        }, delay, period);
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public int getRunCount() {
        return runCount;
    }

    void addedToTaskList(TaskList taskList) {
        taskLists.add(taskList);
    }

    private void removeFromTaskLists() {
        for(TaskList list : taskLists) {
            list.removeTask(this);
        }
    }

    public void cancel() {
        if(taskState != TaskState.SCHEDULED) return;
        Bukkit.getScheduler().cancelTask(bukkitTask);
        taskState = TaskState.CANCELLED;

        removeFromTaskLists();
    }

    public void finish() {
        if(!finishAllowed) throw new RuntimeException("Tasks can only finish() themselves");
        if(taskState != TaskState.SCHEDULED) return;
        Bukkit.getScheduler().cancelTask(bukkitTask);
        taskState = TaskState.FINISHED;

        removeFromTaskLists();
    }

    void removedFromTaskList(TaskList taskList) {
        taskLists.remove(taskList);
    }

    public enum TaskState {
        CREATED,
        SCHEDULED,
        FINISHED,
        CANCELLED
    }

}
