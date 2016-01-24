package com.ithinkrok.minigames.command;

import com.ithinkrok.minigames.Game;
import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.Kit;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.team.TeamIdentifier;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 12/01/16.
 */
public class GameCommandHandler implements CommandExecutor {

    private final Game game;
    private Map<String, GameCommandExecutor> executors = new HashMap<>();

    public GameCommandHandler(Game game) {
        this.game = game;
    }

    public void addExecutor(GameCommandExecutor executor, String...commandNames) {
        for(String commandName : commandNames) {
            executors.put(commandName, executor);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!executors.containsKey(command.getName().toLowerCase())) return false;

        List<String> correctedArgs = mergeArgumentsInQuotes(args);

        Map<String, Object> arguments = parseArgumentListToMap(correctedArgs);

        User user = null;
        GameGroup gameGroup = null;
        TeamIdentifier teamIdentifier = null;

        User userSender = null;
        if (sender instanceof Player) {
            user = userSender = game.getUser(((Player) sender).getUniqueId());
        }

        if (arguments.containsKey("u")) {
            OfflinePlayer player = Bukkit.getPlayer(arguments.get("u").toString());
            if (player != null) user = game.getUser(player.getUniqueId());
        }

        if(user != null) {
            gameGroup = user.getGameGroup();
            teamIdentifier = user.getTeamIdentifier();
        }

        if(arguments.containsKey("t")) {
            teamIdentifier = game.getTeamIdentifier(arguments.get("t").toString());
        }

        if(gameGroup == null) gameGroup = game.getSpawnGameGroup();

        Kit kit = null;
        if(arguments.containsKey("k")) {
            kit = game.getKit(arguments.get("k").toString());
        }

        Command gameCommand = new Command(command.getName(), arguments, gameGroup, user, teamIdentifier, kit);

        com.ithinkrok.minigames.command.CommandSender messagable;
        if(userSender != null) messagable = userSender;
        else messagable = new ConsoleSender(game);

        return executors.get(command.getName()).onCommand(messagable, gameCommand);
    }

    private List<String> mergeArgumentsInQuotes(String[] args) {
        List<String> correctedArgs = new ArrayList<>();

        StringBuilder currentArg = new StringBuilder();

        boolean inQuote = false;

        for (String arg : args) {
            if(currentArg.length() > 0) currentArg.append(' ');
            currentArg.append(arg.replace("\"", ""));

            int quoteCount = StringUtils.countMatches(arg, "\"");
            if (((quoteCount & 1) == 1)) inQuote = !inQuote;

            if (!inQuote) {
                correctedArgs.add(currentArg.toString());
                currentArg = new StringBuilder();
            }
        }
        return correctedArgs;
    }

    private Map<String, Object> parseArgumentListToMap(List<String> correctedArgs) {
        List<Object> defaultArguments = new ArrayList<>();
        Map<String, Object> arguments = new HashMap<>();

        String key = null;

        for (String arg : correctedArgs) {
            if (arg.startsWith("-") && arg.length() > 1) {
                key = arg.substring(1);
            } else {
                if(key != null) arguments.put(key, parse(arg));
                else defaultArguments.add(parse(arg));
                key = null;
            }
        }

        arguments.put("default", defaultArguments);
        return arguments;
    }

    private Object parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
        }

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ignored) {
        }

        switch (s.toLowerCase()) {
            case "true":
            case "yes":
                return true;
            case "false":
            case "no":
                return false;
        }

        return s;
    }

    private static class ConsoleSender implements com.ithinkrok.minigames.command.CommandSender {

        private static ConsoleCommandSender consoleCommandSender = Bukkit.getConsoleSender();
        private final Game game;

        public ConsoleSender(Game game) {
            this.game = game;
        }

        @Override
        public void sendMessage(String message) {
            sendMessageNoPrefix(game.getChatPrefix() + message);
        }

        @Override
        public void sendMessageNoPrefix(String message) {
            consoleCommandSender.sendMessage(message);
        }

        @Override
        public void sendLocale(String locale, Object... args) {
            sendMessage(game.getLocale(locale, args));
        }

        @Override
        public void sendLocaleNoPrefix(String locale, Object... args) {
            sendMessageNoPrefix(game.getLocale(locale, args));
        }

        @Override
        public LanguageLookup getLanguageLookup() {
            return game;
        }

        @Override
        public boolean hasPermission(String name) {
            return true;
        }
    }
}
