package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.base.command.MinigamesCommand;

/**
 * Created by paul on 24/01/16.
 */
public class UserCommandEvent extends UserEvent implements MinigamesCommandEvent {

    private final MinigamesCommand command;

    /**
     * This is true by default if there is a CommandConfig for this command.
     */
    private boolean handled = false;

    /**
     * An invalid command will show the incorrect usage method after it is called.
     */
    private boolean validCommand = true;

    public UserCommandEvent(User user, MinigamesCommand command) {
        super(user);
        this.command = command;
    }

    @Override
    public MinigamesCommand getCommand() {
        return command;
    }

    @Override
    public User getCommandSender() {
        return getUser();
    }

    @Override
    public boolean isHandled() {
        return handled;
    }

    @Override
    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    @Override
    public boolean isValidCommand() {
        return validCommand;
    }

    @Override
    public void setValidCommand(boolean validCommand) {
        this.validCommand = validCommand;
    }
}
