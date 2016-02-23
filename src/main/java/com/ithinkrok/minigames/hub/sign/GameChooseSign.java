package com.ithinkrok.minigames.hub.sign;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by paul on 20/02/16.
 */
public class GameChooseSign extends HubSign {

    private final String runtimeID = UUID.randomUUID().toString();

    private final String[] existsFormat;
    private final String[] notExistsFormat;

    public GameChooseSign(UserEditSignEvent event) {
        super(event);

        existsFormat = defaultExistsFormat();
        notExistsFormat = defaultNotExistsFormat();
    }

    public GameChooseSign(GameGroup gameGroup, Config config) {
        super(gameGroup, config);

        existsFormat = loadFormatFromConfig(config, "exists_format", defaultExistsFormat());
        notExistsFormat = loadFormatFromConfig(config, "not_exists_format", defaultNotExistsFormat());
    }

    private String[] defaultExistsFormat() {
        return new String[]{"&8[&3{formatted_type}&8]", "&cSpectate Games", "&8{amount} &0games available",
                "&9Right click chose"};
    }

    private String[] defaultNotExistsFormat() {
        return new String[]{"&8[&3{formatted_type}&8]", "", "&cNo Games", ""};
    }

    @Override
    protected void updateSign() {
        Config config = new MemoryConfig();

        config.set("type", gameGroupType);
        config.set("formatted_type", WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')));

        Collection<GameGroupInfo> all = gameGroup.getControllerInfo().getGameGroups(gameGroupType);

        config.set("amount", all.size());

        if (all.isEmpty()) {
            updateSignFromFormat(notExistsFormat, config);
        } else {
            updateSignFromFormat(existsFormat, config);
        }

        for (User user : gameGroup.getUsers()) {
            if (user.getClickableInventory() == null || !user.getClickableInventory().getIdentifier().equals(runtimeID))
                continue;

            updateInventory(user);
        }
    }

    @Override
    public void onRightClick(User user) {
        updateInventory(user);
    }

    private void updateInventory(final User user) {
        String title = WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')) + " Games";

        ClickableInventory inventory = new ClickableInventory(title, runtimeID);

        Collection<GameGroupInfo> allCollection = user.getGameGroup().getControllerInfo().getGameGroups(gameGroupType);

        List<GameGroupInfo> all = new ArrayList<>(allCollection);

        Collections.sort(all, (o1, o2) -> {
            if (o1.isAcceptingPlayers() && !o2.isAcceptingPlayers()) return 1;
            else if (o2.isAcceptingPlayers() && !o1.isAcceptingPlayers()) return -1;
            return o1.getName().compareTo(o2.getName());
        });

        for (GameGroupInfo gameGroup : all) {
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
                            .sendJoinGameGroupPacket(user.getUuid(), gameGroup.getType(), gameGroup.getName());
                }
            };

            inventory.addItem(clickableItem);
        }

        user.showInventory(inventory, location);
    }

    @Override
    public Config toConfig() {
        Config config = super.toConfig();

        config.set("spectators", true);

        return config;
    }
}
