package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 06/01/16.
 */
public class UserChangeTeamEvent extends UserEvent {

    private final Team oldTeam;
    private final Team newTeam;

    public UserChangeTeamEvent(User user, Team oldTeam, Team newTeam) {
        super(user);
        this.oldTeam = oldTeam;
        this.newTeam = newTeam;
    }

    public Team getOldTeam() {
        return oldTeam;
    }

    public Team getNewTeam() {
        return newTeam;
    }
}
