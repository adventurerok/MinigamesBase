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
import com.ithinkrok.msm.bukkit.util.BukkitConfigUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 23/02/16.
 */
public class GameChooseMenu implements CustomListener {

    private final List<Config> gameGroups = new ArrayList<>();

    private ItemStack directJoinOff;
    private ItemStack directJoinOn;

    private String inventoryTitleLocale;
    private String transferLocale;
    private String directJoinOnLocale;
    private String directJoinOffLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, CustomItem> event) {
        Config config = event.getConfigOrEmpty();

        gameGroups.addAll(config.getConfigList("gamegroups"));

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

        for (Config config : gameGroups) {
            ItemStack display = MinigamesConfigs.getItemStack(config, "item");

            ClickableItem item = new GameChooseItem(display, config, metadata);

            inventory.addItem(item);
        }

        ClickableItem directJoin = new DirectJoinItem(metadata);

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

        public static GameChooseMetadata getOrCreate(User user) {
            GameChooseMetadata metadata = user.getMetadata(GameChooseMetadata.class);

            if (metadata == null) {
                metadata = new GameChooseMetadata(user);
                user.setMetadata(metadata);
            }

            return metadata;
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
    }

    private class GameChooseItem extends ClickableItem {

        private final Config config;
        private final GameChooseMetadata metadata;
        private final String ggType;
        private final List<String> ggParams;

        private final Config query;

        private final boolean allowTeleport;
        private final boolean allowDirectJoin;

        public GameChooseItem(ItemStack display, Config config, GameChooseMetadata metadata) {
            super(display, config.getInt("slot", -1));
            this.config = config;
            this.metadata = metadata;

            ggType = config.getString("type");
            ggParams = config.getStringList("params");

            query = config.getConfigOrNull("query");

            allowDirectJoin = config.getBoolean("direct_join_allow");
            allowTeleport = config.getBoolean("teleport_location_allow");
        }

        @Override
        public void onClick(UserClickItemEvent event) {
            if ((metadata.isDirectJoin() && allowDirectJoin) || !allowTeleport) {
                event.getUser().sendLocale(transferLocale, ggType);
                ClientMinigamesRequestProtocol requestProtocol = event.getUserGameGroup().getRequestProtocol();
                requestProtocol.sendJoinGameGroupPacket(event.getUser().getUuid(), ggType, null, ggParams, query);
            } else {
                Location loc =
                        BukkitConfigUtils.getLocation(config, event.getUser().getLocation().getWorld(), "teleport");
                event.getUser().teleport(loc);
            }

            event.getUser().closeInventory();
        }
    }

    private class DirectJoinItem extends ClickableItem {

        private final GameChooseMetadata metadata;

        public DirectJoinItem(GameChooseMetadata metadata) {
            super(GameChooseMenu.this.directJoinOff, 26);
            this.metadata = metadata;
        }

        @Override
        public void onCalculateItem(CalculateItemForUserEvent event) {
            ItemStack item;

            if (metadata.isDirectJoin()) {
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

            if (metadata.isDirectJoin()) {
                event.getUser().sendLocale(directJoinOnLocale);
            } else {
                event.getUser().sendLocale(directJoinOffLocale);
            }
        }
    }
}
