package com.ithinkrok.minigames.api.user.scoreboard;

import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 06/01/16.
 */
public interface ScoreboardHandler {

    void updateScoreboard(User user, ScoreboardDisplay scoreboard);
    void setupScoreboard(User user, ScoreboardDisplay scoreboard);

}
