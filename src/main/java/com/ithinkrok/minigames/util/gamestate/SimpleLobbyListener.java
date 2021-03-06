package com.ithinkrok.minigames.util.gamestate;

import com.ithinkrok.minigames.api.Countdown;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.api.event.game.CountdownMessageEvent;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.map.MapCreatureSpawnEvent;
import com.ithinkrok.minigames.api.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.api.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.api.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.api.event.user.world.*;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.CountdownConfig;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.util.ItemGiver;
import com.ithinkrok.minigames.util.scoreboard.MapScoreboardHandler;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Objects;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by paul on 30/01/16.
 */
public class SimpleLobbyListener implements CustomListener {

    protected String quitLocale;
    protected String joinLocale;

    protected GameState gameState;

    private CountdownConfig startCountdown;

    private String needsMorePlayersLocale;
    private int minPlayersToStartGame;

    private String nextGameState;

    private String joinLobbyLocaleStub;

    private ItemGiver giveOnJoin;
    private Config config;
    
    private String waitingForPlayersLocale;
    private String starting5Locale;
    private String starting10Locale;
    private String starting30Locale;
    private String starting60Locale;
    private String starting120Locale;
    private String startingEventuallyLocale;
    private String startedLocale;

    private boolean teleportToSpawn;
    private boolean useMapScoreboard;

    private TreeMap<Double, String> possibleLobbyMaps = new TreeMap<>();

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        gameState = event.getRepresenting();

        config = event.getConfig();

        nextGameState = config.getString("next_gamestate");

        useMapScoreboard = config.getBoolean("use_map_scoreboard", true);
        teleportToSpawn = config.getBoolean("teleport_to_spawn", true);

        if(config.contains("start_countdown")) {
            configureCountdown(config.getConfigOrEmpty("start_countdown"));
        }

        String lobbyMapName = config.getString("lobby_map");
        if(lobbyMapName != null) {
            possibleLobbyMaps.put(1.0, lobbyMapName);
        } else {
            Config multiMapConfig = config.getConfigOrEmpty("lobby_maps");
            double low = 0.0;
            for(String mapName : multiMapConfig.getKeys(false)) {
                low += multiMapConfig.getDouble(mapName);
                possibleLobbyMaps.put(low, mapName);
            }
        }

        giveOnJoin = new ItemGiver(config.getConfigOrEmpty("give_on_join"));

        joinLobbyLocaleStub = config.getString("join_lobby_locale_stub", "lobby.info");

        quitLocale = config.getString("user_quit_lobby_locale", "user.quit.lobby");
        joinLocale = config.getString("user_join_lobby_locale", "user.join.lobby");
        
        waitingForPlayersLocale = config.getString("motd.waiting_locale", "motd.waiting");
        starting5Locale = config.getString("motd.starting_5_locale", "motd.starting_5");
        starting10Locale = config.getString("motd.starting_10_locale", "motd.starting_10");
        starting30Locale = config.getString("motd.starting_30_locale", "motd.starting_30");
        starting60Locale = config.getString("motd.starting_60_locale", "motd.starting_60");
        starting120Locale = config.getString("motd.starting_120_locale", "motd.starting_120");
        startingEventuallyLocale = config.getString("motd.starting_later_locale", "motd.starting_later");
        startedLocale = config.getString("motd.started_locale", "motd.started");
    }


    private void configureCountdown(Config config) {
        startCountdown = MinigamesConfigs.getCountdown(config, "");
        minPlayersToStartGame = config.getInt("min_players");
        needsMorePlayersLocale = config.getString("needs_more_players_locale");
    }

    @CustomEventHandler
    public void onUserBreakBlock(UserBreakBlockEvent event) {
        if (config.getBoolean("simple_lobby.deny_block_break", true)) event.setCancelled(true);
    }

    @CustomEventHandler
    public void onUserPlaceBlock(UserPlaceBlockEvent event) {
        if (config.getBoolean("simple_lobby.deny_block_place", true)) event.setCancelled(true);
    }

    @CustomEventHandler(priority = CustomEventHandler.LOW)
    public void onUserJoin(UserJoinEvent event) {
        userJoinLobby(event.getUser());

        updateMotd(event.getGameGroup());

        if (event.getGameGroup().hasActiveCountdown()) return;

        resetCountdown(event.getGameGroup());
    }

    @CustomEventHandler(priority = CustomEventHandler.HIGH)
    public void onUserQuit(UserQuitEvent event) {
        updateMotd(event.getGameGroup());
    }

    @CustomEventHandler
    public void onCountdownMessage(CountdownMessageEvent event) {
        updateMotd(event.getGameGroup());
    }

    private void updateMotd(GameGroup gameGroup) {
        if(gameGroup.getUserCount() < minPlayersToStartGame) {
            gameGroup.setMotd(gameGroup.getLocale(waitingForPlayersLocale));
            return;
        }

        Countdown countdown = gameGroup.getCountdown();
        if(countdown == null) return;
        int seconds = countdown.getSecondsRemaining();

        if(seconds <= 5) gameGroup.setMotd(gameGroup.getLocale(starting5Locale));
        else if(seconds <= 10) gameGroup.setMotd(gameGroup.getLocale(starting10Locale));
        else if(seconds <= 30) gameGroup.setMotd(gameGroup.getLocale(starting30Locale));
        else if(seconds <= 60) gameGroup.setMotd(gameGroup.getLocale(starting60Locale));
        else if(seconds <= 120) gameGroup.setMotd(gameGroup.getLocale(starting120Locale));
        else gameGroup.setMotd(gameGroup.getLocale(startingEventuallyLocale));
    }

    private void userJoinLobby(User user) {
        if (!user.isPlayer()) {
            user.removeNonPlayer();
            return;
        }

        user.unDisguise();

        user.setInGame(false);
        if (!user.isPlayer()) return;

        user.setGameMode(GameMode.ADVENTURE);
        user.setSpectator(false);
        user.resetUserStats(true);
        user.setAllowFlight(false);

        user.setDisplayName(user.getName());
        user.setTabListName(user.getName());

        user.getInventory().clear();
        user.clearArmor();

        giveOnJoin.giveToUser(user);

        if(teleportToSpawn) {
            user.teleport(user.getGameGroup().getCurrentMap().getSpawn());
        }

        String message;

        for (int counter = 0; ; ++counter) {
            message = user.getGameGroup().getLocale(joinLobbyLocaleStub + "." + counter);
            if (message == null) break;

            user.sendMessage(message);

        }

        if(useMapScoreboard) {
            user.setScoreboardHandler(new MapScoreboardHandler(user));
            user.updateScoreboard();
        }
    }

    private void resetCountdown(GameGroup gameGroup) {
        if(startCountdown != null) {
            gameGroup.startCountdown(startCountdown);
        }
    }

    @CustomEventHandler
    public void onUserInventoryClick(UserInventoryClickEvent event) {
        if (config.getBoolean("simple_lobby.deny_inventory_move", true)) event.setCancelled(true);
    }

    @CustomEventHandler
    public void onGameStateChanged(GameStateChangedEvent event) {
        if (!Objects.equals(event.getNewGameState(), gameState)) return;

        if(!possibleLobbyMaps.isEmpty()) {
            Random random = new Random();
            double lobbyMapKey = random.nextDouble() * possibleLobbyMaps.lastKey();
            String mapName = possibleLobbyMaps.higherEntry(lobbyMapKey).getValue();

            event.getGameGroup().changeMap(mapName);
        }

        resetCountdown(event.getGameGroup());

        for (User user : event.getGameGroup().getUsers()) {
            userJoinLobby(user);
        }
    }

    @CustomEventHandler
    public void onCountdownFinished(CountdownFinishedEvent event) {
        if (startCountdown == null || !event.getCountdown().getName().equals(startCountdown.getName())) return;

        int userCount = event.getGameGroup().getUserCount();
        if (userCount < 1) return;
        if (userCount < minPlayersToStartGame) {
            event.getGameGroup().sendLocale(needsMorePlayersLocale);
            resetCountdown(event.getGameGroup());
            return;
        }

        event.getGameGroup().setMotd(event.getGameGroup().getLocale(startedLocale));
        event.getGameGroup().changeGameState(nextGameState);
    }

    @CustomEventHandler
    public void onUserPickupItem(UserPickupItemEvent event) {
        if (config.getBoolean("simple_lobby.deny_pickup_items", true)) event.setCancelled(true);
    }

    @CustomEventHandler
    public void onUserDamaged(UserDamagedEvent event) {
        if (config.getBoolean("simple_lobby.deny_damage", true)) event.setCancelled(true);
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        if (event.getInteractType() == UserInteractEvent.InteractType.REPRESENTING) return;
        if (event.hasItem() && event.getItem().getType() == Material.WRITTEN_BOOK) return;

        if (!event.hasBlock() || !isRedstoneControl(event.getClickedBlock().getType())) {
            event.setCancelled(true);
        }
    }

    private static boolean isRedstoneControl(Material type) {
        switch (type) {
            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case STONE_PLATE:
            case WOOD_PLATE:
            case GOLD_PLATE:
            case IRON_PLATE:
            case WOOD_DOOR:
            case TRAP_DOOR:
            case ACACIA_DOOR:
            case BIRCH_DOOR:
            case IRON_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case WOODEN_DOOR:
            case DARK_OAK_DOOR:
            case IRON_TRAPDOOR:
                return true;
            default:
                return false;
        }
    }

    @CustomEventHandler
    public void onUserFoodLevelChange(UserFoodLevelChangeEvent event) {
        if (config.getBoolean("simple_lobby.deny_hunger_loss", true)) event.setFoodLevel(20);
    }

    @CustomEventHandler
    public void onUserDropItem(UserDropItemEvent event) {
        if (!config.getBoolean("simple_lobby.deny_drop_items", true)) return;

        event.setCancelled(true);
    }

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        if (!config.getBoolean("simple_lobby.deny_kill_command", true)) return;
        switch (event.getCommand().getCommand().toLowerCase()) {
            case "kill":
            case "suicide":
                event.setHandled(true);
        }
    }

    @CustomEventHandler
    public void onCreatureSpawn(MapCreatureSpawnEvent event) {
        if (!config.getBoolean("simply_lobby.deny_creature_spawn", true)) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        event.setCancelled(true);
    }

    @CustomEventHandler
    public void onItemSpawn(MapItemSpawnEvent event) {
        if (!config.getBoolean("simple_lobby.deny_item_spawn", true)) return;
        event.setCancelled(true);
    }

    @CustomEventHandler(priority = CustomEventHandler.MONITOR)
    public void sendQuitMessageOnUserQuit(UserQuitEvent event) {
        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getGameGroup().getUserCount() - 1;
        int maxPlayers = event.getGameGroup().getMaxPlayers();

        event.getGameGroup().sendLocale(quitLocale, name, currentPlayers, maxPlayers);
    }

    @CustomEventHandler(priority = CustomEventHandler.FIRST)
    public void sendJoinMessageOnUserJoin(UserJoinEvent event) {
        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getGameGroup().getUserCount();
        int maxPlayers = event.getGameGroup().getMaxPlayers();

        event.getGameGroup().sendLocale(joinLocale, name, currentPlayers, maxPlayers);
    }
}
