package com.ithinkrok.minigames.base.event.user.game;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.user.UserEvent;
import com.ithinkrok.minigames.base.team.Team;

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
