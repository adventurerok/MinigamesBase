package com.ithinkrok.minigames.event.user.game;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.team.Team;

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
