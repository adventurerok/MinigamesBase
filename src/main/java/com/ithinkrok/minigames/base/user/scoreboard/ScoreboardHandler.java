package com.ithinkrok.minigames.base.user.scoreboard;

import com.ithinkrok.minigames.base.User;

/**
 * Created by paul on 06/01/16.
 */
public interface ScoreboardHandler {

    void updateScoreboard(User user, ScoreboardDisplay scoreboard);
    void setupScoreboard(User user, ScoreboardDisplay scoreboard);

}
