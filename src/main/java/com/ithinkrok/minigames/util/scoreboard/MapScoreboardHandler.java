package com.ithinkrok.minigames.util.scoreboard;

import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.util.metadata.MapVote;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardDisplay;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardHandler;
import com.ithinkrok.util.config.Config;

import java.util.List;

/**
 * Created by paul on 23/01/16.
 */
public class MapScoreboardHandler implements ScoreboardHandler {


    private final String displayName;
    private final List<String> votableMaps;

    public MapScoreboardHandler(User user) {
        Config config = user.getSharedObject("map_scoreboard");

        String displayNameLocale = config.getString("title", "map_scoreboard.title");
        displayName = user.getLanguageLookup().getLocale(displayNameLocale);

        votableMaps = config.getStringList("votable_maps");
    }

    @Override
    public void updateScoreboard(User user, ScoreboardDisplay scoreboard) {
        for(String map : votableMaps) {
            int score = MapVote.getVotesForMap(user.getGameGroup().getUsers(), map);

            if(score == 0) scoreboard.removeScore(map);
            else scoreboard.setScore(map, score);
        }
    }

    @Override
    public void setupScoreboard(User user, ScoreboardDisplay scoreboard) {
        scoreboard.setDisplayName(displayName);
        scoreboard.resetAndDisplay();
    }
}
