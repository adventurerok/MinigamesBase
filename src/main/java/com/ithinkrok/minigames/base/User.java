package com.ithinkrok.minigames.base;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.base.command.MinigamesCommandSender;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.game.MapChangedEvent;
import com.ithinkrok.minigames.base.event.user.game.*;
import com.ithinkrok.minigames.base.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.base.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.base.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.base.inventory.ClickableInventory;
import com.ithinkrok.minigames.base.item.CustomItem;
import com.ithinkrok.minigames.base.map.GameMap;
import com.ithinkrok.minigames.base.metadata.MetadataHolder;
import com.ithinkrok.minigames.base.metadata.UserMetadata;
import com.ithinkrok.minigames.base.task.GameRunnable;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.task.TaskList;
import com.ithinkrok.minigames.base.task.TaskScheduler;
import com.ithinkrok.minigames.base.team.Team;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.minigames.base.user.AttackerTracker;
import com.ithinkrok.minigames.base.user.CooldownHandler;
import com.ithinkrok.minigames.base.user.UpgradeHandler;
import com.ithinkrok.minigames.base.user.UserResolver;
import com.ithinkrok.minigames.base.user.scoreboard.ScoreboardDisplay;
import com.ithinkrok.minigames.base.user.scoreboard.ScoreboardHandler;
import com.ithinkrok.minigames.base.util.InventoryUtils;
import com.ithinkrok.minigames.base.util.SoundEffect;
import com.ithinkrok.minigames.base.util.disguise.Disguise;
import com.ithinkrok.minigames.base.util.playerstate.PlayerState;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventExecutor;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

import static com.ithinkrok.minigames.base.util.InventoryUtils.createLeatherArmorItem;

/**
 * Created by paul on 31/12/15.
 */
@SuppressWarnings("unchecked")
public class User implements MinigamesCommandSender, TaskScheduler, Listener, UserResolver, MetadataHolder<UserMetadata>,
        SharedObjectAccessor, Nameable {

    private static final HashSet<Material> SEE_THROUGH = new HashSet<>();
    private static final Random random = new Random();

    static {
        SEE_THROUGH.add(Material.AIR);
        SEE_THROUGH.add(Material.WATER);
        SEE_THROUGH.add(Material.STATIONARY_WATER);
    }

    private final GameGroup gameGroup;
    private final UUID uuid;
    private final AttackerTracker fireAttacker = new AttackerTracker(this);
    private final AttackerTracker witherAttacker = new AttackerTracker(this);
    private final AttackerTracker lastAttacker = new AttackerTracker(this);
    private final UpgradeHandler upgradeHandler = new UpgradeHandler(this);
    private final CooldownHandler cooldownHandler = new CooldownHandler(this);
    private final ClassToInstanceMap<UserMetadata> metadataMap = MutableClassToInstanceMap.create();
    private final String name;
    private final TaskList userTaskList = new TaskList();
    private final TaskList inGameTaskList = new TaskList();
    private final Collection<CustomListener> listeners = new ArrayList<>();
    private Team team;
    private Kit kit;
    private LivingEntity entity;
    private PlayerState playerState;
    private Disguise disguise;
    private boolean cloaked = false;
    private boolean showCloakedPlayers = false;
    private ScoreboardDisplay scoreboardDisplay;
    private ScoreboardHandler scoreboardHandler;
    private boolean isInGame = false;
    private ClickableInventory openInventory;
    private Collection<CustomListener> kitListeners = new ArrayList<>();

    private Vector inventoryTether;
    private boolean spectator;

    private GameTask revalidateTask;

    public User(GameGroup gameGroup, Team team, UUID uuid, LivingEntity entity) {
        this.gameGroup = gameGroup;
        this.team = team;
        this.uuid = uuid;
        this.entity = entity;

        this.name = entity.getName();
        listeners.add(new UserListener());

        if (isPlayer()) {
            scoreboardDisplay = new ScoreboardDisplay(this, getPlayer());
        }

        unDisguise();
        fixCloakedUsers();

        repeatInFuture(task -> decrementAttackerTimers(), 20, 20);
    }

    public void fixCloakedUsers() {
        for(User u : gameGroup.getUsers()) {
            if(this == u) continue;

            if(u.isCloaked()) hidePlayer(u);
            else{
                hidePlayer(u);

                doInFuture(task -> {
                    if(!u.isCloaked()) {
                        showPlayer(u);
                    }
                });
            }

            if(isCloaked()) u.hidePlayer(this);
            else {
                u.hidePlayer(this);

                doInFuture(task -> {
                    if(!isCloaked()) {
                        u.showPlayer(this);
                    }
                });
            }
        }
    }

    public boolean isPlayer() {
        return entity instanceof Player;
    }

    public Player getPlayer() {
        if (!isPlayer()) throw new RuntimeException("You have no player");
        return (Player) entity;
    }

    private void decrementAttackerTimers() {
        lastAttacker.decreaseAttackerTimer(20);
        fireAttacker.decreaseAttackerTimer(20);
        witherAttacker.decreaseAttackerTimer(20);
    }

    public ClickableInventory getOpenInventory() {
        return openInventory;
    }

    public void setShowCloakedUsers(boolean showCloakedPlayers) {
        this.showCloakedPlayers = showCloakedPlayers;

        for (User u : gameGroup.getUsers()) {
            if (this == u) continue;

            if (!u.isCloaked()) continue;

            if (showCloakedPlayers) showPlayer(u);
            else hidePlayer(u);
        }
    }

    public void becomeEntity(EntityType entityType) {
        if (!isPlayer()) return;

        makeEntityFromEntity(entity, getLocation(), entityType);

        scoreboardDisplay = null;
        openInventory = null;

        revalidateTask = repeatInFuture(task -> revalidateNonPlayer(entity.getLocation()), 100, 100);
    }

    private void makeEntityFromEntity(LivingEntity from, Location location, EntityType entityType) {
        if (playerState == null) playerState = new PlayerState();

        playerState.capture(from);
        entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);
        playerState.restore(entity);
        playerState.setPlaceholder(entity);

        if (entity instanceof Zombie) {
            ((Zombie) entity).setVillager(false);
            ((Zombie) entity).setBaby(false);
        }

        gameGroup.getGame().makeEntityActualUser(this, entity);

        entity.setRemoveWhenFarAway(true);

        if(disguise != null) disguise(disguise);
    }

    public Location getLocation() {
        return entity.getLocation();
    }

    private boolean revalidateNonPlayer(Location loc) {
        if (isPlayer() || entity.isValid()) return false;

        LivingEntity oldEntity = entity;
        makeEntityFromEntity(oldEntity, loc, oldEntity.getType());

        oldEntity.remove();

        return true;
    }

    public void launchVictoryFirework() {
        Location loc = getLocation();

        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

        Color color = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        Color fade = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));

        firework.setVelocity(new Vector(0, 0.5f, 0));
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST).trail(true).withColor(color)
                .withFade(fade).build());
        firework.setFireworkMeta(meta);
    }

    public void becomePlayer(Player player) {
        if (isPlayer()) return;

        revalidateTask.cancel();

        playerState.capture(entity);

        player.teleport(entity.getLocation());
        playerState.restore(player);
        playerState.setPlaceholder(null);

        entity.remove();
        entity = player;

        if (isCloaked()) cloak();

        scoreboardDisplay = new ScoreboardDisplay(this, player);
        updateScoreboard();

        if(disguise != null) disguise(disguise);
    }

    public boolean isCloaked() {
        return cloaked;
    }

    public void cloak() {
        cloaked = true;

        for (User u : gameGroup.getUsers()) {
            if (this == u) continue;

            if (u.showCloakedUsers()) continue;

            u.hidePlayer(this);
        }
    }

    public void updateScoreboard() {
        if (scoreboardDisplay == null || scoreboardHandler == null) return;

        scoreboardHandler.updateScoreboard(this, scoreboardDisplay);
    }

    public boolean showCloakedUsers() {
        return showCloakedPlayers;
    }

    private void hidePlayer(User other) {
        if (!isPlayer() || !other.isPlayer()) return;
        getPlayer().hidePlayer(other.getPlayer());
    }

    public void removeNonPlayer() {
        if (isPlayer()) return;
        entity.remove();

        revalidateTask.cancel();

        gameGroup.userEvent(new UserQuitEvent(this, UserQuitEvent.QuitReason.NON_PLAYER_REMOVED));
    }

    public void disguise(EntityType type) {
        disguise(new Disguise(type));
    }

    public void disguise(Disguise disguise) {
        this.disguise = disguise;
        gameGroup.getGame().disguiseUser(this, disguise);
    }

    public void unDisguise() {
        this.disguise = null;
        gameGroup.getGame().unDisguiseUser(this);
    }

    public void setAllowFlight(boolean allowFlight) {
        if (isPlayer()) getPlayer().setAllowFlight(allowFlight);
        else playerState.setAllowFlight(allowFlight);
    }

    public void setFlySpeed(double flySpeed) {
        if (isPlayer()) getPlayer().setFlySpeed((float) flySpeed);
        else playerState.setFlySpeed((float) flySpeed);
    }

    public void resetUserStats(boolean removePotionEffects) {
        Config defaultStats = getSharedObjectOrEmpty("user").getConfigOrEmpty("default_stats");

        setMaxHealth(defaultStats.getDouble("max_health", 10) * 2);
        setHealth(defaultStats.getDouble("health", 10) * 2);
        setFoodLevel((int) (defaultStats.getDouble("food_level", 10) * 2));
        setSaturation(defaultStats.getDouble("saturation", 5.0));
        setFlySpeed(defaultStats.getDouble("fly_speed", 0.1));
        setWalkSpeed(defaultStats.getDouble("walk_speed", 0.2));

        if (removePotionEffects) removePotionEffects();
    }

    public void setMaxHealth(double maxHealth) {
        entity.setMaxHealth(maxHealth);
    }

    public void setFoodLevel(int foodLevel) {
        if (isPlayer()) getPlayer().setFoodLevel(foodLevel);
        else playerState.setFoodLevel(foodLevel);
    }

    public void setSaturation(double saturation) {
        if (isPlayer()) getPlayer().setSaturation((float) saturation);
        else playerState.setSaturation((float) saturation);
    }

    public void setWalkSpeed(double walkSpeed) {
        if (isPlayer()) getPlayer().setWalkSpeed((float) walkSpeed);
        else playerState.setWalkSpeed((float) walkSpeed);
    }

    public void removePotionEffects() {
        List<PotionEffect> effects = new ArrayList<>(entity.getActivePotionEffects());

        for (PotionEffect effect : effects) {
            entity.removePotionEffect(effect.getType());
        }

        entity.setFireTicks(0);
    }

    public void setCollidesWithEntities(boolean collides) {
        if (!isPlayer()) return;

        getPlayer().spigot().setCollidesWithEntities(collides);
    }

    public void decloak() {
        cloaked = false;

        for (User u : gameGroup.getUsers()) {
            if (this == u) continue;

            u.showPlayer(this);
        }
    }

    private void showPlayer(User other) {
        if (!isPlayer() || !other.isPlayer()) return;
        getPlayer().showPlayer(other.getPlayer());
    }

    public String getTeamName() {
        return team != null ? team.getName() : null;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        if (team == this.team) return;

        Team oldTeam = this.team;
        Team newTeam = this.team = team;

        if (oldTeam != null) oldTeam.removeUser(this);
        if (newTeam != null) newTeam.addUser(this);

        UserChangeTeamEvent event = new UserChangeTeamEvent(this, oldTeam, newTeam);
        gameGroup.userEvent(event);
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        if (kit == this.kit) return;

        Kit oldKit = this.kit;
        Kit newKit = this.kit = kit;

        if (oldKit != null) {
            this.listeners.removeAll(kitListeners);
        }

        if (newKit != null) {
            this.listeners.addAll(kitListeners = kit.createListeners(this));
        }

        UserChangeKitEvent event = new UserChangeKitEvent(this, oldKit, newKit);
        gameGroup.userEvent(event);
    }

    public TeamIdentifier getTeamIdentifier() {
        return team != null ? team.getTeamIdentifier() : null;
    }

    public Collection<CustomListener> getListeners() {
        return listeners;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }

    @Override
    public User getUser(UUID uuid) {
        return gameGroup.getUser(uuid);
    }

    public boolean isInGame() {
        return isInGame;
    }

    @SuppressWarnings("unchecked")
    public void setInGame(boolean inGame) {
        isInGame = inGame;

        inGameTaskList.cancelAllTasks();

        if (!inGame) {
            upgradeHandler.clearUpgrades();
            cooldownHandler.cancelCoolDowns();

            inventoryTether = null;
            openInventory = null;
            setKit(null);
            setTeam(null);
        } else {
            showCloakedPlayers = false;
        }

        gameGroup.userEvent(new UserInGameChangeEvent(this));
    }

    public void setSpectator(boolean spectator) {
        if (spectator == this.spectator) return;
        if (spectator && isInGame())
            throw new RuntimeException("You cannot be a spectator when you are already in a game");

        this.spectator = spectator;

        if (!isPlayer()) return;

        if (spectator) {
            cloak();
            resetUserStats(true);

            setAllowFlight(true);
            setCollidesWithEntities(false);
            getInventory().clear();
            clearArmor();

            updateScoreboard();
        } else {
            setAllowFlight(false);
            setCollidesWithEntities(true);

            decloak();
        }

        gameGroup.userEvent(new UserSpectatorChangeEvent(this, spectator));
    }

    @Override
    public <B extends UserMetadata> B getMetadata(Class<? extends B> clazz) {
        return metadataMap.getInstance(clazz);
    }

    @Override
    public <B extends UserMetadata> void setMetadata(B metadata) {
        UserMetadata oldMetadata = metadataMap.put(metadata.getMetadataClass(), metadata);

        if (oldMetadata != null && oldMetadata != metadata) {
            oldMetadata.cancelAllTasks();
            oldMetadata.removed();
        }
    }

    @Override
    public boolean hasMetadata(Class<? extends UserMetadata> clazz) {
        return metadataMap.containsKey(clazz);
    }

    public void setScoreboardHandler(ScoreboardHandler scoreboardHandler) {
        this.scoreboardHandler = scoreboardHandler;
        if (scoreboardDisplay != null) {
            if (scoreboardHandler == null) scoreboardDisplay.remove();
            else scoreboardHandler.setupScoreboard(this, scoreboardDisplay);
        }
    }

    public GameMode getGameMode() {
        return isPlayer() ? getPlayer().getGameMode() : playerState.getGameMode();
    }

    public void setGameMode(GameMode gameMode) {
        if (isPlayer()) getPlayer().setGameMode(gameMode);
        else playerState.setGameMode(gameMode);
    }

    public int getFireTicks() {
        return entity.getFireTicks();
    }

    public void setFireTicks(User fireAttacker, int fireTicks) {
        this.fireAttacker.setAttacker(fireAttacker, fireTicks);
        entity.setFireTicks(fireTicks);
    }

    public void setWitherTicks(User witherAttacker, int witherTicks) {
        setWitherTicks(witherAttacker, witherTicks, 0);
    }

    /**
     * @param witherAmplifier The amplifier for the effect. Level n is amplifier n-1.
     */
    public void setWitherTicks(User witherAttacker, int witherTicks, int witherAmplifier) {
        this.witherAttacker.setAttacker(witherAttacker, witherTicks);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherAmplifier, witherAmplifier));
    }

    public boolean startCoolDown(String ability, int seconds, String coolDownLocale) {
        return cooldownHandler.startCoolDown(ability, seconds, coolDownLocale);
    }

    public void stopCoolDown(String ability, String stopLocale) {
        cooldownHandler.stopCoolDown(ability, stopLocale);
    }

    public void playSound(Location location, SoundEffect sound) {
        if (!isPlayer()) return;

        getPlayer().playSound(location, sound.getSound(), sound.getVolume(), sound.getPitch());
    }

    public boolean isCoolingDown(String ability) {
        return cooldownHandler.isCoolingDown(ability);
    }

    public int getUpgradeLevel(String upgrade) {
        return upgradeHandler.getUpgradeLevel(upgrade);
    }

    public void setUpgradeLevel(String upgrade, int level) {
        upgradeHandler.setUpgradeLevel(upgrade, level);
    }

    public ItemStack createCustomItemForUser(CustomItem item) {
        return item.createWithVariables(gameGroup, upgradeHandler);
    }

    public <T extends Projectile> T launchProjectile(Class<? extends T> projectileClass) {
        return entity.launchProjectile(projectileClass);
    }

    public UpgradeHandler getUpgradeLevels() {
        return upgradeHandler;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean teleport(Vector loc) {
        Location target =
                new Location(getLocation().getWorld(), loc.getX(), loc.getY(), loc.getZ(), getLocation().getYaw(),
                        getLocation().getPitch());
        return teleport(target);

    }

    @SuppressWarnings("unchecked")
    public boolean teleport(Location location) {
        UserTeleportEvent event = new UserTeleportEvent(this, getLocation(), location);

        gameGroup.userEvent(event);

        if (event.isCancelled()) return false;
        boolean success = entity.teleport(event.getTo());

        fixCloakedUsers();

        return revalidateNonPlayer(event.getTo()) || success;
    }

    public boolean isViewingClickableInventory() {
        return openInventory != null;
    }

    public ClickableInventory getClickableInventory() {
        return openInventory;
    }

    @SuppressWarnings("unchecked")
    public void showInventory(ClickableInventory inventory, Location inventoryTether) {
        doInFuture(task -> {
            if (!isPlayer()) return;

            this.openInventory = inventory;
            this.inventoryTether = inventoryTether != null ? inventoryTether.toVector() : null;
            getPlayer().openInventory(inventory.createInventory(this));
        });
    }

    @Override
    public GameTask doInFuture(GameRunnable task) {
        GameTask gameTask = gameGroup.doInFuture(task);

        userTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask doInFuture(GameRunnable task, int delay) {
        GameTask gameTask = gameGroup.doInFuture(task, delay);

        userTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask repeatInFuture(GameRunnable task, int delay, int period) {
        GameTask gameTask = gameGroup.repeatInFuture(task, delay, period);

        userTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public void cancelAllTasks() {
        userTaskList.cancelAllTasks();
    }

    public void redoInventory() {
        if (this.openInventory == null || !isPlayer()) return;

        Inventory viewing = getPlayer().getOpenInventory().getTopInventory();
        viewing.clear();
        this.openInventory.populateInventory(viewing, this);
    }

    public Location getInventoryTether() {
        return gameGroup.getCurrentMap().getLocation(inventoryTether);
    }

    public void setXpLevel(int level) {
        if (isPlayer()) getPlayer().setLevel(level);
        else playerState.setLevel(level);
    }

    public float getExp() {
        return isPlayer() ? getPlayer().getExp() : playerState.getExp();
    }

    public void setExp(double exp) {
        if (isPlayer()) getPlayer().setExp((float) exp);
        else playerState.setExp((float) exp);
    }

    public void bindTaskToInGame(GameTask task) {
        inGameTaskList.addTask(task);
    }

    public String getTabListName() {
        if (!isPlayer()) return playerState.getTabListName();
        else return getPlayer().getPlayerListName();
    }

    public void setTabListName(String tabListName) {
        if (!isPlayer()) playerState.setTabListName(tabListName);
        else getPlayer().setPlayerListName(tabListName);
    }

    public Block rayTraceBlocks(int maxDistance) {
        return entity.getTargetBlock(SEE_THROUGH, maxDistance);
    }

    @Override
    public boolean hasPermission(String permission) {
        return entity.hasPermission(permission);
    }

    public Inventory createInventory(int size, String title) {
        size = ((size / 9) + 1) * 9;

        return Bukkit.createInventory((InventoryHolder) entity, size, title);
    }

    public void closeInventory() {
        doInFuture(task -> {
            if (!isPlayer()) return;

            openInventory = null;
            inventoryTether = null;
            getPlayer().closeInventory();
        });
    }

    @Override
    public boolean hasSharedObject(String name) {
        return gameGroup.hasSharedObject(name);
    }

    @Override
    public Config getSharedObject(String name) {
        return gameGroup.getSharedObject(name);
    }

    @Override
    public Config getSharedObjectOrEmpty(String name) {
        return gameGroup.getSharedObjectOrEmpty(name);
    }

    public void giveColoredArmor(Color color, boolean unbreakable) {
        giveColoredArmor(color, unbreakable, true, true, true, true);
    }

    public void giveColoredArmor(Color color, boolean unbreakable, boolean helmet, boolean chestplate, boolean leggings,
                                 boolean boots) {
        PlayerInventory inv = getInventory();
        if (color == null) clearArmor(helmet, chestplate, leggings, boots);
        else {
            if (helmet) {
                inv.setHelmet(createLeatherArmorItem(Material.LEATHER_HELMET, color, unbreakable));
            }
            if (chestplate) {
                inv.setChestplate(createLeatherArmorItem(Material.LEATHER_CHESTPLATE, color, unbreakable));
            }
            if (leggings) {
                inv.setLeggings(createLeatherArmorItem(Material.LEATHER_LEGGINGS, color, unbreakable));
            }
            if (boots) {
                inv.setBoots(createLeatherArmorItem(Material.LEATHER_BOOTS, color, unbreakable));
            }
        }
    }

    public PlayerInventory getInventory() {
        return isPlayer() ? getPlayer().getInventory() : playerState.getInventory();
    }

    public void clearArmor(boolean helmet, boolean chestplate, boolean leggings, boolean boots) {
        EntityEquipment equipment = entity.getEquipment();

        if (helmet) equipment.setHelmet(null);
        if (chestplate) equipment.setChestplate(null);
        if (leggings) equipment.setLeggings(null);
        if (boots) equipment.setBoots(null);
    }

    public void clearArmor() {
        clearArmor(true, true, true, true);
    }

    public String getKitName() {
        return kit != null ? kit.getName() : null;
    }

    public Location getCompassTarget() {
        if (!isPlayer()) return null;
        return getPlayer().getCompassTarget();
    }

    public void setCompassTarget(Location compassTarget) {
        if (isPlayer()) getPlayer().setCompassTarget(compassTarget);
    }

    public void addPotionEffect(PotionEffect effect) {
        entity.addPotionEffect(effect);
    }

    public void addPotionEffect(PotionEffect effect, boolean force) {
        entity.addPotionEffect(effect, force);
    }

    public double getHeath() {
        return entity.getHealth();
    }

    public User getFireAttacker() {
        return fireAttacker.getAttacker();
    }

    public User getWitherAttacker() {
        return witherAttacker.getAttacker();
    }

    public User getLastAttacker() {
        return lastAttacker.getAttacker();
    }

    public void setLastAttacker(User lastAttacker) {
        this.lastAttacker.setAttacker(lastAttacker);
    }

    public boolean isInsideVehicle() {
        return entity.isInsideVehicle();
    }

    public Entity getVehicle() {
        return entity.getVehicle();
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public TNTPrimed createExplosion(Location loc, float power, boolean fire, int fuseTicks) {
        TNTPrimed tnt = (TNTPrimed) getMap().spawnEntity(loc, EntityType.PRIMED_TNT);

        makeEntityRepresentUser(tnt);

        tnt.setIsIncendiary(fire);
        tnt.setYield(power);

        tnt.setFuseTicks(fuseTicks);

        return tnt;
    }

    public GameMap getMap() {
        return gameGroup.getCurrentMap();
    }

    public void makeEntityRepresentUser(Entity entity) {
        gameGroup.getGame().makeEntityRepresentUser(this, entity);
    }

    public boolean isFlying() {
        return isPlayer() && getPlayer().isFlying();
    }

    public void setFlying(boolean flying) {
        if (!isPlayer()) return;
        getPlayer().setFlying(flying);
    }

    public double getHealth() {
        return entity.getHealth();
    }

    public void setHealth(double health) {
        entity.setHealth(health);
    }

    public boolean hasPotionEffect(PotionEffectType type) {
        return entity.hasPotionEffect(type);
    }

    public void setFallDistance(double fallDistance) {
        entity.setFallDistance((float) fallDistance);
    }

    public boolean isOnGround() {
        return entity.isOnGround();
    }

    public boolean unstuck(int maxRadius) {
        Block base = getLocation().add(0, 1, 0).getBlock();
        Block block;

        for (int radius = 0; radius < maxRadius; ++radius) {
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    int state = 0;
                    for (int y = radius + 1; y >= -radius - 2; --y) {
                        if (Math.abs(x) < radius && Math.abs(y) + 3 < radius && Math.abs(z) < radius) continue;
                        block = base.getRelative(x, y, z);

                        boolean air = block.getType().isTransparent() || block.isLiquid();
                        if (!air && state < 2) {
                            state = 0;
                            continue;
                        } else if (air && state == 2) continue;
                        else if (state < 3) {
                            ++state;
                            continue;
                        }

                        Location teleport = block.getLocation().clone().add(0.5, 2.0, 0.5);
                        teleport.setPitch(getLocation().getPitch());
                        teleport.setYaw(getLocation().getYaw());

                        teleport(teleport);
                        setVelocity(new Vector(0, -1, 0));
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void setVelocity(Vector velocity) {
        entity.setVelocity(velocity);
    }

    public void removeFromGameGroup() {
        setScoreboardHandler(null);

        setInGame(false);

        setTabListName(getName());
        setDisplayName(getName());

        for (UserMetadata metadata : metadataMap.values()) {
            metadata.removed();
        }

        metadataMap.clear();
    }    @Override
    public void sendLocale(String locale, Object... args) {
        sendMessage(gameGroup.getLocale(locale, args));
    }

    private class UserListener implements CustomListener {

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventInGameChange(UserInGameChangeEvent event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if (metadata.removeOnInGameChange(event)) iterator.remove();
            }
        }

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventGameStateChange(GameStateChangedEvent event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if (metadata.removeOnGameStateChange(event)) {
                    metadata.cancelAllTasks();
                    metadata.removed();
                    iterator.remove();
                }
            }
        }

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventMapChange(MapChangedEvent event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if (metadata.removeOnMapChange(event)) {
                    metadata.cancelAllTasks();
                    metadata.removed();
                    iterator.remove();
                }
            }
        }

        @CustomEventHandler
        public void eventInventoryClick(UserInventoryClickEvent event) {
            if (!isViewingClickableInventory()) return;

            getClickableInventory().inventoryClick(event);
        }

        @CustomEventHandler
        public void eventUpgrade(UserUpgradeEvent event) {
            PlayerInventory inv = getInventory();

            for (int index = 0; index < inv.getSize(); ++index) {
                ItemStack old = inv.getItem(index);

                int id = InventoryUtils.getIdentifier(old);
                if (id < 0) continue;

                CustomItem customItem = gameGroup.getCustomItem(id);
                if (!customItem.replaceOnUpgrade()) continue;

                ItemStack replace = customItem.createForUser(User.this);
                if (replace.isSimilar(old)) continue;

                replace.setAmount(old.getAmount());

                inv.setItem(index, replace);
            }
        }

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventInventoryClose(UserInventoryCloseEvent event) {
            if (!isViewingClickableInventory()) return;

            openInventory = null;
        }

        @CustomEventHandler(priority = CustomEventHandler.HIGH)
        public void eventInteract(UserInteractEvent event) {
            ItemStack item = getInventory().getItemInHand();
            int identifier = InventoryUtils.getIdentifier(item);
            if (identifier < 0) return;

            CustomItem customItem = gameGroup.getCustomItem(identifier);

            //If event is a UserAttackEvent this will call both event handler methods in CustomItem
            CustomEventExecutor.executeEvent(event, customItem);
        }

        @CustomEventHandler
        public void eventAbilityCooldown(UserAbilityCooldownEvent event) {
            for (ItemStack item : getInventory()) {
                int identifier = InventoryUtils.getIdentifier(item);
                if (identifier < 0) continue;

                CustomItem customItem = gameGroup.getCustomItem(identifier);

                CustomEventExecutor.executeEvent(event, customItem);
            }
        }
    }

    public EntityType getVisibleEntityType() {
        if(disguise != null) return disguise.getEntityType();
        else return entity.getType();
    }


    @Override
    public void sendMessage(String message) {
        sendMessageNoPrefix(gameGroup.getChatPrefix() + message);
    }


    @Override
    public void sendMessageNoPrefix(String message) {
        entity.sendMessage(message);
    }


    @Override
    public void sendLocaleNoPrefix(String locale, Object... args) {
        sendMessageNoPrefix(gameGroup.getLocale(locale, args));
    }


    @Override
    public String getFormattedName() {
        String displayName = getDisplayName();
        return displayName != null ? displayName : getName();
    }


    @Override
    public String getName() {
        return name;
    }


    public void setDisplayName(String displayName) {
        if (!isPlayer()) entity.setCustomName(displayName);
        else getPlayer().setDisplayName(displayName);

        if(disguise != null && disguise.isShowUserNameAboveEntity()) disguise(disguise);
    }


    public String getDisplayName() {
        return isPlayer() ? getPlayer().getDisplayName() : entity.getCustomName();
    }


    @Override
    public LanguageLookup getLanguageLookup() {
        return gameGroup.getLanguageLookup();
    }


}
