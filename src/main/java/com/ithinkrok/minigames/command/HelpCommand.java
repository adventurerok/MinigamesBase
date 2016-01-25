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
        if (!event.getCommand().requireGameGroup(event.getCommandSender())) return;


        CommandSender sender = event.getCommandSender();

        if (event.getCommand().hasArg(0)) {
            String commandName = event.getCommand().getStringArg(0, "unspecified").toLowerCase();
            CommandConfig commandConfig = event.getCommand().getGameGroup().getCommand(commandName);

            if (commandConfig == null) {
                sender.sendLocale("command.help.unknown", commandName);
                return;
            }

            String usage = commandConfig.getUsage().replace("<command>", commandName);
            String desc = commandConfig.getDescription();

            sender.sendLocaleNoPrefix("command.help.usage", commandName, usage, desc);
        } else {
            sender.sendLocaleNoPrefix("command.help.title");
            //Commands are stored as a TreeMap so no need to sort
            Map<String, CommandConfig> commands = event.getCommand().getGameGroup().getCommands();

            for (CommandConfig command : commands.values()) {
                sender.sendLocaleNoPrefix("command.help.line", command.getName(), command.getDescription());
            }
        }


        //TODO Split into pages
    }
}
