package com.ithinkrok.minigames.event.team;

import com.ithinkrok.minigames.event.MinigamesEvent;
import com.ithinkrok.minigames.team.Team;

/**
 * Created by paul on 08/01/16.
 */
public class TeamEvent implements MinigamesEvent {

    private final Team team;

    public TeamEvent(Team team) {
        this.team = team;
    }

    public Team getTeam() {
        return team;
    }
}
