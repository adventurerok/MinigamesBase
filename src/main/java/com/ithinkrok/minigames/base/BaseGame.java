package com.ithinkrok.minigames.base;

import com.comphenix.packetwrapper.WrapperPlayClientTabComplete;
import com.comphenix.packetwrapper.WrapperPlayServerTabComplete;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.MapMaker;
import com.ithinkrok.minigames.api.Game;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.database.DatabaseTask;
import com.ithinkrok.minigames.api.database.Persistence;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesProtocol;
import com.ithinkrok.minigames.api.task.GameRunnable;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.JSONBook;
import com.ithinkrok.minigames.api.util.disguise.Disguise;
import com.ithinkrok.minigames.api.util.disguise.DisguiseController;
import com.ithinkrok.minigames.base.bukkitlistener.GameBukkitListener;
import com.ithinkrok.minigames.base.util.InvisiblePlayerAttacker;
import com.ithinkrok.minigames.base.util.disguise.LDDisguiseController;
import com.ithinkrok.minigames.base.util.disguise.MinigamesDisguiseController;
import com.ithinkrok.minigames.base.util.io.FileLoader;
import com.ithinkrok.msm.bukkit.MSMPlugin;
import com.ithinkrok.msm.bukkit.protocol.ClientAPIProtocol;
import com.ithinkrok.msm.bukkit.util.BukkitConfig;
import com.ithinkrok.msm.client.Client;
import com.ithinkrok.msm.client.ClientListener;
import com.ithinkrok.msm.client.impl.MSMClient;
import com.ithinkrok.msm.client.protocol.ClientUpdateFileProtocol;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.lang.LangFile;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by paul on 31/12/15.
 * <p>
 * In future: Will be a TaskScheduler, UserResolver, FileLoader and DatabaseTaskRunner only
 */
@SuppressWarnings("unchecked")
public class BaseGame implements Game, FileLoader {

    private final String name;

    private final String hubServer;

    private final Plugin plugin;
    private final Persistence persistence;

    /**
     * Maps game group type names to their config locations
     * <p>
     * e.g. colony_wars -> colony_wars/colony_wars.yml
     */
    private final Map<String, String> gameGroupConfigMap = new HashMap<>();
    private final String fallbackConfig;

    private final Map<String, BaseGameGroup> mapToGameGroup = new MapMaker().weakValues().makeMap();
    private final Map<String, BaseGameGroup> nameToGameGroup = new HashMap<>();

    /**
     * Maps player UUID to game group joining data
     */
    private final Map<UUID, JoiningGameGroupData> playersJoiningGameGroupTypes = new ConcurrentHashMap<>();

    /**
     * Maps player UUID to game group name
     */
    private final Map<UUID, String> playersJoinGameGroups = new ConcurrentHashMap<>();

    private final Path configDirectory;
    private final Path mapDirectory;
    private final Path assetsDirectory;
    private final Path ramdiskDirectory;
    private final Path resourceDirectory;

    private final ClientMinigamesProtocol protocol;

    private DisguiseController disguiseController;

    public BaseGame(BasePlugin plugin, Config config) {
        this.plugin = plugin;

        name = config.getString("bungee.name");

        hubServer = config.getString("bungee.hub");

        fallbackConfig = config.getString("fallback_gamegroup");

        if (config.contains("directories.resource")) {
            resourceDirectory = Paths.get(config.getString("directories.resource"));

            configDirectory = resourceDirectory.resolve("config");
            mapDirectory = resourceDirectory.resolve("maps");
            assetsDirectory = resourceDirectory.resolve("assets");
        } else {
            configDirectory = Paths.get(config.getString("directories.config"));
            mapDirectory = Paths.get(config.getString("directories.maps"));
            assetsDirectory = Paths.get(config.getString("directories.assets"));

            resourceDirectory = configDirectory.getParent();
        }

        ramdiskDirectory =
                config.getBoolean("ramdisk.enable") ? Paths.get(config.getString("ramdisk.directory")) : null;

        if (ramdiskDirectory != null && !Files.exists(ramdiskDirectory)) {
            try {
                Files.createDirectory(ramdiskDirectory);
            } catch (IOException e) {
                System.out.println("Failed to create ramdisk directory");
                e.printStackTrace();
            }
        }

        persistence = new Persistence(plugin);

        InvisiblePlayerAttacker.enablePlayerAttacker(this, plugin, ProtocolLibrary.getProtocolManager());

        ProtocolLibrary.getProtocolManager()
                .addPacketListener(new TabCompleteListener(plugin, ListenerPriority.NORMAL));

        unloadDefaultWorlds();

        setupDisguiseController();

        //Is this minecraft server the primary server on this server machine
        boolean primary = config.getBoolean("primary", false);
        protocol = new ClientMinigamesProtocol(this, primary);

        MSMClient.addProtocol("Minigames", protocol);

        ClientListener updateProtocol = new ClientUpdateFileProtocol(primary, resourceDirectory);
        MSMClient.addProtocol("MinigamesUpdate", updateProtocol);
    }

    /**
     * Unloads all chunks in all worlds
     */
    private void unloadDefaultWorlds() {
        if (Bukkit.getWorlds().size() != 1) System.out.println("You should disable the nether/end worlds to save RAM!");

        for (World world : Bukkit.getWorlds()) {
            world.setKeepSpawnInMemory(false);

            for (Chunk chunk : world.getLoadedChunks()) {
                chunk.unload(false, true);
            }
        }
    }

    private void setupDisguiseController() {
        if (Bukkit.getPluginManager().getPlugin("LibsDisguises") != null) {
            disguiseController = new LDDisguiseController();
        } else {
            disguiseController = new MinigamesDisguiseController();
        }
    }

    @Override
    public ClientMinigamesProtocol getProtocol() {
        return protocol;
    }

    @Override
    public Collection<String> getAvailableGameGroupTypes() {
        return gameGroupConfigMap.keySet();
    }

    @Override
    public Collection<BaseGameGroup> getGameGroups() {
        return nameToGameGroup.values();
    }

    @Override
    public Path getAssetDirectory() {
        return assetsDirectory;
    }

    @Override
    public Path getRamdiskDirectory() {
        return ramdiskDirectory;
    }

    @Override
    public Path getConfigDirectory() {
        return configDirectory;
    }

    @Override
    public Path getMapDirectory() {
        return mapDirectory;
    }

    @Override
    public Path getResourceDirectory() {
        return resourceDirectory;
    }

    @Override
    public void registerListeners() {
        Listener listener = new GameBukkitListener(this);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    @Override
    public void registerGameGroupConfig(String name, String configFile) {
        gameGroupConfigMap.put(name, configFile);
    }

    @Override
    public BaseGameGroup getGameGroup(String ggName) {
        return nameToGameGroup.get(ggName);
    }

    @Override
    public void unload() {
        nameToGameGroup.values().forEach(GameGroup::unload);

        persistence.onPluginDisabled();
    }

    @Override
    public void removeGameGroup(GameGroup gameGroup) {
        Validate.notNull(gameGroup, "gameGroup cannot be null");
        if (!(gameGroup instanceof BaseGameGroup)) {
            throw new UnsupportedOperationException("Only supports BaseGameGroup");
        }

        nameToGameGroup.values().remove(gameGroup);

        protocol.sendGameGroupKilledPayload(gameGroup);
    }

    @Override
    public void makeEntityRepresentUser(User user, Entity entity) {
        entity.setMetadata("rep", new FixedMetadataValue(plugin, user.getUuid()));
    }

    @Override
    public void makeEntityActualUser(User user, Entity entity) {
        entity.setMetadata("actual", new FixedMetadataValue(plugin, user.getUuid()));
    }

    @Override
    public void setCustomEntityName(Entity entity, String name) {
        entity.setMetadata("custom_name", new FixedMetadataValue(plugin, name));
    }

    @Override
    public void setGameGroupForMap(GameGroup gameGroup, String mapName) {
        Validate.notNull(gameGroup, "gameGroup cannot be null");

        if (!(gameGroup instanceof BaseGameGroup)) {
            throw new UnsupportedOperationException("Only supports BaseGameGroup");
        }

        mapToGameGroup.put(mapName, (BaseGameGroup) gameGroup);
    }

    @Override
    public BaseGameGroup getGameGroupFromMapName(String mapName) {
        return mapToGameGroup.get(mapName);
    }

    @Override
    public void makeEntityRepresentTeam(Team team, Entity entity) {
        entity.setMetadata("team", new FixedMetadataValue(plugin, team.getName()));
    }

    @Override
    public void disguiseUser(User user, Disguise disguise) {
        disguiseController.disguise(user, disguise);
    }

    @Override
    public void unDisguiseUser(User user) {
        disguiseController.unDisguise(user);
    }

    @Override
    public void rejoinPlayer(Player player) {
        BaseGameGroup gameGroup = getGameGroupForJoining(player.getUniqueId());

        if (gameGroup == null) {
            if (nameToGameGroup.isEmpty()) {
                //The fallback gamegroup config must not require parameters
                createGameGroup(fallbackConfig, new ArrayList<>());
            }
            gameGroup = getSpawnGameGroup();
        }

        BaseUser user = gameGroup.getUser(player.getUniqueId());
        BaseUser oldUser = getUser(player);

        boolean hadUser = user != null;

        if (user == null) {
            user = new BaseUser(gameGroup, null, player.getUniqueId(), player);
        }

        //Send an event to the old user's gamegroup notifying them that the user is no longer a player
        if (oldUser != null && oldUser != user && oldUser.isPlayer()) {
            BaseGameGroup oldGameGroup = oldUser.getGameGroup();
            UserQuitEvent quitEvent = new UserQuitEvent(oldUser, UserQuitEvent.QuitReason.CHANGED_GAMEGROUP);

            oldGameGroup.userEvent(quitEvent);
        }

        //If the user is rejoining, make them become a player
        if (hadUser) {
            user.becomePlayer(player);
        }

        gameGroup.userEvent(new UserJoinEvent(user, UserJoinEvent.JoinReason.JOINED_SERVER));

        final BaseUser finalUser = user;
        user.doInFuture(task -> {
            hideNonGameGroupPlayers(finalUser);
        });
    }

    @Override
    public boolean sendPlayerToHub(Player player) {
        if (hubServer == null) return false;
        Client client = protocol.getClient();

        if (client == null) return false;

        return client.changePlayerServer(player.getUniqueId(), hubServer);
    }

    @Override
    public BaseGameGroup createGameGroup(String type, List<String> params) {
        getLogger().info("Create game group " + type + " with params " + params);

        BaseGameGroup gameGroup =
                new BaseGameGroup(this, nextGameGroupName(type), type, gameGroupConfigMap.get(type), params);

        nameToGameGroup.put(gameGroup.getName(), gameGroup);

        getLogger().info("Created " + type + " gamegroup: " + gameGroup.getName());

        protocol.sendGameGroupSpawnedPayload(gameGroup);

        return gameGroup;
    }

    @Override
    public BaseGameGroup getSpawnGameGroup() {
        if (nameToGameGroup.isEmpty()) return null;

        return nameToGameGroup.values().iterator().next();
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public void preJoinGameGroup(UUID playerUUID, String type, String name, List<String> params) {
        doInFuture(task -> {
            playersJoiningGameGroupTypes.put(playerUUID, new JoiningGameGroupData(type, params));
            if (name != null) playersJoinGameGroups.put(playerUUID, name);

            Player player = plugin.getServer().getPlayer(playerUUID);

            if (player != null) {
                rejoinPlayer(player);
            }

        });
    }

    @Override
    public BaseUser getUser(Entity entity) {
        String mapName = entity.getWorld().getName();
        BaseGameGroup gameGroup = mapToGameGroup.get(mapName);

        if (gameGroup == null) return null;
        return gameGroup.getUser(entity.getUniqueId());
    }

    @Override
    public void checkResourcesRestart() {
        if (!Bukkit.getOnlinePlayers().isEmpty()) return;

        //Garbage collect first
        System.gc();


        long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        long actualFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;

        long freeMemoryMB = actualFreeMemory / (1024 * 1024);

        //400+ MB is a good amount apparently. This should really be put in a config
        if(freeMemoryMB > 400) return;

        ClientAPIProtocol apiProtocol = MSMPlugin.getApiProtocol();
        apiProtocol.scheduleRestart(1, 0);
    }

    @Override
    public GameTask doInFuture(GameRunnable task) {
        return doInFuture(task, 1);
    }

    @Override
    public GameTask doInFuture(GameRunnable task, int delay) {
        GameTask gameTask = new GameTask(task);

        gameTask.schedule(plugin, delay);
        return gameTask;
    }

    @Override
    public GameTask repeatInFuture(GameRunnable task, int delay, int period) {
        GameTask gameTask = new GameTask(task);

        gameTask.schedule(plugin, delay, period);
        return gameTask;
    }

    @Override
    public void cancelAllTasks() {
        throw new RuntimeException("You cannot cancel all game tasks");
    }

    private BaseGameGroup getGameGroupForJoining(UUID uniqueId) {
        String gameGroupName = playersJoinGameGroups.remove(uniqueId);

        if (gameGroupName != null && nameToGameGroup.containsKey(gameGroupName)) {
            return nameToGameGroup.get(gameGroupName);
        }

        JoiningGameGroupData data = playersJoiningGameGroupTypes.remove(uniqueId);

        if (data != null) {
            for (BaseGameGroup gameGroup : getGameGroups()) {
                if (!gameGroup.getType().equals(data.type)) continue;
                if (!gameGroup.isAcceptingPlayers()) continue;
                if (!data.params.isEmpty() && !data.params.equals(gameGroup.getParameters())) continue;

                return gameGroup;
            }

            return createGameGroup(data.type, data.params);
        }

        return null;
    }

    private void hideNonGameGroupPlayers(BaseUser user) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            BaseUser other = getUser(player);
            if (other != null && other.getGameGroup() == user.getGameGroup()) continue;

            user.getPlayer().hidePlayer(player);
            player.hidePlayer(user.getPlayer());
        }
    }

    private String nextGameGroupName(String configName) {
        String result;
        int counter = 0;

        do {
            result = name + "_" + configName + "_" + counter;
            ++counter;
        } while (nameToGameGroup.containsKey(result));

        return result;
    }

    public void removeGameGroupForMap(String mapName) {
        mapToGameGroup.remove(mapName);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormattedName() {
        return name;
    }

    @Override
    public Config loadConfig(String path) {
        try {
            return new BukkitConfig(YamlConfiguration.loadConfiguration(configDirectory.resolve(path).toFile()));
        } catch (Exception e) {
            throw new RuntimeException("Error while loading config at: " + path, e);
        }
    }

    @Override
    public LangFile loadLangFile(String path) {
        try {
            return new LangFile(configDirectory.resolve(path));
        } catch (IOException e) {
            System.out.println("Failed to load lang file: " + path);
            e.printStackTrace();
            return new LangFile(new Properties());
        }
    }

    @Override
    public JSONBook loadBook(String name, String pathName) {
        Path path = configDirectory.resolve(pathName);

        try {
            List<String> lines = Files.readAllLines(path);

            StringBuilder json = new StringBuilder();

            for (String line : lines) {
                json.append(line);
            }

            return new JSONBook(name, json.toString());
        } catch (IOException e) {
            System.out.println("Failed to load json book file: " + pathName);
            e.printStackTrace();
            return new JSONBook(name, "failed to load");
        }
    }

    @Override
    public void doDatabaseTask(DatabaseTask task) {
        persistence.doTask(task);
    }

    private static class JoiningGameGroupData {
        String type;

        List<String> params;

        public JoiningGameGroupData(String type, List<String> params) {
            this.type = type;
            this.params = params;
        }
    }

    private class TabCompleteListener extends PacketAdapter {

        public TabCompleteListener(Plugin plugin, ListenerPriority listenerPriority) {
            super(plugin, listenerPriority, PacketType.Play.Client.TAB_COMPLETE, PacketType.Play.Server.TAB_COMPLETE);
        }

        @Override
        public void onPacketReceiving(PacketEvent event) {
            if (event.getPacketType() != PacketType.Play.Client.TAB_COMPLETE) return;

            WrapperPlayClientTabComplete packet = new WrapperPlayClientTabComplete(event.getPacket());

            System.out.println(packet.getText());
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            if (event.getPacketType() != PacketType.Play.Server.TAB_COMPLETE) return;

            WrapperPlayServerTabComplete packet = new WrapperPlayServerTabComplete(event.getPacket());

            System.out.println(Arrays.toString(packet.getText()));
        }
    }
}
