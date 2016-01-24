package com.ithinkrok.minigames.event.game;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;

/**
 * Created by paul on 24/01/16.
 */
public class GameCommandEvent extends GameEvent {

    private final CommandSender sender;
    private final Command command;

    /**
     * This is true by default if there is a CommandConfig for this command.
     */
    private boolean handled = false;

    /**
     * An invalid command will show the incorrect usage method after it is called.
     */
    private boolean validCommand = true;

    public GameCommandEvent(GameGroup gameGroup, CommandSender sender, Command command) {
        super(gameGroup);
        this.sender = sender;
        this.command = command;
    }

    public CommandSender getSender() {
        return sender;
    }

    public Command getCommand() {
        return command;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public boolean isValidCommand() {
        return validCommand;
    }

    public void setValidCommand(boolean validCommand) {
        this.validCommand = validCommand;
    }
}
