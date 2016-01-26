package com.ithinkrok.minigames.command;

import com.ithinkrok.minigames.event.CommandEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 25/01/16.
 */
public class HelpCommand implements Listener {

    @MinigamesEventHandler
    public void onCommand(CommandEvent event) {
        if (!event.getCommand().requireGameGroup(event.getCommandSender())) return;

        Command command = event.getCommand();
        CommandSender sender = event.getCommandSender();

        if (event.getCommand().hasArg(0)) {
            onUsageCommand(sender, command);
        } else {
            onListCommand(sender, command);
        }


        //TODO Split into pages
    }

    private void onUsageCommand(CommandSender sender, Command command) {
        String commandName = command.getStringArg(0, "unspecified").toLowerCase();
        CommandConfig commandConfig = command.getGameGroup().getCommand(commandName);

        String usage, desc;

        if (commandConfig == null) {
            PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);

            if(pluginCommand == null) {
                sender.sendLocale("command.help.unknown", commandName);
                return;
            }

            usage = pluginCommand.getUsage();
            desc = pluginCommand.getDescription();
        } else {
            usage = commandConfig.getUsage();
            desc = commandConfig.getDescription();
        }

        usage = usage.replace("<command>", commandName);

        sender.sendLocaleNoPrefix("command.help.usage", commandName, usage, desc);
    }

    private void onListCommand(CommandSender sender, Command command) {
        sender.sendLocaleNoPrefix("command.help.title");
        //Commands are stored as a TreeMap so no need to sort
        Map<String, CommandConfig> commands = command.getGameGroup().getCommands();

        List<String> outputLines = new ArrayList<>();

        for (CommandConfig commandConfig : commands.values()) {
            if (!sender.hasPermission(commandConfig.getPermission())) continue;

            outputLines.add(command.getGameGroup()
                    .getLocale("command.help.line", commandConfig.getName(), commandConfig.getDescription()));
        }

        for (String commandName : Bukkit.getCommandAliases().keySet()) {
            PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);

            if (pluginCommand.getPermission() != null && !sender.hasPermission(pluginCommand.getPermission())) continue;

            outputLines.add(command.getGameGroup()
                    .getLocale("command.help.line", pluginCommand.getName(), pluginCommand.getDescription()));
        }

        Collections.sort(outputLines);

        for (String line : outputLines) {
            sender.sendMessageNoPrefix(line);
        }
    }
}
