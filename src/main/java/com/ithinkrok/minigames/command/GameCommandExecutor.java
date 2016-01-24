package com.ithinkrok.minigames.command;

/**
 * Created by paul on 12/01/16.
 */
public interface GameCommandExecutor {

    boolean onCommand(CommandSender sender, Command command);
}
