package com.ithinkrok.minigames.hub.item;

import com.ithinkrok.minigames.api.database.Database;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.api.event.game.MapChangedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.CalculateItemForUserEvent;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.metadata.UserMetadata;
import com.ithinkrok.minigames.api.protocol.ClientMinigamesRequestProtocol;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.api.util.MinigamesConfigs;
import com.ithinkrok.minigames.hub.sign.GameChooseSign;
import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 23/02/16.
 */
public class GameChooseMenu implements CustomListener {

    private final Map<String, Config> gameGroups = new HashMap<>();

    private ItemStack directJoinOff;
    private ItemStack directJoinOn;

    private String inventoryTitleLocale;
    private String transferLocale;
    private String directJoinOnLocale;
    private String directJoinOffLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, CustomItem> event) {
        Config config = event.getConfigOrEmpty();

        Config gameGroupsConfig = config.getConfigOrEmpty("gamegroups");

        for(String gameGroupType : gameGroupsConfig.getKeys(false)) {
            gameGroups.put(gameGroupType, gameGroupsConfig.getConfigOrNull(gameGroupType));
        }

        directJoinOn = MinigamesConfigs.getItemStack(config, "direct_join_enabled_item");
        directJoinOff = MinigamesConfigs.getItemStack(config, "direct_join_disabled_item");

        inventoryTitleLocale = config.getString("inventory_title_locale", "game_chooser.title");
        transferLocale = config.getString("transfer_locale", "game_chooser.transfer");
        directJoinOnLocale = config.getString("direct_join_on_locale", "game_chooser.direct_join.enable");
        directJoinOffLocale = config.getString("direct_join_off_locale", "game_chooser.direct_join.disable");
    }

    @CustomEventHandler
    public void onRightClick(UserInteractEvent event) {
        String inventoryTitle = event.getUserGameGroup().getLocale(inventoryTitleLocale);
        ClickableInventory inventory = new ClickableInventory(inventoryTitle);

        GameChooseMetadata metadata = GameChooseMetadata.getOrCreate(event.getUser());

        for(Map.Entry<String, Config> entry : gameGroups.entrySet()) {
            ItemStack display = MinigamesConfigs.getItemStack(entry.getValue(), "item");

            ClickableItem item = new ClickableItem(display, -1) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    if(metadata.isDirectJoin()) {
                        event.getUser().sendLocale(transferLocale, entry.getKey());
                        ClientMinigamesRequestProtocol requestProtocol = event.getUserGameGroup().getRequestProtocol();
                        requestProtocol.sendJoinGameGroupPacket(event.getUser().getUuid(), entry.getKey(), null);
                    } else {
                        Location loc = BukkitConfigUtils.getLocation(entry.getValue(), event.getUser().getLocation()
                                .getWorld(), "teleport");
                        event.getUser().teleport(loc);
                    }

                    event.getUser().closeInventory();
                }
            };

            inventory.addItem(item);
        }

        ClickableItem directJoin = new ClickableItem(directJoinOff, 26) {

            @Override
            public void onCalculateItem(CalculateItemForUserEvent event) {
                ItemStack item;

                if(metadata.isDirectJoin()) {
                    item = directJoinOn;
                } else {
                    item = directJoinOff;
                }

                item = InventoryUtils.addIdentifier(item, this.getIdentifier());
                event.setDisplay(item);
            }

            @Override
            public void onClick(UserClickItemEvent event) {
                metadata.setDirectJoin(!metadata.isDirectJoin());
                event.getUser().redoInventory();

                if(metadata.isDirectJoin()) {
                    event.getUser().sendLocale(directJoinOnLocale);
                } else {
                    event.getUser().sendLocale(directJoinOffLocale);
                }
            }
        };

        inventory.addItem(directJoin);

        event.getUser().showInventory(inventory, null);
    }

    private static class GameChooseMetadata extends UserMetadata {

        private final User user;

        private boolean directJoin = false;

        public GameChooseMetadata(User user) {
            this.user = user;

            Database database = user.getGameGroup().getDatabase();
            database.getBooleanUserValue(user, "mg_direct_join", aBoolean -> {
                directJoin = aBoolean;
                user.doInFuture(task -> {
                    user.redoInventory();
                }, 2);
            }, false);
        }

        public boolean isDirectJoin() {
            return directJoin;
        }

        public void setDirectJoin(boolean directJoin) {
            this.directJoin = directJoin;

            Database database = user.getGameGroup().getDatabase();
            database.setBooleanUserValue(user, "mg_direct_join", directJoin);
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
            return false;
        }

        public static GameChooseMetadata getOrCreate(User user) {
            GameChooseMetadata metadata = user.getMetadata(GameChooseMetadata.class);

            if(metadata == null) {
                metadata = new GameChooseMetadata(user);
                user.setMetadata(metadata);
            }

            return metadata;
        }
    }
}
