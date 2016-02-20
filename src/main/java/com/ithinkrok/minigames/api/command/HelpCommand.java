package com.ithinkrok.minigames.api.command;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Created by paul on 25/01/16.
 */
public class HelpCommand implements CustomListener {


    private boolean showBukkitCommands;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        showBukkitCommands = event.getConfigOrEmpty().getBoolean("all", true);
    }

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        if (!event.getCommand().requireGameGroup(event.getCommandSender())) return;

        MinigamesCommand command = event.getCommand();
        MinigamesCommandSender sender = event.getCommandSender();

        if (event.getCommand().hasArg(0) && !event.getCommand().hasIntArg(0)) {
            onUsageCommand(sender, command);
        } else {
            onListCommand(sender, command);
        }

    }

    private void onUsageCommand(MinigamesCommandSender sender, MinigamesCommand command) {
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

    private void onListCommand(MinigamesCommandSender sender, MinigamesCommand command) {
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

        if(showBukkitCommands) {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {

                Map<String, Map<String, Object>> commandMap = plugin.getDescription().getCommands();
                if (commandMap == null) continue;

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

        if(page < maxPage) {
            sender.sendLocaleNoPrefix("command.help.next_page", page + 1);
        } else sender.sendLocaleNoPrefix("command.help.end");
    }
}
