package com.ithinkrok.minigames.hub.scoreboard;

import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardDisplay;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardHandler;
import com.ithinkrok.util.config.Config;

/**
 * Created by paul on 18/09/16.
 */
public class HubScoreboardHandler implements ScoreboardHandler {

    public HubScoreboardHandler(Config config) {

    }

    @Override
    public void updateScoreboard(User user, ScoreboardDisplay scoreboard) {
        scoreboard.setTextLine(0, "Cow");
        scoreboard.setTextLine(1, "Killed");
        scoreboard.setTextLine(2, "Pig");
    }

    @Override
    public void setupScoreboard(User user, ScoreboardDisplay scoreboard) {
        scoreboard.setTextLineCount(3);

        updateScoreboard(user, scoreboard);
    }
}
