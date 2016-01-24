package com.ithinkrok.minigames.task;

/**
 * Created by paul on 02/01/16.
 */
public interface TaskScheduler {

    GameTask doInFuture(GameRunnable task);
    GameTask doInFuture(GameRunnable task, int delay);
    GameTask repeatInFuture(GameRunnable task, int delay, int period);

    /**
     * Cancels all tasks that were created with this scheduler
     */
    void cancelAllTasks();
}
