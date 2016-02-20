package com.ithinkrok.minigames.base.metadata;

import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.task.TaskList;

/**
 * Created by paul on 04/01/16.
 */
public abstract class Metadata{

    private final TaskList taskList = new TaskList();

    public final void bindTaskToMetadata(GameTask task) {
        taskList.addTask(task);
    }

    public final void cancelAllTasks() {
        taskList.cancelAllTasks();
    }

    public void removed() {

    }

    public abstract boolean removeOnGameStateChange(GameStateChangedEvent event);

    public abstract boolean removeOnMapChange(MapChangedEvent event);

    /**
     *
     * @return The class that is used as a key to store this metadata. The current class must be castable to it.
     */
    public Class<? extends Metadata> getMetadataClass() {
        return getClass();
    }
}
