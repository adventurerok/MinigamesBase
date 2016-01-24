package com.ithinkrok.minigames.user.scoreboard;

import com.ithinkrok.minigames.User;

/**
 * Created by paul on 06/01/16.
 */
public interface ScoreboardHandler {

    void updateScoreboard(User user, ScoreboardDisplay scoreboard);
    void setupScoreboard(User user, ScoreboardDisplay scoreboard);

}
