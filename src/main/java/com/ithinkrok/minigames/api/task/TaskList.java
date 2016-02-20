package com.ithinkrok.minigames.api.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by paul on 02/01/16.
 * <p>
 * Holds a list of game tasks. Allows all the tasks on the list to be cancelled at once. Good for holding tasks that
 * should not be run if a state changes in the future.
 */
public class TaskList {

    private Set<GameTask> tasks = new HashSet<>();

    public void addTask(GameTask task) {
        if (task.getTaskState() == GameTask.TaskState.CANCELLED || task.getTaskState() == GameTask.TaskState.FINISHED)
            return;

        if(!tasks.add(task)) return;
        task.addedToTaskList(this);
    }

    public void removeTask(GameTask task) {
        tasks.remove(task);

        if (task.getTaskState() == GameTask.TaskState.CANCELLED || task.getTaskState() == GameTask.TaskState.FINISHED)
            return;

        task.removedFromTaskList(this);
    }

    public void cancelAllTasks() {
        List<GameTask> tasksCopy = new ArrayList<>(tasks);

        tasksCopy.forEach(GameTask::cancel);
    }
}
