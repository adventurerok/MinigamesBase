package com.ithinkrok.minigames.base.event;

import com.ithinkrok.minigames.base.command.MinigamesCommand;
import com.ithinkrok.minigames.base.command.CommandSender;

/**
 * Created by paul on 25/01/16.
 */
public interface CommandEvent extends MinigamesEvent {

    MinigamesCommand getCommand();
    CommandSender getCommandSender();

    boolean isHandled();
    void setHandled(boolean handled);

    boolean isValidCommand();
    void setValidCommand(boolean validCommand);
}
