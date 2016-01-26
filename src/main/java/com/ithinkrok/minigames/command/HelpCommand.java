package com.ithinkrok.minigames.command;

import com.ithinkrok.minigames.event.CommandEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.*;

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

            if (pluginCommand == null) {
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
        //Commands are stored as a TreeMap so no need to sort
        Map<String, CommandConfig> commands = command.getGameGroup().getCommands();

        Set<String> doneCommands = new HashSet<>();
        List<String> outputLines = new ArrayList<>();

        for (CommandConfig commandConfig : commands.values()) {
            if (!sender.hasPermission(commandConfig.getPermission())) continue;

            if (!doneCommands.add(commandConfig.getName())) continue;
            outputLines.add(command.getGameGroup()
                    .getLocale("command.help.line", commandConfig.getName(), commandConfig.getDescription()));
        }

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {

            Map<String, Map<String, Object>> commandMap = plugin.getDescription().getCommands();
            if(commandMap == null) continue;

            for (String commandName : commandMap.keySet()) {
                PluginCommand pluginCommand = Bukkit.getPluginCommand(commandName);

                if (pluginCommand == null) {
                    System.out.println("Null command: " + commandName);
                    continue;
                }

                if (pluginCommand.getPermission() != null && !sender.hasPermission(pluginCommand.getPermission()))
                    continue;

                if (!doneCommands.add(commandName)) continue;
                outputLines.add(command.getGameGroup()
                        .getLocale("command.help.line", pluginCommand.getName(), pluginCommand.getDescription()));
            }
        }

        Collections.sort(outputLines);

        int page = command.getIntArg(0, 1);
        int maxPage = (outputLines.size() + 7) / 8;
        if(page < 1 || page > maxPage) {
            sender.sendLocale("command.help.invalid_page", page);
        }

        sender.sendLocaleNoPrefix("command.help.title", page, maxPage);

        int maxIndexPlusOne = page * 8;

        for (int index = (page - 1) * 8; index < maxIndexPlusOne && index < outputLines.size(); ++index) {
            sender.sendMessageNoPrefix(outputLines.get(index));
        }
    }
}
