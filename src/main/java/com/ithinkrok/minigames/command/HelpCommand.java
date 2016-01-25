package com.ithinkrok.minigames.command;

import com.ithinkrok.minigames.event.CommandEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

/**
 * Created by paul on 25/01/16.
 */
public class HelpCommand implements Listener {

    @MinigamesEventHandler
    public void onCommand(CommandEvent event) {
        if(!event.getCommand().requireGameGroup(event.getCommandSender())) return;

        //Commands are stored as a TreeMap so no need to sort
        Map<String, CommandConfig> commands = event.getCommand().getGameGroup().getCommands();

        CommandSender sender = event.getCommandSender();
        sender.sendLocale("command.help.title");

        for(CommandConfig command : commands.values()) {
            String usage = command.getUsage();
            usage = usage.replace("<command>", command.getName());

            sender.sendMessageNoPrefix(usage + ": " + command.getDescription());
        }

        //TODO don't show usage unless requested. Split into pages, etc...
    }
}
