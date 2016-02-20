package com.ithinkrok.minigames.api.event;

import com.ithinkrok.minigames.base.command.MinigamesCommand;
import com.ithinkrok.minigames.base.command.MinigamesCommandSender;
import com.ithinkrok.util.command.event.CustomCommandEvent;

/**
 * Created by paul on 25/01/16.
 */
public interface MinigamesCommandEvent extends MinigamesEvent, CustomCommandEvent {

    MinigamesCommand getCommand();
    MinigamesCommandSender getCommandSender();

}
