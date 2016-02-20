package com.ithinkrok.minigames.base.team;

import com.ithinkrok.minigames.api.Team;

/**
 * Created by paul on 16/01/16.
 */
public interface TeamResolver {

    Team getTeam(String name);
    Team getTeam(TeamIdentifier identifier);
    TeamIdentifier getTeamIdentifier(String name);
}
