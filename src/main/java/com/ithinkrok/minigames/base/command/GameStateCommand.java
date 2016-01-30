package com.ithinkrok.minigames.base.command;

import com.ithinkrok.minigames.base.GameState;
import com.ithinkrok.minigames.base.command.Command;
import com.ithinkrok.minigames.base.command.CommandSender;
import com.ithinkrok.minigames.base.event.CommandEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import org.bukkit.event.Listener;

/**
 * Created by paul on 13/01/16.
 */
public class GameStateCommand implements Listener {


    @MinigamesEventHandler
    public void onCommand(CommandEvent event) {
        CommandSender sender = event.getCommandSender();
        Command command = event.getCommand();

        if(!command.requireGameGroup(sender) || !command.requireArgumentCount(sender, 1)){
            event.setValidCommand(false);
            return;
        }
        String gameStateName = command.getStringArg(0, null);

        GameState gameState = command.getGameGroup().getGameState(gameStateName);
        if(gameState == null) {
            sender.sendLocale("command.gamestate.unknown", gameStateName);
            return;
        }

        command.getGameGroup().changeGameState(gameState);
        sender.sendLocale("command.gamestate.changed", gameStateName);
    }
}
