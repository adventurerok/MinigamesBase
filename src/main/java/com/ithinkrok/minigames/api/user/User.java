package com.ithinkrok.minigames.api.user;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.Nameable;
import com.ithinkrok.minigames.api.SharedObjectAccessor;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.map.GameMap;
import com.ithinkrok.minigames.api.map.MapPoint;
import com.ithinkrok.minigames.api.metadata.MetadataHolder;
import com.ithinkrok.minigames.api.metadata.UserMetadata;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.task.TaskScheduler;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardHandler;
import com.ithinkrok.minigames.api.util.SoundEffect;
import com.ithinkrok.minigames.api.util.disguise.Disguise;
import com.ithinkrok.msm.common.economy.Account;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by paul on 20/02/16.
 */
public interface User
        extends MinigamesCommandSender, TaskScheduler, UserResolver, MetadataHolder<UserMetadata>, SharedObjectAccessor,
        Nameable {
    void fixCloakedUsers();

    boolean isPlayer();

    Player getPlayer();

    ClickableInventory getOpenInventory();

    void setShowCloakedUsers(boolean showCloakedPlayers);

    void becomeEntity(EntityType entityType);

    Location getLocation();

    MapPoint getMapLocation();

    void launchVictoryFirework();

    void becomePlayer(Player player);

    boolean isCloaked();

    void cloak();

    void updateScoreboard();

    boolean showCloakedUsers();

    void removeNonPlayer();

    void disguise(EntityType type);

    void disguise(Disguise disguise);

    Disguise getDisguise();

    void unDisguise();

    void setAllowFlight(boolean allowFlight);

    void setFlySpeed(double flySpeed);

    void resetUserStats(boolean removePotionEffects);

    int getHitDelayTicks();

    void setHitDelayTicks(int ticks);

    void setMaxHealth(double maxHealth);

    void setFoodLevel(int foodLevel);

    void setSaturation(double saturation);

    void setWalkSpeed(double walkSpeed);

    void removePotionEffects();

    void setCollidesWithEntities(boolean collides);

    void decloak();

    String getTeamName();

    Team getTeam();

    void setTeam(Team team);

    Kit getKit();

    void setKit(Kit kit);

    TeamIdentifier getTeamIdentifier();

    GameGroup getGameGroup();

    boolean isInGame();

    @SuppressWarnings("unchecked")
    void setInGame(boolean inGame);

    void setSpectator(boolean spectator);

    void setScoreboardHandler(ScoreboardHandler scoreboardHandler);

    GameMode getGameMode();

    void setGameMode(GameMode gameMode);

    int getFireTicks();

    void setFireTicks(User fireAttacker, int fireTicks);

    void setWitherTicks(User witherAttacker, int witherTicks);

    void setWitherTicks(User witherAttacker, int witherTicks, int witherAmplifier);

    boolean startCoolDown(String ability, double seconds, String coolDownLocale);

    void stopCoolDown(String ability, String stopLocale);

    void playSound(Location location, SoundEffect sound);

    boolean isCoolingDown(String ability);

    double getCooldownSeconds(String ability);

    double getUserVariable(String upgrade);

    void setUserVariable(String upgrade, double level);

    ItemStack createCustomItemForUser(CustomItem item);

    <T extends Projectile> T launchProjectile(Class<? extends T> projectileClass);

    UserVariableHandler getUserVariables();

    UUID getUuid();

    boolean teleport(Location location);

    boolean teleport(MapPoint point);

    boolean isViewingClickableInventory();

    ClickableInventory getClickableInventory();

    Inventory getEnderInventory();

    void showInventory(ClickableInventory inventory, Location inventoryTether);

    void showInventory(Inventory inventory, Location inventoryTether);

    void redoInventory();

    Location getInventoryTether();

    void setXpLevel(int level);

    float getExp();

    void setExp(double exp);

    void bindTaskToInGame(GameTask task);

    String getTabListName();

    void setTabListName(String tabListName);

    Block rayTraceBlocks(int maxDistance);

    Inventory createInventory(int size, String title);

    void closeInventory();

    void giveColoredArmor(Color color, boolean unbreakable);

    void giveColoredArmor(Color color, boolean unbreakable, boolean helmet, boolean chestplate, boolean leggings,
                          boolean boots);

    PlayerInventory getInventory();

    void clearArmor(boolean helmet, boolean chestplate, boolean leggings, boolean boots);

    void clearArmor();

    String getKitName();

    Location getCompassTarget();

    void setCompassTarget(Location compassTarget);

    void addPotionEffect(PotionEffect effect);

    void addPotionEffect(PotionEffect effect, boolean force);

    User getFireAttacker();

    User getWitherAttacker();

    User getLastAttacker();

    void setLastAttacker(User lastAttacker);

    boolean isInsideVehicle();

    Entity getVehicle();

    LivingEntity getEntity();

    TNTPrimed createExplosion(Location loc, float power, boolean fire, int fuseTicks);

    GameMap getMap();

    void makeEntityRepresentUser(Entity entity);

    boolean isFlying();

    void setFlying(boolean flying);

    void showAboveHotbarLocale(String locale, Object... args);

    void showAboveHotbarMessage(String message);

    void showTitle(String title);

    void showTitle(String title, String subTitle);

    void showTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut);

    double getHealth();

    void setHealth(double health);

    boolean hasPotionEffect(PotionEffectType type);

    void setFallDistance(double fallDistance);

    boolean isOnGround();

    boolean unstuck(int maxRadius);

    void setVelocity(Vector velocity);

    Vector getVelocity();

    void removeFromGameGroup();

    EntityType getVisibleEntityType();

    String getDisplayName();

    void setDisplayName(String displayName);

    Collection<CustomListener> getListeners();

    Account getEconomyAccount();

    Config getGlobalConfig();

    Config getMinigameSpecificConfig();
}
