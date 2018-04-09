package com.ithinkrok.minigames.util.gamestate;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.util.ItemGiver;
import com.ithinkrok.minigames.util.metadata.GameTimer;
import com.ithinkrok.minigames.util.metadata.MapVote;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

import java.util.*;

/**
 * Created by paul on 30/01/16.
 */
public class SimpleGameStartListener implements CustomListener {

    protected final Random random = new Random();

    private String randomMapName;
    private String disableMapName;
    private List<String> mapList;
    private List<String> teamList;
    private List<String> kitList;
    private String lobbyGameState;

    private final Set<UUID> gamePlayers = new HashSet<>();

    private String teamInfoLocale, kitInfoLocale;

    private ItemGiver itemGiver;

    protected GameState gameState;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        gameState = event.getRepresenting();

        Config config = event.getConfigOrEmpty();

        lobbyGameState = config.getString("lobby_gamestate", "lobby");

        teamList = config.getStringList("choosable_teams");
        kitList = config.getStringList("choosable_kits");

        configureMapVoting(config.getConfigOrEmpty("map_voting"));

        itemGiver = new ItemGiver(config.getConfigOrNull("start_items"));

        teamInfoLocale = config.getString("team_info_locale", "start_info.team");
        kitInfoLocale = config.getString("kit_info_locale", "start_info.kit");
    }

    private void configureMapVoting(Config config) {
        randomMapName = config.getString("random_map", "random");
        disableMapName = config.getString("disable_map", "disable");

        mapList = new ArrayList<>(config.getStringList("map_list"));
        mapList.remove(randomMapName);

        if(mapList.size() < 1) throw new RuntimeException("The game requires at least one map!");
    }

    @CustomEventHandler
    public void onGameStateChange(GameStateChangedEvent event) {
        if(!Objects.equals(event.getNewGameState(), gameState)) return;

        if(event.getOldGameState() != null && !event.getOldGameState().getName().equals(lobbyGameState)) return;

        //Stop accepting players
        event.getGameGroup().setAcceptingPlayers(false);
        GameTimer.getOrCreate(event.getGameGroup());

        String mapName = assignGameMap(event.getGameGroup());
        if(!mapName.equals(disableMapName)) {
            event.getGameGroup().changeMap(mapName);
        }

        postMapLoad(event.getGameGroup());

        GameGroup gameGroup = event.getGameGroup();
        gameGroup.getUsers().forEach(this::setupUser);

    }

    protected void postMapLoad(GameGroup gameGroup){

    }


    protected void setupUser(User user) {
        user.decloak();

        if(user.getTeam() == null) {
            user.setTeam(assignUserTeam(user.getGameGroup()));
        }

        if(user.getKit() == null) {
            user.setKit(assignUserKit(user.getGameGroup()));
        }

        System.out.println("User " + user.getName() + " playing on " + user.getTeamName() + " team as " + user.getKitName());

        if(teamInfoLocale != null && user.getTeam() != null) {
            user.sendLocale(teamInfoLocale, user.getTeamIdentifier().getFormattedName());
        }

        if(kitInfoLocale != null && user.getKit() != null) {
            user.sendLocale(kitInfoLocale, user.getKit().getFormattedName());
        }

        user.setInGame(true);
        user.resetUserStats(true);
        user.setCollidesWithEntities(true);
        user.setScoreboardHandler(null);

        itemGiver.giveToUser(user);

        gamePlayers.add(user.getUuid());
    }

    protected Kit assignUserKit(GameGroup gameGroup) {
        if(kitList == null || kitList.isEmpty()) return null;
        String kitName = kitList.get(random.nextInt(kitList.size()));

        return gameGroup.getKit(kitName);
    }

    protected Team assignUserTeam(GameGroup gameGroup) {
        if(teamList == null || teamList.isEmpty()) return null;

        ArrayList<Team> smallest = new ArrayList<>();
        int leastCount = Integer.MAX_VALUE;

        for(String teamName : teamList) {
            Team team = gameGroup.getTeam(teamName);
            if(team.getUserCount() < leastCount) {
                leastCount = team.getUserCount();
                smallest.clear();
            }

            if(team.getUserCount() == leastCount) smallest.add(team);
        }

        return smallest.get(random.nextInt(smallest.size()));
    }

    protected String assignGameMap(GameGroup gameGroup) {
        String winningVote = MapVote.getWinningVote(gameGroup.getUsers());

        if(winningVote == null || winningVote.equals(randomMapName)) {
            winningVote = mapList.get(random.nextInt(mapList.size()));
        }

        return winningVote;
    }

    @CustomEventHandler
    public void onUserInGameChange(UserInGameChangeEvent event) {
        if(!event.isInGame() && gamePlayers.contains(event.getUser().getUuid())) {

            //Give the participation reward.
            //Doing it this way in the future ensures that quitters don't get their reward

            event.getUser().doInFuture(task -> {
                event.getGameGroup().getRewarder().giveParticipationReward(event.getUser());
            });
        }
    }
}
