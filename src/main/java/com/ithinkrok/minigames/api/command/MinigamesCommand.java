package com.ithinkrok.minigames.api.command;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.util.command.CustomCommand;
import com.ithinkrok.util.lang.Messagable;

import java.util.*;

/**
 * Created by paul on 12/01/16.
 */
public class MinigamesCommand extends CustomCommand {


    private final GameGroup gameGroup;
    private final User user;
    private final TeamIdentifier teamIdentifier;
    private final Kit kit;

    @SuppressWarnings("unchecked")
    public MinigamesCommand(String command, Map<String, Object> params, GameGroup gameGroup, User user,
                            TeamIdentifier teamIdentifier, Kit kit) {
        super(command, params, (List<String>) params.get("default"));

        this.gameGroup = gameGroup;
        this.user = user;
        this.teamIdentifier = teamIdentifier;
        this.kit = kit;
    }


    @Override
    public MinigamesCommand subCommand() {
        if (getArgumentCount() < 1) return null;


        List<Object> newArgs = new ArrayList<>();

        for (int index = 1; index < getArgumentCount(); ++index) newArgs.add(getArg(index, null));

        Map<String, Object> newParams = new HashMap<>(getParameters());
        newParams.put("default", newArgs);

        return new MinigamesCommand(getStringArg(0, null), newParams, gameGroup, user, teamIdentifier, kit);
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
        if (gameGroup != null) return true;

        sender.sendLocale("command.requires.game_group");
        return false;
    }

    public boolean requireArgumentCount(Messagable sender, int minArgs) {
        if (getArgumentCount() >= minArgs) return true;

        sender.sendLocale("command.requires.arguments", minArgs);
        return false;
    }

    public boolean requireUser(Messagable sender) {
        if (user != null) return true;

        sender.sendLocale("command.requires.user");
        return false;
    }

    public boolean requireTeamIdentifier(Messagable sender) {
        if (teamIdentifier != null) return true;

        sender.sendLocale("command.requires.team_identifier");
        return false;
    }

    public boolean requireOthersPermission(MinigamesCommandSender sender, String permission) {
        User userSender = (sender instanceof User) ? (User) sender : null;

        boolean userCheck = (user != null) && (userSender != null) && (user != userSender);
        boolean teamCheck = (teamIdentifier != null) && (userSender != null) &&
                (!Objects.equals(teamIdentifier, userSender.getTeamIdentifier()));
        boolean gameGroupCheck =
                (gameGroup != null) && (userSender != null) && (!Objects.equals(gameGroup, userSender.getGameGroup()));

        if (!userCheck && !teamCheck && !gameGroupCheck) return true;

        return requirePermission(sender, permission);
    }

    public static boolean requirePermission(MinigamesCommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;

        sender.sendLocale("command.requires.permission", permission);
        return false;
    }
}
