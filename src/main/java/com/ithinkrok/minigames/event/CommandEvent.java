package com.ithinkrok.minigames.event;

import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;

/**
 * Created by paul on 25/01/16.
 */
public interface CommandEvent extends MinigamesEvent {

    Command getCommand();
    CommandSender getCommandSender();

    boolean isHandled();
    void setHandled(boolean handled);

    boolean isValidCommand();
    void setValidCommand(boolean validCommand);
}
