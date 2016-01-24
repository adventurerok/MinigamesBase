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

    public static List<String> splitStringIntoArguments(String command) {
        if (command.startsWith("/")) command = command.substring(1);

        boolean wasBackslash = false;
        boolean inQuotes = false;

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int index = 0; index < command.length(); ++index) {
            char c = command.charAt(index);

            if (wasBackslash) {
                current.append(c);
                wasBackslash = false;
                continue;
            }

            switch (c) {
                case '\\':
                    wasBackslash = true;
                    break;
                case '"':
                    inQuotes = !inQuotes;
                    break;
                case ' ':
                    if (inQuotes) {
                        current.append(' ');
                        break;
                    }

                    if (current.length() < 1) break;
                    result.add(current.toString());
                    current = new StringBuilder();
                    break;
                default:
                    current.append(c);
            }
        }

        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    public void addExecutor(GameCommandExecutor executor, String... commandNames) {
        for (String commandName : commandNames) {
            executors.put(commandName, executor);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!executors.containsKey(command.getName().toLowerCase())) return false;

        List<String> correctedArgs = mergeArgumentsInQuotes(args);

        Map<String, Object> arguments = parseArgumentListToMap(correctedArgs);

        User user = null;
        GameGroup gameGroup = game.getSpawnGameGroup();
        TeamIdentifier teamIdentifier = null;

        User userSender = null;
        if (sender instanceof Player) {
            user = userSender = game.getUser(((Player) sender).getUniqueId());
        } else if (arguments.containsKey("u")) {
            OfflinePlayer player = Bukkit.getPlayer(arguments.get("u").toString());
            if (player != null) user = game.getUser(player.getUniqueId());
        }

        if (user != null) {
            gameGroup = user.getGameGroup();
            teamIdentifier = user.getTeamIdentifier();
        }

        if (gameGroup == null) gameGroup = game.getSpawnGameGroup();

        if (arguments.containsKey("t")) {
            teamIdentifier = gameGroup.getTeamIdentifier(arguments.get("t").toString());
        }

        Kit kit = null;
        if (arguments.containsKey("k")) {
            kit = gameGroup.getKit(arguments.get("k").toString());
        }

        Command gameCommand = new Command(command.getName(), arguments, gameGroup, user, teamIdentifier, kit);

        com.ithinkrok.minigames.command.CommandSender messagable;
        if (userSender != null) messagable = userSender;
        else messagable = new ConsoleSender(gameGroup);

        return executors.get(command.getName()).onCommand(messagable, gameCommand);
    }

    /**
     * Fixes arguments lists that contain quotes, that were parsed by Bukkit instead of
     * splitStringIntoArguments().
     *
     * @param args The arguments to fix
     * @return The corrected arguments
     */
    public static List<String> mergeArgumentsInQuotes(String[] args) {
        List<String> correctedArgs = new ArrayList<>();

        StringBuilder currentArg = new StringBuilder();

        boolean inQuote = false;

        for (String arg : args) {
            if (currentArg.length() > 0) currentArg.append(' ');
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

    public static Map<String, Object> parseArgumentListToMap(List<String> correctedArgs) {
        List<Object> defaultArguments = new ArrayList<>();
        Map<String, Object> arguments = new HashMap<>();

        String key = null;

        for (String arg : correctedArgs) {
            if (arg.startsWith("-") && arg.length() > 1) {
                key = arg.substring(1);
            } else {
                if (key != null) arguments.put(key, parse(arg));
                else defaultArguments.add(parse(arg));
                key = null;
            }
        }

        arguments.put("default", defaultArguments);
        return arguments;
    }

    private static Object parse(String s) {
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
        private final GameGroup gameGroup;

        private ConsoleSender(GameGroup gameGroup) {
            this.gameGroup = gameGroup;
        }

        @Override
        public void sendLocale(String locale, Object... args) {
            sendMessage(gameGroup.getLocale(locale, args));
        }

        @Override
        public void sendMessage(String message) {
            sendMessageNoPrefix(gameGroup.getChatPrefix() + message);
        }

        @Override
        public void sendMessageNoPrefix(String message) {
            consoleCommandSender.sendMessage(message);
        }

        @Override
        public void sendLocaleNoPrefix(String locale, Object... args) {
            sendMessageNoPrefix(gameGroup.getLocale(locale, args));
        }

        @Override
        public LanguageLookup getLanguageLookup() {
            return gameGroup;
        }

        @Override
        public boolean hasPermission(String name) {
            return true;
        }
    }
}
