package com.ithinkrok.minigames.command;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.Kit;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.team.TeamIdentifier;

import java.util.*;

/**
 * Created by paul on 12/01/16.
 */
public class Command {

    private final String command;
    private final Map<String, Object> params;
    private final List<Object> defaultArgs;

    private final GameGroup gameGroup;
    private final User user;
    private final TeamIdentifier teamIdentifier;
    private final Kit kit;

    @SuppressWarnings("unchecked")
    public Command(String command, Map<String, Object> params, GameGroup gameGroup, User user,
                   TeamIdentifier teamIdentifier, Kit kit) {
        this.command = command;
        this.params = params;
        this.gameGroup = gameGroup;
        this.user = user;
        this.teamIdentifier = teamIdentifier;
        this.kit = kit;

        this.defaultArgs = (List<Object>) params.get("default");
    }

    public List<Object> getDefaultArgs() {
        return defaultArgs;
    }

    public boolean hasArg(int index) {
        return defaultArgs.size() > index;
    }

    public double getDoubleArg(int index, double def) {
        try {
            return ((Number) defaultArgs.get(index)).doubleValue();
        } catch (Exception e) {
            return def;
        }
    }

    public int getIntArg(int index, int def) {
        try {
            return ((Number) defaultArgs.get(index)).intValue();
        } catch (Exception e) {
            return def;
        }
    }

    public boolean getBooleanArg(int index, boolean def) {
        try {
            return ((Boolean) defaultArgs.get(index));
        } catch (Exception e) {
            return def;
        }
    }

    public String getStringArg(int index, String def) {
        if(index >= defaultArgs.size()) return def;
        Object o = defaultArgs.get(index);

        return o != null ? o.toString() : def;
    }

    public boolean hasParameter(String name) {
        return params.containsKey(name);
    }

    public double getDoubleParam(String name, double def) {
        try {
            return ((Number) params.get(name)).doubleValue();
        } catch (Exception e) {
            return def;
        }
    }

    public int getIntParam(String name, int def) {
        try {
            return ((Number) params.get(name)).intValue();
        } catch (Exception e) {
            return def;
        }
    }

    public Command subCommand() {
        if(defaultArgs.size() < 1) return null;

        List<Object> newArgs = new ArrayList<>();

        for(int index = 1; index < defaultArgs.size(); ++index) newArgs.add(defaultArgs.get(index));

        Map<String, Object> newParams = new HashMap<>(params);
        newParams.put("default", newArgs);

        return new Command(getStringArg(0, null), newParams, gameGroup, user, teamIdentifier, kit);
    }

    public boolean getBooleanParam(String name, boolean def) {
        try{
            return (Boolean) params.get(name);
        } catch (Exception e) {
            return def;
        }
    }

    public String getStringParam(String name, String def) {
        Object o = params.get(name);
        return o != null ? o.toString() : def;
    }

    public String getCommand() {
        return command;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }

    public User getUser() {
        return user;
    }

    public TeamIdentifier getTeamIdentifier() {
        return teamIdentifier;
    }

    public Kit getKit() {
        return kit;
    }

    public boolean requireGameGroup(Messagable sender) {
        if(gameGroup != null) return true;

        sender.sendLocale("command.requires.game_group");
        return false;
    }

    public boolean requireArgumentCount(Messagable sender, int minArgs) {
        if(defaultArgs.size() >= minArgs) return true;

        sender.sendLocale("command.requires.arguments", minArgs);
        return false;
    }

    public boolean requireUser(Messagable sender) {
        if(user != null) return true;

        sender.sendLocale("command.requires.user");
        return false;
    }

    public boolean requireTeamIdentifier(Messagable sender) {
        if(teamIdentifier != null) return true;

        sender.sendLocale("command.requires.team_identifier");
        return false;
    }

    public boolean requireOthersPermission(CommandSender sender, String permission) {
        User userSender = (sender instanceof User) ? (User) sender : null;

        boolean userCheck = (user != null) && (userSender != null) && (user != userSender);
        boolean teamCheck = (teamIdentifier != null) && (userSender != null) && (!Objects.equals(teamIdentifier,
                userSender.getTeamIdentifier()));
        boolean gameGroupCheck = (gameGroup != null) && (userSender != null) && (!Objects.equals(gameGroup,
                userSender.getGameGroup()));

        if(!userCheck && !teamCheck && !gameGroupCheck) return true;

        return requirePermission(sender, permission);
    }

    public static boolean requirePermission(CommandSender sender, String permission) {
        if(sender.hasPermission(permission)) return true;

        sender.sendLocale("command.requires.permission", permission);
        return false;
    }
}
