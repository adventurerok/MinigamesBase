package com.ithinkrok.minigames.api.event.team;

import com.ithinkrok.minigames.api.event.MinigamesEvent;
import com.ithinkrok.minigames.api.team.Team;

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
