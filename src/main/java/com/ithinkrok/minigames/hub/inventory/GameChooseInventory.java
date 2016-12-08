package com.ithinkrok.minigames.hub.inventory;

import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by paul on 08/12/16.
 */
public class GameChooseInventory extends ClickableInventory {

    public static final String ID = "GameChooseInventory";

    public GameChooseInventory(String title, User user, String gameGroupType, List<String> gameGroupParams) {
        super(title, ID);

        Collection<GameGroupInfo> allGGsCollection =
                user.getGameGroup().getControllerInfo().getGameGroups(gameGroupType, gameGroupParams);


        List<GameGroupInfo> orderedGGs = new ArrayList<>(allGGsCollection);

        //Order so that GGs that are accepting show up first
        orderedGGs.sort((o1, o2) -> {
            if (o1.isAcceptingPlayers() && !o2.isAcceptingPlayers()) return 1;
            else if (o2.isAcceptingPlayers() && !o1.isAcceptingPlayers()) return -1;
            return o1.getName().compareTo(o2.getName());
        });

        for (GameGroupInfo gameGroup : orderedGGs) {

            //Skip hubs unless we are specifically for them
            if((gameGroupType == null || gameGroupType.isEmpty()) && "hub".equals(gameGroup.getType())) {
                continue;
            }

            Material mat = gameGroup.isAcceptingPlayers() ? Material.GOLD_BLOCK : Material.IRON_BLOCK;

            List<String> lore = new ArrayList<>();

            if (gameGroup.isAcceptingPlayers()) {
                lore.add(ChatColor.GREEN + "Join Lobby");
            } else {
                lore.add(ChatColor.RED + "Spectate Game");
            }

            lore.add(ChatColor.WHITE + gameGroup.getMotd());

            lore.add(ChatColor.RED + "Type: " + ChatColor.GRAY + gameGroupType);
            lore.add(ChatColor.RED + "Players: " + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY +
                             gameGroup.getPlayerCount() + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY +
                             gameGroup.getMaxPlayerCount() + ChatColor.DARK_GRAY +
                             "]");

            ItemStack item = InventoryUtils
                    .createItemWithNameAndLore(mat, 1, 0, gameGroup.getName(), lore.toArray(new String[lore.size()]));

            ClickableItem clickableItem = new ClickableItem(item) {
                @Override
                public void onClick(UserClickItemEvent event) {
                    user.sendMessage("Sending you to gamegroup: " + gameGroup.getName());

                    user.getGameGroup().getRequestProtocol()
                            .sendJoinGameGroupPacket(user.getUuid(), gameGroup.getType(), gameGroup.getName(),
                                                     gameGroup.getParams());
                }
            };

            addItem(clickableItem);
        }
    }
}
