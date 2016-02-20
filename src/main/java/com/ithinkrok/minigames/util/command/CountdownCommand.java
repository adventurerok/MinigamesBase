package com.ithinkrok.minigames.util.command;

import com.ithinkrok.minigames.api.Countdown;
import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 19/01/16.
 */
public class CountdownCommand implements CustomListener {


    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        MinigamesCommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

        if(!command.requireGameGroup(sender)) return;
        if(!command.requireArgumentCount(sender, 1)){
            event.setValidCommand(false);
            return;
        }

        if("start".equals(command.getStringArg(0, null))) {
            if(!command.requireArgumentCount(sender, 4)) {
                event.setValidCommand(false);
                return;
            }

            String name = command.getStringArg(1, null);
            int seconds = command.getIntArg(2, 30);
            String localeStub = command.getStringArg(3, null);

            command.getGameGroup().startCountdown(name, localeStub, seconds);
            sender.sendLocale("command.countdown.started", name, seconds);
            return;
        }

        if(!command.getGameGroup().hasActiveCountdown()) {
            sender.sendLocale("commands.countdown.none");
            return;
        }

        Countdown countdown = command.getGameGroup().getCountdown();

        int amount = command.getIntArg(1, 1);

        switch(command.getStringArg(0, null)) {
            case "add":
                if(!command.requireArgumentCount(sender, 2)) {
                    event.setValidCommand(false);
                    return;
                }
                countdown.setSecondsRemaining(countdown.getSecondsRemaining() + amount);
                sender.sendLocale("command.countdown.added", amount);
                return;
            case "set":
                if(!command.requireArgumentCount(sender, 2)) {
                    event.setValidCommand(false);
                    return;
                }
                countdown.setSecondsRemaining(amount);
                sender.sendLocale("command.countdown.set", amount);
                return;
            case "finish":
                countdown.setSecondsRemaining(1);
                sender.sendLocale("command.countdown.finished");
                return;
            case "cancel":
            case "stop":
                command.getGameGroup().stopCountdown();
                sender.sendLocale("command.countdown.stopped");
                return;
            default:
                event.setValidCommand(false);
        }
    }
}
