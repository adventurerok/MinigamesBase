package com.ithinkrok.minigames.hub.sign;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.inventory.ClickableItem;
import com.ithinkrok.minigames.api.inventory.event.UserClickItemEvent;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.sign.SignController;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.minigames.hub.inventory.GameChooseInventory;
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

    private final String[] existsFormat;
    private final String[] notExistsFormat;

    public GameChooseSign(UserEditSignEvent event, SignController signController) {
        super(event, signController);

        existsFormat = defaultExistsFormat();
        notExistsFormat = defaultNotExistsFormat();
    }

    private String[] defaultExistsFormat() {
        return new String[]{"&8[&3{formatted_type}&8]", "&cSpectate Games", "&8{amount} &0games available",
                "&9Right click chose"};
    }

    private String[] defaultNotExistsFormat() {
        return new String[]{"&8[&3{formatted_type}&8]", "", "&cNo Games", ""};
    }

    public GameChooseSign(GameGroup gameGroup, Config config, SignController signController) {
        super(gameGroup, config, signController);

        existsFormat = loadFormatFromConfig(config, "exists_format", defaultExistsFormat());
        notExistsFormat = loadFormatFromConfig(config, "not_exists_format", defaultNotExistsFormat());
    }

    @Override
    protected void updateSign() {
        Config config = new MemoryConfig();

        config.set("type", gameGroupType);
        config.set("formatted_type", WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')));

        int index = 0;
        for (String param : gameGroupParams) {
            config.set("param" + (index + 1), param);

            ++index;
        }

        Collection<GameGroupInfo> all = gameGroup.getControllerInfo().getGameGroups(gameGroupType, gameGroupParams);

        config.set("amount", all.size());

        if (all.isEmpty()) {
            updateSignFromFormat(notExistsFormat, config);
        } else {
            updateSignFromFormat(existsFormat, config);
        }

        for (User user : gameGroup.getUsers()) {
            if (user.getClickableInventory() == null ||
                    !user.getClickableInventory().getIdentifier().equals(GameChooseInventory.ID)) continue;

            updateInventory(user);
        }
    }

    @Override
    public void onRightClick(User user) {
        updateInventory(user);
    }

    private void updateInventory(final User user) {
        String title = WordUtils.capitalizeFully(gameGroupType.replace('_', ' ')) + " Games";

        ClickableInventory inventory = new GameChooseInventory(title, user, gameGroupType, gameGroupParams);

        user.showInventory(inventory, location);
    }

    @Override
    public Config toConfig() {
        Config config = super.toConfig();

        config.set("spectators", true);

        return config;
    }
}
