package com.ithinkrok.minigames.base.gamestate;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.GameState;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.CommandEvent;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.map.MapCreatureSpawnEvent;
import com.ithinkrok.minigames.base.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.base.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.base.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.base.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.base.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.base.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.base.event.user.world.*;
import com.ithinkrok.minigames.base.scoreboard.MapScoreboardHandler;
import com.ithinkrok.minigames.base.util.MinigamesConfigs;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.minigames.base.util.CountdownConfig;
import com.ithinkrok.minigames.base.util.CustomItemGiver;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Objects;

/**
 * Created by paul on 30/01/16.
 */
public class SimpleLobbyListener implements Listener {

    protected String quitLocale;
    protected String joinLocale;

    protected GameState gameState;

    private CountdownConfig startCountdown;

    private String needsMorePlayersLocale;
    private int minPlayersToStartGame;


    private String lobbyMapName;
    private String nextGameState;

    private String joinLobbyLocaleStub;

    private CustomItemGiver giveOnJoin;
    private ConfigurationSection config;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        gameState = event.getRepresenting();

        config = event.getConfig();

        nextGameState = config.getString("next_gamestate");

        configureCountdown(config.getConfigurationSection("start_countdown"));

        lobbyMapName = config.getString("lobby_map");

        giveOnJoin = new CustomItemGiver(config.getConfigurationSection("give_on_join"));

        joinLobbyLocaleStub = config.getString("join_lobby_locale_stub", "lobby.info");

        quitLocale = config.getString("user_quit_lobby_locale", "user.quit.lobby");
        joinLocale = config.getString("user_join_lobby_locale", "user.join.lobby");
    }


    private void configureCountdown(ConfigurationSection config) {
        startCountdown = MinigamesConfigs.getCountdown(config, "");
        minPlayersToStartGame = config.getInt("min_players");
        needsMorePlayersLocale = config.getString("needs_more_players_locale");
    }

    @MinigamesEventHandler
    public void onUserBreakBlock(UserBreakBlockEvent event) {
        if (config.getBoolean("simple_lobby.deny_block_break", true)) event.setCancelled(true);
    }

    @MinigamesEventHandler
    public void onUserPlaceBlock(UserPlaceBlockEvent event) {
        if (config.getBoolean("simple_lobby.deny_block_place", true)) event.setCancelled(true);
    }

    @MinigamesEventHandler(priority = MinigamesEventHandler.LOW)
    public void eventUserJoin(UserJoinEvent event) {
        userJoinLobby(event.getUser());

        if (event.getUserGameGroup().hasActiveCountdown()) return;

        resetCountdown(event.getUserGameGroup());
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

        user.setDisplayName(user.getName());
        user.setTabListName(user.getName());

        user.getInventory().clear();
        user.clearArmor();

        giveOnJoin.giveToUser(user);

        user.teleport(user.getGameGroup().getCurrentMap().getSpawn());

        String message;

        for (int counter = 0; ; ++counter) {
            message = user.getGameGroup().getLocale(joinLobbyLocaleStub + "." + counter);
            if (message == null) break;

            user.sendMessage(message);

        }

        user.setScoreboardHandler(new MapScoreboardHandler(user));
        user.updateScoreboard();
    }

    private void resetCountdown(GameGroup gameGroup) {
        gameGroup.startCountdown(startCountdown);
    }

    @MinigamesEventHandler
    public void onUserInventoryClick(UserInventoryClickEvent event) {
        if (config.getBoolean("simple_lobby.deny_inventory_move", true)) event.setCancelled(true);
    }

    @MinigamesEventHandler
    public void onGameStateChanged(GameStateChangedEvent event) {
        if (!Objects.equals(event.getNewGameState(), gameState)) return;

        event.getGameGroup().changeMap(lobbyMapName);

        resetCountdown(event.getGameGroup());

        for (User user : event.getGameGroup().getUsers()) {
            userJoinLobby(user);
        }
    }

    @MinigamesEventHandler
    public void onCountdownFinished(CountdownFinishedEvent event) {
        if (!event.getCountdown().getName().equals(startCountdown.getName())) return;

        int userCount = event.getGameGroup().getUserCount();
        if (userCount < 1) return;
        if (userCount < minPlayersToStartGame) {
            event.getGameGroup().sendLocale(needsMorePlayersLocale);
            resetCountdown(event.getGameGroup());
            return;
        }

        event.getGameGroup().changeGameState(nextGameState);
    }

    @MinigamesEventHandler
    public void onUserPickupItem(UserPickupItemEvent event) {
        if (config.getBoolean("simple_lobby.deny_pickup_items", true)) event.setCancelled(true);
    }

    @MinigamesEventHandler
    public void onUserDamaged(UserDamagedEvent event) {
        if (config.getBoolean("simple_lobby.deny_damage", true)) event.setCancelled(true);
    }

    @MinigamesEventHandler
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
                return true;
            default:
                return false;
        }
    }

    @MinigamesEventHandler
    public void onUserFoodLevelChange(UserFoodLevelChangeEvent event) {
        if (config.getBoolean("simple_lobby.deny_hunger_loss", true)) event.setFoodLevel(20);
    }

    @MinigamesEventHandler
    public void onUserDropItem(UserDropItemEvent event) {
        if (!config.getBoolean("simple_lobby.deny_drop_items", true)) return;

        event.setCancelled(true);
    }

    @MinigamesEventHandler
    public void onCommand(CommandEvent event) {
        if (!config.getBoolean("simple_lobby.deny_kill_command", true)) return;
        switch (event.getCommand().getCommand().toLowerCase()) {
            case "kill":
            case "suicide":
                event.setHandled(true);
        }
    }

    @MinigamesEventHandler
    public void onCreatureSpawn(MapCreatureSpawnEvent event) {
        if (!config.getBoolean("simply_lobby.deny_creature_spawn", true)) return;
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        event.setCancelled(true);
    }

    @MinigamesEventHandler
    public void onItemSpawn(MapItemSpawnEvent event) {
        if (!config.getBoolean("simple_lobby.deny_item_spawn", true)) return;
        event.setCancelled(true);
    }

    @MinigamesEventHandler(priority = MinigamesEventHandler.MONITOR)
    public void sendQuitMessageOnUserQuit(UserQuitEvent event) {
        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getUserGameGroup().getUserCount() - 1;
        int maxPlayers = Bukkit.getMaxPlayers();

        event.getUserGameGroup().sendLocale(quitLocale, name, currentPlayers, maxPlayers);
    }

    @MinigamesEventHandler(priority = MinigamesEventHandler.FIRST)
    public void sendJoinMessageOnUserJoin(UserJoinEvent event) {
        String name = event.getUser().getFormattedName();
        int currentPlayers = event.getUserGameGroup().getUserCount();
        int maxPlayers = Bukkit.getMaxPlayers();

        event.getUserGameGroup().sendLocale(joinLocale, name, currentPlayers, maxPlayers);
    }
}
