package com.ithinkrok.minigames.api.user.scoreboard;

import com.ithinkrok.minigames.api.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 06/01/16.
 */
public class ScoreboardDisplay {

    private final User user;
    private final Player player;

    public ScoreboardDisplay(User user, Player player) {
        this.user = user;
        this.player = player;
    }

    private Scoreboard scoreboard;
    private Objective objective;

    private String displayName = "Set display name";

    private final List<Team> fakeTeams = new ArrayList<>();

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        if(objective != null) objective.setDisplayName(displayName);
    }

    public void setDisplayLocale(String displayLocale, Object...args) {
        setDisplayName(user.getLanguageLookup().getLocale(displayLocale, args));
    }

    public boolean isDisplaying() {
        return objective != null;
    }

    public void setTextLine(int line, String text) {
        if(line >= fakeTeams.size()){
            throw new RuntimeException("Wrong scoreboard mode or invalid line number: " + line);
        }


        if(text == null || text.isEmpty()) fakeTeams.get(line).setPrefix(emptyString(line));
        else {
            //Prevent issues with text longer than 16 which throws an exception on Bukkit
            if(text.length() > 16) {
                text = text.substring(0, 16);
            }

            fakeTeams.get(line).setPrefix(text);
        }
    }

    public void setTextLocale(int line, String locale, Object...args) {
        setTextLine(line, user.getLanguageLookup().getLocale(locale, args));
    }

    public int getTextLineCount() {
        return fakeTeams.size();
    }

    public void setTextLineCount(int lineCount) {
        while(fakeTeams.size() > lineCount) {
            Team fake = fakeTeams.remove(fakeTeams.size() - 1);
            fake.unregister();
        }

        while(fakeTeams.size() < lineCount) {
            Team fake = scoreboard.registerNewTeam(emptyString(fakeTeams.size()));
            fake.addEntry(emptyString(fakeTeams.size()));

            objective.getScore(emptyString(fakeTeams.size())).setScore(99 - fakeTeams.size());

            fakeTeams.add(fake);
        }
    }

    public void setScore(String label, int score) {
        objective.getScore(label).setScore(score);

    }

    public void removeScore(String label) {
        scoreboard.resetScores(label);
    }

    public void remove() {
        if(objective != null) {
            objective.unregister();
            objective = null;
        }
    }

    public void resetAndDisplay() {
        remove();

        if(scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        setTextLineCount(0);
        scoreboard.getTeams().forEach(Team::unregister);

        objective = scoreboard.registerNewObjective("main", "dummy");
        objective.setDisplayName(displayName);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private String emptyString(int number) {
        StringBuilder result = new StringBuilder(Integer.toHexString(number));

        result.insert(0, ChatColor.COLOR_CHAR);
        if(number > 0xf) result.insert(2, ChatColor.COLOR_CHAR);

        return result.toString();
    }
}
