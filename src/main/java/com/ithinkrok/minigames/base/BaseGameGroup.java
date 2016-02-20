package com.ithinkrok.minigames.base;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.api.*;
import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.database.DatabaseTask;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.event.MinigamesEvent;
import com.ithinkrok.minigames.api.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.api.event.game.GameEvent;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.map.MapBlockBreakNaturallyEvent;
import com.ithinkrok.minigames.api.event.team.TeamEvent;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.api.event.user.world.UserBreakBlockEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.item.IdentifierMap;
import com.ithinkrok.minigames.api.map.GameMapInfo;
import com.ithinkrok.minigames.api.metadata.Metadata;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.api.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.api.schematic.Schematic;
import com.ithinkrok.minigames.api.task.GameRunnable;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskList;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.CountdownConfig;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.minigames.base.command.CommandConfig;
import com.ithinkrok.minigames.base.map.BaseMap;
import com.ithinkrok.minigames.base.util.io.ConfigHolder;
import com.ithinkrok.minigames.base.util.io.ConfigParser;
import com.ithinkrok.minigames.base.util.io.FileLoader;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import com.ithinkrok.util.event.CustomEventExecutor;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LangFile;
import com.ithinkrok.util.lang.LanguageLookup;
import com.ithinkrok.util.lang.MultipleLanguageLookup;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by paul on 31/12/15.
 */
public class BaseGameGroup implements GameGroup, ConfigHolder, FileLoader {

    private final String name;
    private final String type;

    private final ConcurrentMap<UUID, BaseUser> usersInGroup = new ConcurrentHashMap<>();

    private final Map<TeamIdentifier, BaseTeam> teamsInGroup = new HashMap<>();
    private final BaseGame game;
    private final TaskList gameGroupTaskList = new TaskList();
    private final TaskList gameStateTaskList = new TaskList();
    private final CustomListener gameGroupListener;
    private final List<CustomListener> gameStateListeners = new ArrayList<>();
    private final ClassToInstanceMap<Metadata> metadataMap = MutableClassToInstanceMap.create();
    private final String chatPrefix;
    //Loaded from config
    private final HashMap<String, CustomListener> defaultListeners = new HashMap<>();
    private final IdentifierMap<CustomItem> customItemIdentifierMap = new IdentifierMap<>();
    private final Map<String, Config> sharedObjectMap = new HashMap<>();
    private final Map<String, Schematic> schematicMap = new HashMap<>();
    private final Map<String, JSONBook> bookMap = new HashMap<>();
    private final Map<String, TeamIdentifier> teamIdentifiers = new HashMap<>();
    private final Map<String, GameState> gameStates = new HashMap<>();
    private final Map<String, Kit> kits = new HashMap<>();
    private final Map<String, CommandConfig> commandMap = new TreeMap<>();
    private final Map<String, CommandConfig> commandAliasesMap = new HashMap<>();
    private final Map<String, GameMapInfo> gameMapInfoMap = new HashMap<>();
    private final MultipleLanguageLookup languageLookup = new MultipleLanguageLookup();
    private GameState gameState;
    private BaseMap currentMap;
    private List<CustomListener> defaultAndMapListeners = new ArrayList<>();
    private Countdown countdown;

    private boolean acceptingPlayers = true;
    private final int maxPlayers;
    private String motd = "default motd";

    public BaseGameGroup(BaseGame game, String name, String type, String configFile) {
        this.game = game;
        this.name = name;
        this.type = type;

        gameGroupListener = new GameGroupListener();
        defaultAndMapListeners = createDefaultAndMapListeners();

        Config baseConfig = game.loadConfig(configFile);
        chatPrefix = baseConfig.getString("chat_prefix").replace('&', '§');

        ConfigParser.parseConfig(game, this, this, this, configFile, baseConfig);

        if (currentMap != null) defaultAndMapListeners = createDefaultAndMapListeners(currentMap.getListenerMap());
        else defaultAndMapListeners = createDefaultAndMapListeners();

        maxPlayers = baseConfig.getInt("max_players", 40);
        motd = baseConfig.getString("default_motd", "No default motd");

        changeGameState(baseConfig.getString("start_game_state"));

        String startMap = baseConfig.getString("start_map");
        if (startMap != null) changeMap(startMap);
    }

    @Override public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override public String getMotd() {
        return motd;
    }

    @Override public void setMotd(String motd) {
        if(motd.equals(this.motd)) return;

        this.motd = motd;

        game.getProtocol().sendGameGroupUpdatePayload(this);
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

    @Override public String getType() {
        return type;
    }

    /**
     * Sets acceptingPlayers. This boolean is used to determine if players can join this gamegroup to play the game.
     * It should be set to false if players cannot join this gamegroup to play the game, even if spectators are allowed.
     *
     * @param acceptingPlayers The accepting players boolean
     */
    @Override public void setAcceptingPlayers(boolean acceptingPlayers) {
        this.acceptingPlayers = acceptingPlayers;

        game.getProtocol().sendGameGroupUpdatePayload(this);
    }

    @Override public boolean isAcceptingPlayers() {
        return acceptingPlayers;
    }

    @Override public void changeGameState(String gameStateName) {
        GameState gameState = gameStates.get(gameStateName);
        if (gameState == null) throw new IllegalArgumentException("Unknown game state name: " + gameStateName);

        changeGameState(gameState);
    }

    @Override public void changeMap(String mapName) {
        GameMapInfo mapInfo = gameMapInfoMap.get(mapName);
        Validate.notNull(mapInfo, "The map " + mapName + " does not exist");

        changeMap(mapInfo);
    }

    @Override
    public ClientMinigamesRequestProtocol getRequestProtocol() {
        return BasePlugin.getRequestProtocol();
    }

    @Override
    public ControllerInfo getControllerInfo() {
        return getRequestProtocol().getControllerInfo();
    }

    @Override
    public void requestControllerInfo() {
        getRequestProtocol().enableControllerInfo();
    }

    @Override public GameMapInfo getMap(String mapName) {
        return gameMapInfoMap.get(mapName);
    }

    @Override@SuppressWarnings("unchecked")
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
        CustomEventExecutor
                .executeEvent(event, getListeners(getAllUserListeners(), getAllTeamListeners(), oldListeners));
    }

    @Override public void changeMap(GameMapInfo mapInfo) {
        BaseMap oldMap = currentMap;
        BaseMap newMap = new BaseMap(this, mapInfo);

        usersInGroup.values().forEach(newMap::teleportUser);

        currentMap = newMap;

        game.setGameGroupForMap(this, newMap.getWorld().getName());

        MinigamesEvent event = new MapChangedEvent(this, oldMap, newMap);

        CustomEventExecutor
                .executeEvent(event, getListeners(getAllUserListeners(), getAllTeamListeners(), newMap.getListeners()));

        defaultAndMapListeners = createDefaultAndMapListeners(newMap.getListenerMap());

        if (oldMap != null) oldMap.unloadMap();
    }

    @Override public void stopCountdown() {
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

        for (BaseUser user : usersInGroup.values()) {
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

    @Override public Collection<BaseUser> getUsers() {
        return usersInGroup.values();
    }

    @Override public Config toConfig() {
        Config config = new MemoryConfig();

        config.set("name", name);
        config.set("type", type);
        config.set("accepting", acceptingPlayers);

        List<String> users = new ArrayList<>();

        for(User user : getUsers()) {
            if(!user.isPlayer()) continue;
            users.add(user.getUuid().toString());
        }

        config.set("players", users);

        config.set("player_count", users.size());
        config.set("max_players", maxPlayers);
        config.set("motd", motd);

        config.set("server", game.getName());

        return config;
    }

    @Override public String getName() {
        return name;
    }

    @Override public void prepareStart() {
        createDefaultAndMapListeners();
    }

    @Override
    public void doDatabaseTask(DatabaseTask databaseTask) {
        game.doDatabaseTask(databaseTask);
    }

    @Override public BaseMap getCurrentMap() {
        return currentMap;
    }

    @Override
    public boolean hasSharedObject(String name) {
        return getSharedObject(name) != null;
    }

    @Override
    public Config getSharedObject(String name) {
        Config result = null;
        if (currentMap != null) result = currentMap.getSharedObject(name);
        return result != null ? result : sharedObjectMap.get(name);
    }

    @Override
    public Config getSharedObjectOrEmpty(String name) {
        Config result = null;

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

    @Override public Countdown getCountdown() {
        return countdown;
    }

    @Override
    public User getUser(UUID uuid) {
        return usersInGroup.get(uuid);
    }

    @Override public void userEvent(UserEvent event) {
        if (event.getUser().getTeam() != null) {
            CustomEventExecutor.executeEvent(event,
                    getListeners(event.getUser().getListeners(), event.getUser().getTeam().getListeners()));
        } else {
            CustomEventExecutor.executeEvent(event, getListeners(event.getUser().getListeners()));
        }
    }

    @Override public void teamEvent(TeamEvent event) {
        CustomEventExecutor
                .executeEvent(event, event.getTeam().getListeners(), getAllUsersInTeamListeners(event.getTeam()));
    }

    private Collection<CustomListener> getAllUsersInTeamListeners(Team team) {
        ArrayList<CustomListener> result = new ArrayList<>(team.getUsers().size());

        for (User user : team.getUsers()) {
            result.addAll(user.getListeners());
        }

        return result;
    }

    @Override
    public Config loadConfig(String name) {
        return game.loadConfig(name);
    }

    @Override
    public JSONBook loadBook(String name, String path) {
        return game.loadBook(name, path);
    }

    @Override
    public LangFile loadLangFile(String path) {
        return game.loadLangFile(path);
    }

    @Override
    public Path getAssetDirectory() {
        return game.getAssetDirectory();
    }

    @Override public GameState getCurrentGameState() {
        return gameState;
    }

    @Override public void startCountdown(CountdownConfig countdownConfig) {
        startCountdown(countdownConfig.getName(), countdownConfig.getLocaleStub(), countdownConfig.getSeconds());
    }

    @Override public void startCountdown(String name, String localeStub, int seconds) {
        if (countdown != null) countdown.cancel();

        countdown = new Countdown(name, localeStub, seconds);
        countdown.start(this);
    }

    @Override public CustomItem getCustomItem(String name) {
        CustomItem item = null;
        if (currentMap != null) item = currentMap.getCustomItem(name);
        return item != null ? item : customItemIdentifierMap.get(name);
    }

    @Override public CustomItem getCustomItem(int identifier) {
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

    @Override public void gameEvent(GameEvent event) {
        CustomEventExecutor.executeEvent(event, getListeners(getAllUserListeners(), getAllTeamListeners()));
    }

    @Override public void unload() {
        currentMap.unloadMap();
    }

    @Override public void bindTaskToCurrentGameState(GameTask task) {
        gameStateTaskList.addTask(task);
    }

    @Override public void bindTaskToCurrentMap(GameTask task) {
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

    @Override public Game getGame() {
        return game;
    }

    @Override public boolean hasActiveCountdown() {
        return countdown != null;
    }

    @Override public boolean hasActiveCountdown(String name) {
        return countdown != null && countdown.getName().equals(name);
    }

    @Override public int getUserCount() {
        return getUsers().size();
    }

    @Override public Kit getKit(String name) {
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

    @Override public GameState getGameState(String gameStateName) {
        return gameStates.get(gameStateName);
    }

    @Override public Collection<TeamIdentifier> getTeamIdentifiers() {
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
    public void addSharedObject(String name, Config config) {
        sharedObjectMap.put(name, config);
    }

    @Override
    public void addSchematic(Schematic schematic) {
        schematicMap.put(schematic.getName(), schematic);
    }    @Override
    public LanguageLookup getLanguageLookup() {
        return this;
    }

    @Override
    public void addTeamIdentifier(TeamIdentifier teamIdentifier) {
        teamIdentifiers.put(teamIdentifier.getName(), teamIdentifier);

        teamsInGroup.put(teamIdentifier, createTeam(teamIdentifier));
    }

    private BaseTeam createTeam(TeamIdentifier teamIdentifier) {
        return new BaseTeam(teamIdentifier, this);
    }

    @Override
    public void addGameState(GameState gameState) {
        gameStates.put(gameState.getName(), gameState);
    }

    @Override
    public void addKit(Kit kit) {
        kits.put(kit.getName(), kit);
    }

    @Override public JSONBook getBook(String name) {
        if(currentMap == null) return bookMap.get(name);
        JSONBook book = currentMap.getBook(name);

        return book != null ? book : bookMap.get(name);
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

    @Override
    public void addBook(JSONBook book) {
        bookMap.put(book.getName(), book);
    }

    @Override public void kill() {
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
            game.sendPlayerToHub(player);
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

    @Override public CommandConfig getCommand(String name) {
        return name != null ? commandAliasesMap.get(name.toLowerCase()) : null;
    }

    @Override public Map<String, CommandConfig> getCommands() {
        return commandMap;
    }

    private class GameGroupListener implements CustomListener {

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventUserJoin(UserJoinEvent event) {
            if (event.getReason() != UserJoinEvent.JoinReason.JOINED_SERVER) return;
            if(!(event.getUser() instanceof BaseUser)) {
                throw new UnsupportedOperationException("Only supports BaseUser");
            }

            usersInGroup.put(event.getUser().getUuid(), (BaseUser) event.getUser());

            currentMap.teleportUser(event.getUser());

            game.getProtocol().sendGameGroupUpdatePayload(BaseGameGroup.this);
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
                else game.getProtocol().sendGameGroupUpdatePayload(BaseGameGroup.this);
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
        public void eventCommand(MinigamesCommandEvent event) {
            CommandConfig commandConfig = commandAliasesMap.get(event.getCommand().getCommand().toLowerCase());

            if (commandConfig == null) return;
            event.setHandled(true);

            if (!MinigamesCommand.requirePermission(event.getCommandSender(), commandConfig.getPermission())) return;
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


    @Override public String getChatPrefix() {
        return chatPrefix;
    }


    @Override
    public void sendLocaleNoPrefix(String locale, Object... args) {
        sendMessageNoPrefix(getLocale(locale, args));
    }


}
