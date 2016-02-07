package com.ithinkrok.minigames.base;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.base.command.Command;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.base.database.DatabaseTask;
import com.ithinkrok.minigames.base.database.DatabaseTaskRunner;
import com.ithinkrok.minigames.base.event.CommandEvent;
import com.ithinkrok.minigames.base.event.MinigamesEvent;
import com.ithinkrok.minigames.base.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.base.event.game.GameEvent;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.game.MapChangedEvent;
import com.ithinkrok.minigames.base.event.map.MapBlockBreakNaturallyEvent;
import com.ithinkrok.minigames.base.event.team.TeamEvent;
import com.ithinkrok.minigames.base.event.user.UserEvent;
import com.ithinkrok.minigames.base.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.base.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.base.event.user.world.UserBreakBlockEvent;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.minigames.base.item.IdentifierMap;
import com.ithinkrok.minigames.base.lang.LangFile;
import com.ithinkrok.minigames.base.lang.LanguageLookup;
import com.ithinkrok.minigames.base.lang.Messagable;
import com.ithinkrok.minigames.base.lang.MultipleLanguageLookup;
import com.ithinkrok.minigames.base.map.GameMap;
import com.ithinkrok.minigames.base.map.GameMapInfo;
import com.ithinkrok.minigames.base.metadata.Metadata;
import com.ithinkrok.minigames.base.metadata.MetadataHolder;
import com.ithinkrok.minigames.base.schematic.Schematic;
import com.ithinkrok.minigames.base.schematic.SchematicResolver;
import com.ithinkrok.minigames.base.task.GameRunnable;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.task.TaskList;
import com.ithinkrok.minigames.base.task.TaskScheduler;
import com.ithinkrok.minigames.base.team.Team;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.minigames.base.team.TeamUserResolver;
import com.ithinkrok.minigames.base.util.CountdownConfig;
import com.ithinkrok.minigames.base.util.io.ConfigHolder;
import com.ithinkrok.minigames.base.util.io.ConfigParser;
import com.ithinkrok.minigames.base.util.io.FileLoader;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.util.event.CustomEventExecutor;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by paul on 31/12/15.
 */
public class GameGroup implements LanguageLookup, Messagable, TaskScheduler, FileLoader, SharedObjectAccessor,
        MetadataHolder<Metadata>, SchematicResolver, TeamUserResolver, DatabaseTaskRunner, ConfigHolder {

    private final String name;

    private final ConcurrentMap<UUID, User> usersInGroup = new ConcurrentHashMap<>();

    private final Map<TeamIdentifier, Team> teamsInGroup = new HashMap<>();
    private final Game game;
    private final TaskList gameGroupTaskList = new TaskList();
    private final TaskList gameStateTaskList = new TaskList();
    private final CustomListener gameGroupListener;
    private final List<CustomListener> gameStateListeners = new ArrayList<>();
    private final ClassToInstanceMap<Metadata> metadataMap = MutableClassToInstanceMap.create();
    private final String chatPrefix;
    //Loaded from config
    private final HashMap<String, CustomListener> defaultListeners = new HashMap<>();
    private final IdentifierMap<CustomItem> customItemIdentifierMap = new IdentifierMap<>();
    private final Map<String, ConfigurationSection> sharedObjectMap = new HashMap<>();
    private final Map<String, Schematic> schematicMap = new HashMap<>();
    private final Map<String, TeamIdentifier> teamIdentifiers = new HashMap<>();
    private final Map<String, GameState> gameStates = new HashMap<>();
    private final Map<String, Kit> kits = new HashMap<>();
    private final Map<String, CommandConfig> commandMap = new TreeMap<>();
    private final Map<String, CommandConfig> commandAliasesMap = new HashMap<>();
    private final Map<String, GameMapInfo> gameMapInfoMap = new HashMap<>();
    private final MultipleLanguageLookup languageLookup = new MultipleLanguageLookup();
    private GameState gameState;
    private GameMap currentMap;
    private List<CustomListener> defaultAndMapListeners = new ArrayList<>();
    private Countdown countdown;

    public GameGroup(Game game, String name, String configFile) {
        this.game = game;

        this.name = name;

        gameGroupListener = new GameGroupListener();
        defaultAndMapListeners = createDefaultAndMapListeners();

        ConfigurationSection baseConfig = game.loadConfig(configFile);
        chatPrefix = baseConfig.getString("chat_prefix").replace('&', '§');

        ConfigParser.parseConfig(game, this, this, this, configFile, baseConfig);

        if (currentMap != null) defaultAndMapListeners = createDefaultAndMapListeners(currentMap.getListenerMap());
        else defaultAndMapListeners = createDefaultAndMapListeners();

        changeGameState(baseConfig.getString("start_game_state"));

        String startMap = baseConfig.getString("start_map");
        if (startMap != null) changeMap(startMap);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    private final List<CustomListener> createDefaultAndMapListeners(Map<String, CustomListener>... extra) {
        HashMap<String, CustomListener> clone = (HashMap<String, CustomListener>) defaultListeners.clone();

        for (Map<String, CustomListener> map : extra) {
            clone.putAll(map);
        }

        List<CustomListener> result = new ArrayList<>(clone.values());
        result.add(gameGroupListener);

        return result;
    }

    public void changeGameState(String gameStateName) {
        GameState gameState = gameStates.get(gameStateName);
        if (gameState == null) throw new IllegalArgumentException("Unknown game state name: " + gameStateName);

        changeGameState(gameState);
    }

    public void changeMap(String mapName) {
        GameMapInfo mapInfo = gameMapInfoMap.get(mapName);
        Validate.notNull(mapInfo, "The map " + mapName + " does not exist");

        changeMap(mapInfo);
    }

    @SuppressWarnings("unchecked")
    public void changeGameState(GameState gameState) {
        if (gameState.equals(this.gameState)) return;
        stopCountdown();

        GameState oldState = this.gameState;
        GameState newState = this.gameState = gameState;

        List<CustomListener> oldListeners = new ArrayList<>(gameStateListeners);
        gameStateListeners.clear();
        gameStateListeners.addAll(newState.createListeners(this));

        gameStateTaskList.cancelAllTasks();

        MinigamesEvent event = new GameStateChangedEvent(this, oldState, newState);
        CustomEventExecutor.executeEvent(event, getListeners(getAllUserListeners(), getAllTeamListeners(), oldListeners));
    }

    public void changeMap(GameMapInfo mapInfo) {
        GameMap oldMap = currentMap;
        GameMap newMap = new GameMap(this, mapInfo);

        usersInGroup.values().forEach(newMap::teleportUser);

        currentMap = newMap;

        game.setGameGroupForMap(this, newMap.getWorld().getName());

        MinigamesEvent event = new MapChangedEvent(this, oldMap, newMap);

        CustomEventExecutor
                .executeEvent(event, getListeners(getAllUserListeners(), getAllTeamListeners(), newMap.getListeners()));

        defaultAndMapListeners = createDefaultAndMapListeners(newMap.getListenerMap());

        if (oldMap != null) oldMap.unloadMap();
    }

    public void stopCountdown() {
        if (countdown == null) return;
        countdown.cancel();

        //Remove countdown level from Users
        for (User user : getUsers()) {
            user.setXpLevel(0);
        }
    }

    @SafeVarargs
    private final Collection<Collection<CustomListener>> getListeners(Collection<CustomListener>... extras) {
        Collection<Collection<CustomListener>> listeners = new ArrayList<>(4);
        if (gameState != null) listeners.add(gameStateListeners);
        listeners.add(defaultAndMapListeners);
        Collections.addAll(listeners, extras);

        return listeners;
    }

    private Collection<CustomListener> getAllUserListeners() {
        ArrayList<CustomListener> result = new ArrayList<>(usersInGroup.size());

        for (User user : usersInGroup.values()) {
            result.addAll(user.getListeners());
        }

        return result;
    }

    private Collection<CustomListener> getAllTeamListeners() {
        ArrayList<CustomListener> result = new ArrayList<>(teamsInGroup.size());

        for (Team team : teamsInGroup.values()) {
            result.addAll(team.getListeners());
        }

        return result;
    }

    public Collection<User> getUsers() {
        return usersInGroup.values();
    }

    public String getName() {
        return name;
    }

    public void prepareStart() {
        createDefaultAndMapListeners();
    }

    @Override
    public void doDatabaseTask(DatabaseTask databaseTask) {
        game.doDatabaseTask(databaseTask);
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    @Override
    public boolean hasSharedObject(String name) {
        return getSharedObject(name) != null;
    }

    @Override
    public ConfigurationSection getSharedObject(String name) {
        ConfigurationSection result = null;
        if (currentMap != null) result = currentMap.getSharedObject(name);
        return result != null ? result : sharedObjectMap.get(name);
    }

    @Override
    public ConfigurationSection getSharedObjectOrEmpty(String name) {
        ConfigurationSection result = null;

        if (currentMap != null) result = currentMap.getSharedObject(name);
        if (result != null) return result;

        result = sharedObjectMap.get(name);
        return result != null ? result : ConfigUtils.EMPTY_CONFIG;
    }

    @Override
    public Team getTeam(String name) {
        return getTeam(getTeamIdentifier(name));
    }

    @Override
    public Team getTeam(TeamIdentifier identifier) {
        return teamsInGroup.get(identifier);
    }

    @Override
    public TeamIdentifier getTeamIdentifier(String name) {
        return teamIdentifiers.get(name);
    }

    public Countdown getCountdown() {
        return countdown;
    }

    @Override
    public User getUser(UUID uuid) {
        return usersInGroup.get(uuid);
    }

    public void userEvent(UserEvent event) {
        if (event.getUser().getTeam() != null) {
            CustomEventExecutor.executeEvent(event,
                    getListeners(event.getUser().getListeners(), event.getUser().getTeam().getListeners()));
        } else {
            CustomEventExecutor.executeEvent(event, getListeners(event.getUser().getListeners()));
        }
    }

    public void teamEvent(TeamEvent event) {
        CustomEventExecutor.executeEvent(event, event.getTeam().getListeners(), getAllUsersInTeamListeners(event.getTeam()));
    }

    private Collection<CustomListener> getAllUsersInTeamListeners(Team team) {
        ArrayList<CustomListener> result = new ArrayList<>(team.getUsers().size());

        for (User user : team.getUsers()) {
            result.addAll(user.getListeners());
        }

        return result;
    }

    @Override
    public ConfigurationSection loadConfig(String name) {
        return game.loadConfig(name);
    }

    @Override
    public LangFile loadLangFile(String path) {
        return game.loadLangFile(path);
    }

    @Override
    public Path getAssetDirectory() {
        return game.getAssetDirectory();
    }

    public GameState getCurrentGameState() {
        return gameState;
    }

    public void startCountdown(CountdownConfig countdownConfig) {
        startCountdown(countdownConfig.getName(), countdownConfig.getLocaleStub(), countdownConfig.getSeconds());
    }

    public void startCountdown(String name, String localeStub, int seconds) {
        if (countdown != null) countdown.cancel();

        countdown = new Countdown(name, localeStub, seconds);
        countdown.start(this);
    }

    public CustomItem getCustomItem(String name) {
        CustomItem item = null;
        if (currentMap != null) item = currentMap.getCustomItem(name);
        return item != null ? item : customItemIdentifierMap.get(name);
    }

    public CustomItem getCustomItem(int identifier) {
        CustomItem item = null;
        if (currentMap != null) item = currentMap.getCustomItem(identifier);
        return item != null ? item : customItemIdentifierMap.get(identifier);
    }

    @Override
    public Schematic getSchematic(String name) {
        Schematic schem = null;
        if (currentMap != null) schem = currentMap.getSchematic(name);
        return schem != null ? schem : schematicMap.get(name);
    }

    public void gameEvent(GameEvent event) {
        CustomEventExecutor.executeEvent(event, getListeners(getAllUserListeners(), getAllTeamListeners()));
    }

    public void unload() {
        currentMap.unloadMap();
    }

    public void bindTaskToCurrentGameState(GameTask task) {
        gameStateTaskList.addTask(task);
    }

    public void bindTaskToCurrentMap(GameTask task) {
        if (currentMap == null) throw new RuntimeException("No GameMap to bind task to");
        currentMap.bindTaskToMap(task);
    }

    @Override
    public String getLocale(String name) {
        if (currentMap != null && currentMap.hasLocale(name)) return currentMap.getLocale(name);
        else return languageLookup.getLocale(name);
    }

    @Override
    public String getLocale(String name, Object... args) {
        if (currentMap != null && currentMap.hasLocale(name)) return currentMap.getLocale(name, args);
        else return languageLookup.getLocale(name, args);
    }

    @Override
    public boolean hasLocale(String name) {
        return (currentMap != null && currentMap.hasLocale(name) || languageLookup.hasLocale(name));
    }

    public Game getGame() {
        return game;
    }

    public boolean hasActiveCountdown() {
        return countdown != null;
    }

    public boolean hasActiveCountdown(String name) {
        return countdown != null && countdown.getName().equals(name);
    }

    public int getUserCount() {
        return getUsers().size();
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    @Override
    public <B extends Metadata> B getMetadata(Class<? extends B> clazz) {
        return metadataMap.getInstance(clazz);
    }

    @Override
    public <B extends Metadata> void setMetadata(B metadata) {
        Metadata oldMetadata = metadataMap.put(metadata.getMetadataClass(), metadata);

        if (oldMetadata != null && oldMetadata != metadata) {
            oldMetadata.cancelAllTasks();
            oldMetadata.removed();
        }
    }

    @Override
    public boolean hasMetadata(Class<? extends Metadata> clazz) {
        return metadataMap.containsKey(clazz);
    }

    public GameState getGameState(String gameStateName) {
        return gameStates.get(gameStateName);
    }

    public Collection<TeamIdentifier> getTeamIdentifiers() {
        return teamIdentifiers.values();
    }

    @Override
    public void addListener(String name, CustomListener listener) {
        defaultListeners.put(name, listener);

        createDefaultAndMapListeners();
    }

    @Override
    public void addCustomItem(CustomItem customItem) {
        customItemIdentifierMap.put(customItem.getName(), customItem);
    }

    @Override
    public void addLanguageLookup(LanguageLookup languageLookup) {
        this.languageLookup.addLanguageLookup(languageLookup);
    }

    @Override
    public void addSharedObject(String name, ConfigurationSection config) {
        sharedObjectMap.put(name, config);
    }    @Override
    public LanguageLookup getLanguageLookup() {
        return this;
    }

    @Override
    public void addSchematic(Schematic schematic) {
        schematicMap.put(schematic.getName(), schematic);
    }

    @Override
    public void addTeamIdentifier(TeamIdentifier teamIdentifier) {
        teamIdentifiers.put(teamIdentifier.getName(), teamIdentifier);

        teamsInGroup.put(teamIdentifier, createTeam(teamIdentifier));
    }

    private Team createTeam(TeamIdentifier teamIdentifier) {
        return new Team(teamIdentifier, this);
    }

    @Override
    public void addGameState(GameState gameState) {
        gameStates.put(gameState.getName(), gameState);
    }

    @Override
    public void addKit(Kit kit) {
        kits.put(kit.getName(), kit);
    }

    @Override
    public void addCommand(CommandConfig command) {
        commandMap.put(command.getName(), command);
        commandAliasesMap.put(command.getName(), command);

        for (String alias : command.getAliases()) {
            commandAliasesMap.put(alias.toLowerCase(), command);
        }
    }

    @Override
    public void addMapInfo(GameMapInfo mapInfo) {
        gameMapInfoMap.put(mapInfo.getName(), mapInfo);
    }

    public void kill() {
        List<User> users = new ArrayList<>(getUsers());

        List<Player> players = new ArrayList<>();

        for (User user : users) {
            user.removeFromGameGroup();

            game.removeUser(user);

            if (user.isPlayer()) players.add(user.getPlayer());
        }

        for (Team team : teamsInGroup.values()) {
            team.removeFromGameGroup();
        }

        for (Metadata metadata : metadataMap.values()) {
            metadata.removed();
        }

        metadataMap.clear();

        cancelAllTasks();

        game.removeGameGroup(this);

        for (Player player : players) {
            game.rejoinPlayer(player);
        }

        doInFuture(task -> {
            currentMap.unloadMap();
            currentMap = null;
        });

    }

    @Override
    public GameTask doInFuture(GameRunnable task) {
        GameTask gameTask = game.doInFuture(task);

        gameGroupTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask doInFuture(GameRunnable task, int delay) {
        GameTask gameTask = game.doInFuture(task, delay);

        gameGroupTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask repeatInFuture(GameRunnable task, int delay, int period) {
        GameTask gameTask = game.repeatInFuture(task, delay, period);

        gameGroupTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public void cancelAllTasks() {
        gameGroupTaskList.cancelAllTasks();
    }

    public CommandConfig getCommand(String name) {
        return name != null ? commandAliasesMap.get(name.toLowerCase()) : null;
    }

    public Map<String, CommandConfig> getCommands() {
        return commandMap;
    }

    private class GameGroupListener implements CustomListener {

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventUserJoin(UserJoinEvent event) {
            if (event.getReason() != UserJoinEvent.JoinReason.JOINED_SERVER) return;

            usersInGroup.put(event.getUser().getUuid(), event.getUser());

            currentMap.teleportUser(event.getUser());
        }

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_LAST)
        public void eventUserQuit(UserQuitEvent event) {
            if (event.getRemoveUser()) {
                event.getUser().setTeam(null);
                usersInGroup.remove(event.getUser().getUuid());

                event.getUser().cancelAllTasks();
                game.removeUser(event.getUser());

                //GameGroup only referenced by its users. If there are none left we must unload.
                if (usersInGroup.isEmpty()) kill();
            }
        }

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_LAST)
        public void eventCountdownFinished(CountdownFinishedEvent event) {
            if (event.getCountdown().getSecondsRemaining() > 0) return;
            if (event.getCountdown() != countdown) return;

            countdown = null;
        }

        @CustomEventHandler
        public void eventBlockBreakNaturally(MapBlockBreakNaturallyEvent event) {
            checkInventoryTethers(event.getBlock().getLocation());
        }

        private void checkInventoryTethers(Location location) {
            for (User user : getUsers()) {
                if (!location.equals(user.getInventoryTether())) continue;
                user.closeInventory();
            }
        }

        @CustomEventHandler
        public void eventUserBreakBlock(UserBreakBlockEvent event) {
            checkInventoryTethers(event.getBlock().getLocation());
        }

        @CustomEventHandler
        public void eventCommand(CommandEvent event) {
            CommandConfig commandConfig = commandAliasesMap.get(event.getCommand().getCommand().toLowerCase());

            if (commandConfig == null) return;
            event.setHandled(true);

            if (!Command.requirePermission(event.getCommandSender(), commandConfig.getPermission())) return;
            if (commandConfig.hasOthersPermission() && !event.getCommand()
                    .requireOthersPermission(event.getCommandSender(), commandConfig.getOthersPermission())) return;

            CustomEventExecutor.executeEvent(event, commandConfig.getExecutor());

            if (event.isValidCommand()) return;

            event.getCommandSender().sendMessage(commandConfig.getUsage());
        }

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventGameStateChange(GameStateChangedEvent event) {
            Iterator<Metadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                Metadata metadata = iterator.next();

                if (metadata.removeOnGameStateChange(event)) {
                    metadata.cancelAllTasks();
                    metadata.removed();
                    iterator.remove();
                }
            }
        }

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventMapChange(MapChangedEvent event) {
            Iterator<Metadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                Metadata metadata = iterator.next();

                if (metadata.removeOnMapChange(event)) {
                    metadata.cancelAllTasks();
                    metadata.removed();
                    iterator.remove();
                }
            }
        }
    }




    @Override
    public void sendLocale(String locale, Object... args) {
        sendMessage(getLocale(locale, args));
    }


    @Override
    public void sendMessage(String message) {
        sendMessageNoPrefix(getChatPrefix() + message);
    }


    @Override
    public void sendMessageNoPrefix(String message) {
        for (User user : usersInGroup.values()) {
            user.sendMessageNoPrefix(message);
        }

        Bukkit.getConsoleSender().sendMessage(message);
    }


    public String getChatPrefix() {
        return chatPrefix;
    }


    @Override
    public void sendLocaleNoPrefix(String locale, Object... args) {
        sendMessageNoPrefix(getLocale(locale, args));
    }


}
