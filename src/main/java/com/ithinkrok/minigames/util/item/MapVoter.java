package com.ithinkrok.minigames.util.item;

import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.map.GameMapInfo;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.util.metadata.MapVote;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 04/01/16.
 */
public class MapVoter implements CustomListener {

    private List<String> votable;
    private Material mapMaterial;
    private String voteLocale, transferLocale, alreadyLocale;
    private String randomMapName;
    private String randomMapDescriptionLocale;

    @CustomEventHandler
    public void onListenerEnabled(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfig();

        votable = config.getStringList("votable_maps");
        voteLocale = config.getString("vote_locale", "map_voter.vote.player");
        transferLocale = config.getString("transfer_locale", "map_voter.vote.transfer");
        alreadyLocale = config.getString("already_voted_locale", "map_voter.vote.already_voted");

        randomMapName = config.getString("random_map", "random");
        randomMapDescriptionLocale = config.getString("random_map_desc_locale", "map_voter.random.desc");

        mapMaterial = Material.matchMaterial(config.getString("map_material", "EMPTY_MAP"));
    }

    @SuppressWarnings("unchecked")
    @CustomEventHandler
    public void onInteract(UserInteractEvent event) {
        if (event.getInteractType() != UserInteractEvent.InteractType.RIGHT_CLICK) return;
        event.setCancelled(true);

        ClickableInventory inventory = new ClickableInventory("Map Voter");

        for (String mapName : votable) {
            String description;
            List<String> credit = Collections.emptyList();

            if(mapName.equals(randomMapName)){
                description = event.getGameGroup().getLocale(randomMapDescriptionLocale);
            } else {
                GameMapInfo mapInfo = event.getGameGroup().getMap(mapName);
                if(mapInfo != null) {
                    description = mapInfo.getDescription();
                    credit = mapInfo.getCredit();
                }
                else description = "Invalid map";
            }

            ItemStack display =
                    InventoryUtils.createItemWithNameAndLore(mapMaterial, 1, 0, mapName, description);

            display = InventoryUtils.addLore(display, "");
            display = InventoryUtils.addLore(display, credit);


            ClickableItem item = new ClickableItem(display, -1) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    event.getUser().closeInventory();
                    MapVote oldVote = event.getUser().getMetadata(MapVote.class);
                    if (oldVote != null) {
                        if (mapName.equals(oldVote.getMapVote())) {
                            event.getUser().sendLocale(alreadyLocale, mapName);
                            return;
                        } else event.getUser().getGameGroup()
                                .sendLocale(transferLocale, event.getUser().getFormattedName(), oldVote.getMapVote(),
                                        mapName);
                    } else {

                        event.getUser().getGameGroup()
                                .sendLocale(voteLocale, event.getUser().getFormattedName(), mapName);
                    }
                    event.getUser().setMetadata(new MapVote(event.getUser(), mapName));

                    for (User user : event.getGameGroup().getUsers()) {
                        user.updateScoreboard();
                    }
                }
            };

            inventory.addItem(item);
        }

        event.getUser().showInventory(inventory, null);
    }
}
