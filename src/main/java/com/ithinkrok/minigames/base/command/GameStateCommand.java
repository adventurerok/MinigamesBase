package com.ithinkrok.minigames.base.command;

import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 13/01/16.
 */
public class GameStateCommand implements CustomListener {


    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        MinigamesCommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

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
