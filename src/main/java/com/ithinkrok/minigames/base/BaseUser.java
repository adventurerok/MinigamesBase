package com.ithinkrok.minigames.base;

import com.comphenix.packetwrapper.WrapperPlayServerChat;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.user.game.*;
import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.api.event.user.inventory.UserInventoryUpdateEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.item.CombatMode;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.item.WeaponStats;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.api.metadata.UserMetadata;
import com.ithinkrok.minigames.api.task.GameRunnable;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskList;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.user.AttackerTracker;
import com.ithinkrok.minigames.api.user.CooldownHandler;
import com.ithinkrok.minigames.api.user.UserVariableHandler;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardDisplay;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardHandler;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.minigames.api.util.disguise.Disguise;
import com.ithinkrok.minigames.base.util.playerstate.PlayerState;
import com.ithinkrok.msm.bukkit.util.PlayerMessageSender;
import com.ithinkrok.msm.common.economy.Account;
import com.ithinkrok.msm.common.economy.EconomyAccount;
import com.ithinkrok.msm.common.message.ConfigMessageFactory;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.JsonConfigIO;
import com.ithinkrok.util.event.CustomEventExecutor;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

import static com.ithinkrok.minigames.api.util.InventoryUtils.createLeatherArmorItem;

/**
 * Created by paul on 31/12/15.
 */
@SuppressWarnings("unchecked")
public class BaseUser implements Listener, User {

    private static final HashSet<Material> SEE_THROUGH = new HashSet<>();
    private static final Random random = new Random();

    static {
        SEE_THROUGH.add(Material.AIR);
        SEE_THROUGH.add(Material.WATER);
        SEE_THROUGH.add(Material.STATIONARY_WATER);
    }

    private final BaseGameGroup gameGroup;
    private final UUID uuid;
    private final AttackerTracker fireAttacker = new AttackerTracker(this);
    private final AttackerTracker witherAttacker = new AttackerTracker(this);
    private final AttackerTracker lastAttacker = new AttackerTracker(this);
    private final UserVariableHandler userVariableHandler = new UserVariableHandler(this);
    private final CooldownHandler cooldownHandler = new CooldownHandler(this);
    private final ClassToInstanceMap<UserMetadata> metadataMap = MutableClassToInstanceMap.create();
    private final String name;
    private final TaskList userTaskList = new TaskList();
    private final TaskList inGameTaskList = new TaskList();
    private final Collection<CustomListener> listeners = new ArrayList<>();
    private BaseTeam team;
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

    private final Account economyAccount;

    private MapPoint inventoryTether;
    private boolean spectator;

    private GameTask revalidateTask;

    public BaseUser(BaseGameGroup gameGroup, BaseTeam team, UUID uuid, LivingEntity entity) {
        this.gameGroup = gameGroup;
        this.team = team;
        this.uuid = uuid;
        this.entity = entity;
        this.economyAccount = new EconomyAccount(gameGroup.getEconomy(), uuid);

        this.name = entity.getName();
        listeners.add(new UserListener());

        if (isPlayer()) {
            scoreboardDisplay = new ScoreboardDisplay(this, getPlayer());
        }

        unDisguise();
        fixCloakedUsers();

        repeatInFuture(task -> decrementAttackerTimers(), 20, 20);

        //This just prevents weapon damage values being wrong for more than a second, vastly limiting possible exploits
        repeatInFuture(task -> checkAttributes(), 20, 20);
    }

    @Override
    public void fixCloakedUsers() {
        for (BaseUser u : gameGroup.getUsers()) {
            if (this == u) continue;

            if (u.isCloaked()) hidePlayer(u);
            else {
                //                hidePlayer(u);

                doInFuture(task -> {
                    if (!u.isCloaked()) {
                        showPlayer(u);
                    }
                });
            }

            if (isCloaked()) u.hidePlayer(this);
            else {
                //                u.hidePlayer(this);

                doInFuture(task -> {
                    if (!isCloaked()) {
                        u.showPlayer(this);
                    }
                });
            }
        }
    }

    private void decrementAttackerTimers() {
        lastAttacker.decreaseAttackerTimer(20);
        fireAttacker.decreaseAttackerTimer(20);
        witherAttacker.decreaseAttackerTimer(20);
    }

    private void showPlayer(User other) {
        if (!isPlayer() || !other.isPlayer()) return;
        getPlayer().showPlayer(other.getPlayer());
    }

    @Override
    public User getUser(UUID uuid) {
        return gameGroup.getUser(uuid);
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

    @Override
    public <B extends UserMetadata> B removeMetadata(Class<? extends B> clazz) {
        B metadata = (B) metadataMap.remove(clazz);

        if (metadata != null) metadata.removed();

        return metadata;
    }

    @Override
    public boolean hasPermission(String permission) {
        return entity.hasPermission(permission);
    }

    @Override
    public boolean hasSharedObject(String name) {
        return gameGroup.hasSharedObject(name);
    }    @Override
    public boolean isPlayer() {
        return entity instanceof Player;
    }

    @Override
    public Config getSharedObject(String name) {
        return gameGroup.getSharedObject(name);
    }

    @Override
    public Config getSharedObjectOrEmpty(String name) {
        return gameGroup.getSharedObjectOrEmpty(name);
    }

    private void hidePlayer(User other) {
        if (!isPlayer() || !other.isPlayer()) return;
        getPlayer().hidePlayer(other.getPlayer());
    }

    private class UserListener implements CustomListener {

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventInGameChange(UserInGameChangeEvent event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if (metadata.removeOnInGameChange(event)) {
                    metadata.cancelAllTasks();
                    metadata.removed();
                    iterator.remove();
                }
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
        public void eventUpgrade(UserVariableChangeEvent event) {
            PlayerInventory inv = getInventory();

            for (int index = 0; index < inv.getSize(); ++index) {
                ItemStack old = inv.getItem(index);

                ItemStack replace = upgradeItem(old);
                if (replace == null) continue;

                inv.setItem(index, replace);
            }

            ItemStack[] armor = inv.getArmorContents();

            for (int index = 0; index < armor.length; ++index) {
                ItemStack old = armor[index];

                ItemStack replace = upgradeItem(old);
                if (replace == null) continue;

                armor[index] = replace;
            }

            inv.setArmorContents(armor);
        }

        private ItemStack upgradeItem(ItemStack old) {
            int id = InventoryUtils.getIdentifier(old);
            if (id < 0) return null;

            CustomItem customItem = gameGroup.getCustomItem(id);
            if (!customItem.replaceOnUpgrade()) return null;

            ItemStack replace = customItem.createForUser(BaseUser.this);
            if (replace.isSimilar(old)) return null;

            replace.setAmount(old.getAmount());
            return replace;
        }

        @CustomEventHandler(priority = CustomEventHandler.INTERNAL_FIRST)
        public void eventInventoryClose(UserInventoryCloseEvent event) {
            if (!isViewingClickableInventory()) return;

            openInventory = null;
        }

        @CustomEventHandler(priority = CustomEventHandler.HIGH)
        public void eventInteract(UserInteractEvent event) {
            ItemStack item = event.getItem();
            int identifier = InventoryUtils.getIdentifier(item);
            if (identifier < 0) return;

            CustomItem customItem = gameGroup.getCustomItem(identifier);

            //If event is a UserAttackEvent this will call both event handler methods in CustomItem
            CustomEventExecutor.executeEvent(event, customItem);
        }


        @CustomEventHandler(priority = CustomEventHandler.HIGH)
        public void eventInventoryUpdate(UserInventoryUpdateEvent event) {

            doInFuture(task -> {
                checkAttributes();
                //Call CustomItem listeners
            }, 1);

            ItemStack newItem = getInventory().getItemInMainHand();

            int newIdentifier = InventoryUtils.getIdentifier(newItem);
            if (newIdentifier > 0) {
                CustomItem customItem = gameGroup.getCustomItem(newIdentifier);

                CustomEventExecutor.executeEvent(event, customItem);
            }
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

    private void checkAttributes() {
        AttributeInstance damage = getEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        AttributeInstance speed = getEntity().getAttribute(Attribute.GENERIC_ATTACK_SPEED);


        Config userShared = getGameGroup().getSharedObjectOrEmpty("user");
        boolean useNewCombat = userShared.getBoolean("use_new_combat", false);

        ItemStack newItem = getInventory().getItemInMainHand();

        //We will check to see if there is a custom item overriding combat mode
        CustomItem customItem = null;
        if(newItem != null) {
            int identifier = InventoryUtils.getIdentifier(newItem);
            if(identifier >= 0 ) {
                customItem = getGameGroup().getCustomItem(identifier);
            }
        }
        if(customItem != null && customItem.getCombatMode() != CombatMode.INHERIT) {
            useNewCombat = customItem.getCombatMode() == CombatMode.NEW;
        }


        //Remove our custom damage and speed overrides
        for (AttributeModifier modifier : damage.getModifiers()) {
            if("Damage Override".equals(modifier.getName())) {
                damage.removeModifier(modifier);
            }
        }

        if(speed != null) {
            for (AttributeModifier modifier : speed.getModifiers()) {
                if("Speed Override".equals(modifier.getName())) {
                    speed.removeModifier(modifier);
                }
            }
        }


        if(!useNewCombat) {
            //Remove 1.9+ damage values
            for (AttributeModifier modifier : damage.getModifiers()) {
                if ("Tool modifier".equals(modifier.getName()) ||
                        "Weapon modifier".equals(modifier.getName())) {
                    damage.removeModifier(modifier);
                }
            }

            //Remove 1.9 attack speed
            if(speed != null) {
                for (AttributeModifier attributeModifier : speed.getModifiers()) {
                    speed.removeModifier(attributeModifier);
                }


                speed.addModifier(
                        new AttributeModifier("Speed Override", 4.0, AttributeModifier.Operation.ADD_NUMBER));
            }
        }



        //Set damage to legacy values, if required
        if (newItem != null && !useNewCombat) {
            double legacyDamageModifier =
                    WeaponStats.getLegacyDamage(newItem.getType()) - damage.getBaseValue();

            if (legacyDamageModifier != 0) {
                damage.addModifier(new AttributeModifier("Damage Override", legacyDamageModifier,
                                                         AttributeModifier.Operation.ADD_NUMBER));
            }
        }

    }


    @Override
    public Player getPlayer() {
        if (!isPlayer()) throw new RuntimeException("You have no player");
        return (Player) entity;
    }


    @Override
    public ClickableInventory getOpenInventory() {
        return openInventory;
    }


    @Override
    public void setShowCloakedUsers(boolean showCloakedPlayers) {
        this.showCloakedPlayers = showCloakedPlayers;

        for (User u : gameGroup.getUsers()) {
            if (this == u) continue;

            if (!u.isCloaked()) continue;

            if (showCloakedPlayers) showPlayer(u);
            else hidePlayer(u);
        }
    }


    @Override
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
            ((Zombie) entity).setBaby(false);
        }

        gameGroup.getGame().makeEntityActualUser(this, entity);

        entity.setRemoveWhenFarAway(true);

        if (disguise != null) disguise(disguise);
    }


    private boolean revalidateNonPlayer(Location loc) {
        if (isPlayer() || entity.isValid()) return false;

        LivingEntity oldEntity = entity;
        makeEntityFromEntity(oldEntity, loc, oldEntity.getType());

        oldEntity.remove();

        return true;
    }


    @Override
    public Location getLocation() {
        return entity.getLocation();
    }


    @Override
    public MapPoint getMapLocation() {
        return getMap().getMapPoint(getLocation());
    }


    @Override
    public void launchVictoryFirework() {
        Location loc = getLocation();

        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

        Color color = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        Color fade = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));

        firework.setVelocity(new Vector(0, 0.5f, 0));
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(
                FireworkEffect.builder().with(FireworkEffect.Type.BURST).trail(true).withColor(color).withFade(fade)
                        .build());
        firework.setFireworkMeta(meta);
    }


    @Override
    public void becomePlayer(Player player) {
        if (isPlayer()) return;

        revalidateTask.cancel();

        playerState.capture(entity);

        player.teleport(entity.getLocation());
        playerState.restore(player);
        playerState.setPlaceholder(null);

        entity.remove();
        entity = player;

        fixCloakedUsers();

        scoreboardDisplay = new ScoreboardDisplay(this, player);
        updateScoreboard();

        if (disguise != null) disguise(disguise);
    }


    @Override
    public boolean isCloaked() {
        return cloaked;
    }


    @Override
    public void cloak() {
        cloaked = true;

        for (BaseUser u : gameGroup.getUsers()) {
            if (this == u) continue;

            if (u.showCloakedUsers()) continue;

            u.hidePlayer(this);
        }
    }

    @Override
    public void updateScoreboard() {
        if (scoreboardDisplay == null || scoreboardHandler == null) return;

        scoreboardHandler.updateScoreboard(this, scoreboardDisplay);
    }

    @Override
    public boolean showCloakedUsers() {
        return showCloakedPlayers;
    }

    @Override
    public void removeNonPlayer() {
        if (isPlayer()) return;
        entity.remove();

        revalidateTask.cancel();

        gameGroup.userEvent(new UserQuitEvent(this, UserQuitEvent.QuitReason.NON_PLAYER_REMOVED));
    }

    @Override
    public void disguise(EntityType type) {
        disguise(new Disguise(type));
    }

    @Override
    public void disguise(Disguise disguise) {
        this.disguise = disguise;
        gameGroup.getGame().disguiseUser(this, disguise);
    }

    @Override
    public void unDisguise() {
        this.disguise = null;
        gameGroup.getGame().unDisguiseUser(this);
    }

    @Override
    public void setAllowFlight(boolean allowFlight) {
        if (isPlayer()) getPlayer().setAllowFlight(allowFlight);
        else playerState.setAllowFlight(allowFlight);

        if (!allowFlight) {
            setFlying(false);
        }
    }

    @Override
    public void setFlySpeed(double flySpeed) {
        if (isPlayer()) getPlayer().setFlySpeed((float) flySpeed);
        else playerState.setFlySpeed((float) flySpeed);
    }

    @Override
    public void resetUserStats(boolean removePotionEffects) {
        Config defaultStats = getSharedObjectOrEmpty("user").getConfigOrEmpty("default_stats");

        setMaxHealth(defaultStats.getDouble("max_health", 10) * 2);
        setHealth(defaultStats.getDouble("health", 10) * 2);
        setFoodLevel((int) (defaultStats.getDouble("food_level", 10) * 2));
        setSaturation(defaultStats.getDouble("saturation", 5.0));
        setFlySpeed(defaultStats.getDouble("fly_speed", 0.1));
        setWalkSpeed(defaultStats.getDouble("walk_speed", 0.2));
        setHitDelayTicks((int) (defaultStats.getDouble("max_hit_delay", 1) * 20));

        if (removePotionEffects) removePotionEffects();
    }

    @Override
    public int getHitDelayTicks() {
        return entity.getMaximumNoDamageTicks();
    }

    @Override
    public void setHitDelayTicks(int ticks) {
        entity.setMaximumNoDamageTicks(ticks);
    }

    @Override
    public void setMaxHealth(double maxHealth) {
        entity.setMaxHealth(maxHealth);
    }

    @Override
    public void setFoodLevel(int foodLevel) {
        if (isPlayer()) getPlayer().setFoodLevel(foodLevel);
        else playerState.setFoodLevel(foodLevel);
    }

    @Override
    public void setSaturation(double saturation) {
        if (isPlayer()) getPlayer().setSaturation((float) saturation);
        else playerState.setSaturation((float) saturation);
    }

    @Override
    public void setWalkSpeed(double walkSpeed) {
        if (isPlayer()) getPlayer().setWalkSpeed((float) walkSpeed);
        else playerState.setWalkSpeed((float) walkSpeed);
    }

    @Override
    public void removePotionEffects() {
        List<PotionEffect> effects = new ArrayList<>(entity.getActivePotionEffects());

        for (PotionEffect effect : effects) {
            entity.removePotionEffect(effect.getType());
        }

        entity.setFireTicks(0);
    }

    @Override
    public void setCollidesWithEntities(boolean collides) {
        if (!isPlayer()) return;

        getPlayer().spigot().setCollidesWithEntities(collides);
    }

    @Override
    public void decloak() {
        cloaked = false;

        for (BaseUser u : gameGroup.getUsers()) {
            if (this == u) continue;

            u.showPlayer(this);
        }
    }

    @Override
    public String getTeamName() {
        return team != null ? team.getName() : null;
    }

    @Override
    public BaseTeam getTeam() {
        return team;
    }

    @Override
    public void setTeam(Team team) {
        if (team != null && !(team instanceof BaseTeam)) {
            throw new UnsupportedOperationException("Only supports BaseTeam");
        }

        if (team == this.team) return;

        Team oldTeam = this.team;
        Team newTeam = this.team = (BaseTeam) team;

        if (oldTeam != null) oldTeam.removeUser(this);
        if (newTeam != null) newTeam.addUser(this);

        UserChangeTeamEvent event = new UserChangeTeamEvent(this, oldTeam, newTeam);
        gameGroup.userEvent(event);
    }

    @Override
    public Kit getKit() {
        return kit;
    }

    @Override
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

    @Override
    public TeamIdentifier getTeamIdentifier() {
        return team != null ? team.getTeamIdentifier() : null;
    }

    @Override
    public BaseGameGroup getGameGroup() {
        return gameGroup;
    }

    @Override
    public boolean isInGame() {
        return isInGame;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setInGame(boolean inGame) {
        isInGame = inGame;

        inGameTaskList.cancelAllTasks();

        if (!inGame) {
            userVariableHandler.clearUpgrades();
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

    @Override
    public void setSpectator(boolean spectator) {
        if (spectator == this.spectator) {
            System.out.println(
                    "setSpectator() called on user " + name + " with same spec state as current: " + spectator);
            return;
        }


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
    public void setScoreboardHandler(ScoreboardHandler scoreboardHandler) {
        this.scoreboardHandler = scoreboardHandler;
        if (scoreboardDisplay != null) {
            if (scoreboardHandler == null) scoreboardDisplay.remove();
            else scoreboardHandler.setupScoreboard(this, scoreboardDisplay);
        }
    }

    @Override
    public GameMode getGameMode() {
        return isPlayer() ? getPlayer().getGameMode() : playerState.getGameMode();
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        if (isPlayer()) getPlayer().setGameMode(gameMode);
        else playerState.setGameMode(gameMode);
    }

    @Override
    public int getFireTicks() {
        return entity.getFireTicks();
    }

    @Override
    public void setFireTicks(User fireAttacker, int fireTicks) {
        this.fireAttacker.setAttacker(fireAttacker, fireTicks);
        entity.setFireTicks(fireTicks);
    }

    @Override
    public void setWitherTicks(User witherAttacker, int witherTicks) {
        setWitherTicks(witherAttacker, witherTicks, 0);
    }

    /**
     * @param witherAmplifier The amplifier for the effect. Level n is amplifier n-1.
     */
    @Override
    public void setWitherTicks(User witherAttacker, int witherTicks, int witherAmplifier) {
        this.witherAttacker.setAttacker(witherAttacker, witherTicks);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherAmplifier, witherAmplifier));
    }

    @Override
    public boolean startCoolDown(String ability, double seconds, String coolDownLocale) {
        return cooldownHandler.startCoolDown(ability, seconds, coolDownLocale);
    }

    @Override
    public void stopCoolDown(String ability, String stopLocale) {
        cooldownHandler.stopCoolDown(ability, stopLocale);
    }

    @Override
    public void playSound(Location location, SoundEffect sound) {
        if (!isPlayer()) return;

        getPlayer().playSound(location, sound.getSound(), sound.getVolume(), sound.getPitch());
    }

    @Override
    public boolean isCoolingDown(String ability) {
        return cooldownHandler.isCoolingDown(ability);
    }

    @Override
    public double getCooldownSeconds(String ability) {
        return cooldownHandler.getCooldownSeconds(ability);
    }

    @Override
    public double getUserVariable(String upgrade) {
        return userVariableHandler.getVariable(upgrade);
    }

    @Override
    public void setUserVariable(String upgrade, double level) {
        userVariableHandler.setVariable(upgrade, level);
    }

    @Override
    public ItemStack createCustomItemForUser(CustomItem item) {
        return item.createWithVariables(gameGroup, userVariableHandler);
    }

    @Override
    public <T extends Projectile> T launchProjectile(Class<? extends T> projectileClass) {
        return entity.launchProjectile(projectileClass);
    }

    @Override
    public UserVariableHandler getUserVariables() {
        return userVariableHandler;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean teleport(MapPoint point) {
        Location target = new Location(getMap().getWorld(point.getWorld()),
                                       point.getX(), point.getY(), point.getZ(),
                                       Float.isFinite(point.getYaw()) ? point.getYaw() : getLocation().getYaw(),
                                       Float.isFinite(point.getPitch()) ? point.getPitch() : getLocation().getPitch());

        return teleport(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean teleport(Location location) {
        if (getMap() != null && !getMap().getDefaultWorld().getName().equals(location.getWorld().getName())) {
            try {
                String message = "tried to teleport user " + name + " to another Bukkit world: game_map=" +
                                 getMap().getDefaultWorld().getName() + ", world=" + location.getWorld().getName();

                throw new RuntimeException(message);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        UserTeleportEvent event = new UserTeleportEvent(this, getLocation(), location);

        gameGroup.userEvent(event);

        if (event.isCancelled()) return false;

        //Prevent death due to fall damage after teleporting
        getEntity().setFallDistance(0);

        //Try and fix cloak glitches in showdown
        if (isPlayer()) {
            fixCloakedUsers();
        }

        boolean success = entity.teleport(event.getTo());

        if (!success) {
            System.out.println("Failed to teleport user: " + getName());
        }

        return revalidateNonPlayer(event.getTo()) || success;
    }

    @Override
    public boolean isViewingClickableInventory() {
        return openInventory != null;
    }

    @Override
    public ClickableInventory getClickableInventory() {
        return openInventory;
    }

    @Override
    public Inventory getEnderInventory() {
        if (getPlayer() == null) return null;

        return getPlayer().getEnderChest();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void showInventory(ClickableInventory inventory, Location inventoryTether) {
        doInFuture(task -> {
            if (!isPlayer()) return;

            Inventory old = null;
            if (this.openInventory != null) {
                old = getPlayer().getOpenInventory().getTopInventory();
            }

            this.openInventory = inventory;
            this.inventoryTether = inventoryTether != null ? getMap().getMapPoint(inventoryTether) : null;

            Inventory newInventory = inventory.createInventory(this, old);

            if (newInventory == old) {
                return;
            }

            if (old == null) {
                getPlayer().openInventory(newInventory);
            } else {
                doInFuture(task1 -> {
                    getPlayer().openInventory(newInventory);
                });
            }
        });
    }

    @Override
    public void showInventory(Inventory inventory, Location inventoryTether) {
        doInFuture(task -> {
            if (!isPlayer()) return;

            this.inventoryTether = inventoryTether != null ? getMap().getMapPoint(inventoryTether) : null;

            getPlayer().openInventory(inventory);
        });
    }

    @Override
    public void redoInventory() {
        if (this.openInventory == null || !isPlayer()) return;

        Inventory viewing = getPlayer().getOpenInventory().getTopInventory();
        viewing.clear();
        this.openInventory.populateInventory(viewing, this);
    }

    @Override
    public Location getInventoryTether() {
        return gameGroup.getCurrentMap().getLocation(inventoryTether);
    }

    @Override
    public void setXpLevel(int level) {
        if (isPlayer()) getPlayer().setLevel(level);
        else playerState.setLevel(level);
    }

    @Override
    public float getExp() {
        return isPlayer() ? getPlayer().getExp() : playerState.getExp();
    }

    @Override
    public void setExp(double exp) {
        if (isPlayer()) getPlayer().setExp((float) exp);
        else playerState.setExp((float) exp);
    }

    @Override
    public void bindTaskToInGame(GameTask task) {
        inGameTaskList.addTask(task);
    }

    @Override
    public String getTabListName() {
        if (!isPlayer()) return playerState.getTabListName();
        else return getPlayer().getPlayerListName();
    }

    @Override
    public void setTabListName(String tabListName) {
        if (!isPlayer()) playerState.setTabListName(tabListName);
        else getPlayer().setPlayerListName(tabListName);
    }

    @Override
    public Block rayTraceBlocks(int maxDistance) {
        return entity.getTargetBlock(SEE_THROUGH, maxDistance);
    }

    @Override
    public Inventory createInventory(int size, String title) {
        size = ((size + 8) / 9) * 9;

        return Bukkit.createInventory((InventoryHolder) entity, size, title);
    }

    @Override
    public void closeInventory() {
        doInFuture(task -> {
            if (!isPlayer()) return;

            openInventory = null;
            inventoryTether = null;
            getPlayer().closeInventory();
        });
    }

    @Override
    public void giveColoredArmor(Color color, boolean unbreakable) {
        giveColoredArmor(color, unbreakable, true, true, true, true);
    }

    @Override
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

    @Override
    public PlayerInventory getInventory() {
        return isPlayer() ? getPlayer().getInventory() : playerState.getInventory();
    }

    @Override
    public void clearArmor(boolean helmet, boolean chestplate, boolean leggings, boolean boots) {
        EntityEquipment equipment = entity.getEquipment();

        if (helmet) equipment.setHelmet(null);
        if (chestplate) equipment.setChestplate(null);
        if (leggings) equipment.setLeggings(null);
        if (boots) equipment.setBoots(null);
    }

    @Override
    public void clearArmor() {
        clearArmor(true, true, true, true);
    }

    @Override
    public String getKitName() {
        return kit != null ? kit.getName() : null;
    }

    @Override
    public Location getCompassTarget() {
        if (!isPlayer()) return null;
        return getPlayer().getCompassTarget();
    }

    @Override
    public void setCompassTarget(Location compassTarget) {
        if (isPlayer()) getPlayer().setCompassTarget(compassTarget);
    }

    @Override
    public void addPotionEffect(PotionEffect effect) {
        entity.addPotionEffect(effect);
    }

    @Override
    public void addPotionEffect(PotionEffect effect, boolean force) {
        entity.addPotionEffect(effect, force);
    }

    @Override
    public User getFireAttacker() {
        return fireAttacker.getAttacker();
    }

    @Override
    public User getWitherAttacker() {
        return witherAttacker.getAttacker();
    }

    @Override
    public User getLastAttacker() {
        return lastAttacker.getAttacker();
    }

    @Override
    public void setLastAttacker(User lastAttacker) {
        this.lastAttacker.setAttacker(lastAttacker);
    }

    @Override
    public boolean isInsideVehicle() {
        return entity.isInsideVehicle();
    }

    @Override
    public Entity getVehicle() {
        return entity.getVehicle();
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public TNTPrimed createExplosion(Location loc, float power, boolean fire, int fuseTicks) {
        TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);

        makeEntityRepresentUser(tnt);

        tnt.setIsIncendiary(fire);
        tnt.setYield(power);

        tnt.setFuseTicks(fuseTicks);

        return tnt;
    }

    @Override
    public GameMap getMap() {
        return gameGroup.getCurrentMap();
    }

    @Override
    public void makeEntityRepresentUser(Entity entity) {
        gameGroup.getGame().makeEntityRepresentUser(this, entity);
    }

    @Override
    public boolean isFlying() {
        return isPlayer() && getPlayer().isFlying();
    }

    @Override
    public void setFlying(boolean flying) {
        if (!isPlayer()) return;
        getPlayer().setFlying(flying);
    }

    @Override
    public void showAboveHotbarLocale(String locale, Object... args) {
        showAboveHotbarMessage(gameGroup.getLocale(locale, args));
    }

    @Override
    public void showAboveHotbarMessage(String message) {
        if (!isPlayer()) return;

        WrapperPlayServerChat chatPacket = new WrapperPlayServerChat();

        chatPacket.setMessage(WrappedChatComponent.fromText(message));
        chatPacket.setChatType(EnumWrappers.ChatType.GAME_INFO);

        chatPacket.sendPacket(getPlayer());
    }

    @Override
    public void showTitle(String title) {
        showTitle(title, null);
    }

    @Override
    public void showTitle(String title, String subTitle) {
        showTitle(title, subTitle, 20, 60, 20);
    }

    @Override
    public void showTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (!isPlayer()) return;

        CommandSender console = Bukkit.getConsoleSender();

        Bukkit.dispatchCommand(console, "title " + getName() + " reset");
        Bukkit.dispatchCommand(console, "title " + getName() + " times " + fadeIn + " " + stay + " " + fadeOut);

        if (subTitle != null) {
            Config subConfig = (new ConfigMessageFactory(subTitle)).newBuilder().getResult();
            String subJson = JsonConfigIO.dumpConfig(subConfig);

            Bukkit.dispatchCommand(console, "title " + getName() + " subtitle " + subJson + "");
        }

        Config titleConfig = (new ConfigMessageFactory(title)).newBuilder().getResult();
        String titleJson = JsonConfigIO.dumpConfig(titleConfig);

        Bukkit.dispatchCommand(console, "title " + getName() + " title " + titleJson + "");
    }

    @Override
    public double getHealth() {
        return entity.getHealth();
    }

    @Override
    public void setHealth(double health) {
        entity.setHealth(health);
    }

    @Override
    public boolean hasPotionEffect(PotionEffectType type) {
        return entity.hasPotionEffect(type);
    }

    @Override
    public void setFallDistance(double fallDistance) {
        entity.setFallDistance((float) fallDistance);
    }

    @Override
    public boolean isOnGround() {
        return entity.isOnGround();
    }

    @Override
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormattedName() {
        String displayName = getDisplayName();
        return displayName != null ? displayName : getName();
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

    @Override
    public void setVelocity(Vector velocity) {
        entity.setVelocity(velocity);
    }


    @Override
    public Vector getVelocity() {
        return entity.getVelocity();
    }


    @Override
    public void removeFromGameGroup() {
        setScoreboardHandler(null);

        setInGame(false);

        setTabListName(getName());
        setDisplayName(getName());

        for (UserMetadata metadata : metadataMap.values()) {
            metadata.removed();
        }

        metadataMap.clear();

        cancelAllTasks();
    }


    @Override
    public EntityType getVisibleEntityType() {
        if (disguise != null) return disguise.getEntityType();
        else return entity.getType();
    }

    @Override
    public Disguise getDisguise() {
        return disguise;
    }


    @Override
    public String getDisplayName() {
        return isPlayer() ? getPlayer().getDisplayName() : entity.getCustomName();
    }


    @Override
    public void setDisplayName(String displayName) {
        if (!isPlayer()) entity.setCustomName(displayName);
        else getPlayer().setDisplayName(displayName);

        if (disguise != null && disguise.isShowUserNameAboveEntity()) disguise(disguise);
    }


    @Override
    public Collection<CustomListener> getListeners() {
        return listeners;
    }

    @Override
    public Account getEconomyAccount() {
        return economyAccount;
    }


    @Override
    public void sendLocale(String locale, Object... args) {
        sendMessage(gameGroup.getLocale(locale, args));
    }


    @Override
    public void sendMessage(String message) {
        sendMessageNoPrefix(gameGroup.getMessagePrefix() + message);
    }

    @Override
    public void sendMessageNoPrefix(Config message) {
        Player player = getPlayer();
        if (player == null) return;

        PlayerMessageSender.sendMessage(message, Collections.singleton(player));
    }

    @Override
    public void sendMessageNoPrefix(String message) {
        entity.sendMessage(message);
    }

    @Override
    public String getMessagePrefix() {
        return gameGroup.getMessagePrefix();
    }

    @Override
    public void sendLocaleNoPrefix(String locale, Object... args) {
        sendMessageNoPrefix(gameGroup.getLocale(locale, args));
    }


    @Override
    public LanguageLookup getLanguageLookup() {
        return gameGroup.getLanguageLookup();
    }

}
