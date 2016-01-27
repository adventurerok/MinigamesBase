package com.ithinkrok.minigames.base.metadata;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.game.MapChangedEvent;
import com.ithinkrok.minigames.base.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;

import java.util.*;

/**
 * Created by paul on 04/01/16.
 */
public class MapVote extends UserMetadata {

    private static final Random random = new Random();

    private String mapVote;
    private int voteWeight;

    public MapVote(User user, String vote) {
        mapVote = vote;

        voteWeight = 1;

        int next = 2;
        while(user.hasPermission("minigames.map_vote.weight." + next) && next <= 10) {
            voteWeight = next++;
        }

    }

    public int getVoteWeight() {
        return voteWeight;
    }

    public String getMapVote() {
        return mapVote;
    }

    @Override
    public boolean removeOnInGameChange(UserInGameChangeEvent event) {
        return false;
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent event) {
        return true;
    }

    public static String getWinningVote(Collection<User> users) {
        Map<String, Integer> votes = new HashMap<>();

        for(User user : users) {
            if(!user.hasMetadata(MapVote.class)) continue;

            MapVote vote = user.getMetadata(MapVote.class);
            if(votes.containsKey(vote.mapVote)){
                votes.put(vote.mapVote, votes.get(vote.mapVote) + vote.voteWeight);
            } else votes.put(vote.mapVote, vote.voteWeight);
        }

        List<String> winningMaps = new ArrayList<>();
        int highestVotes = 0;

        for(Map.Entry<String, Integer> entry : votes.entrySet()) {
            if(entry.getValue() < highestVotes) continue;
            if(entry.getValue() == highestVotes) winningMaps.add(entry.getKey());
            else {
                highestVotes = entry.getValue();
                winningMaps.clear();
                winningMaps.add(entry.getKey());
            }
        }

        if(winningMaps.isEmpty()) return null;
        else return winningMaps.get(random.nextInt(winningMaps.size()));
    }

    public static int getVotesForMap(Collection<User> users, String map) {
        int count = 0;

        for(User user : users) {
            if(!user.hasMetadata(MapVote.class)) continue;

            MapVote vote = user.getMetadata(MapVote.class);

            if(Objects.equals(vote.mapVote, map)) ++count;
        }

        return count;
    }
}
