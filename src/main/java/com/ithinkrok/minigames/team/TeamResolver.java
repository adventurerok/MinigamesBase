package com.ithinkrok.minigames.team;

/**
 * Created by paul on 16/01/16.
 */
public interface TeamResolver {

    Team getTeam(String name);
    Team getTeam(TeamIdentifier identifier);
    TeamIdentifier getTeamIdentifier(String name);
}
