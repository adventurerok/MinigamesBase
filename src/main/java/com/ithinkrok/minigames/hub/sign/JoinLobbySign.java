package com.ithinkrok.minigames.hub.sign;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.sign.SignController;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.hub.sign.HubSign;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;
import org.apache.commons.lang.WordUtils;

import java.util.Collection;

/**
 * Created by paul on 20/02/16.
 */
public class JoinLobbySign extends HubSign {


    private final String[] existsFormat;
    private final String[] notExistsFormat;

    public JoinLobbySign(UserEditSignEvent event, SignController signController) {
        super(event, signController);

        existsFormat = defaultExistsFormat();
        notExistsFormat = defaultNotExistsFormat();
    }

    private String[] defaultExistsFormat() {
        return new String[]{"&8[&3{formatted_type}&8]", "&cJoin Lobby", "&8[&7{player_count}&8/&7{max_player_count}&8]",
                "{motd}"};
    }

    private String[] defaultNotExistsFormat() {
        return new String[]{"&8[&3{formatted_type}&8]", "", "&cCreate new Lobby", ""};
    }


    public JoinLobbySign(GameGroup gameGroup, Config config, SignController signController) {
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
        for(String param : gameGroupParams) {
            config.set("param" + index, param);

            ++index;
        }

        Collection<GameGroupInfo> accepting =
                gameGroup.getControllerInfo().getAcceptingGameGroups(gameGroupType, gameGroupParams);

        GameGroupInfo bestMatch = null;

        for (GameGroupInfo gameGroupInfo : accepting) {
            if (bestMatch != null && gameGroupInfo.getPlayerCount() <= bestMatch.getPlayerCount()) continue;

            bestMatch = gameGroupInfo;
        }

        if (bestMatch == null) {
            updateSignFromFormat(notExistsFormat, config);
            return;
        }

        config.set("player_count", bestMatch.getPlayerCount());
        config.set("max_player_count", bestMatch.getMaxPlayerCount());
        config.set("name", bestMatch.getName());
        config.set("motd", bestMatch.getMotd());

        updateSignFromFormat(existsFormat, config);
    }

    @Override
    public void onRightClick(User user) {
        Collection<GameGroupInfo> accepting =
                user.getGameGroup().getControllerInfo().getAcceptingGameGroups(gameGroupType, gameGroupParams);

        GameGroupInfo bestMatch = null;

        for (GameGroupInfo gameGroupInfo : accepting) {
            if (bestMatch != null && gameGroupInfo.getPlayerCount() < bestMatch.getPlayerCount()) continue;

            bestMatch = gameGroupInfo;
        }

        String gameGroupName = bestMatch != null ? bestMatch.getName() : null;

        if (gameGroupName != null) user.sendMessage("Sending you to gamegroup: " + gameGroupName);
        else user.sendMessage("Creating a new " + gameGroupType + " gamegroup for you");

        user.getGameGroup().getRequestProtocol().sendJoinGameGroupPacket(user.getUuid(), gameGroupType, gameGroupName);
    }

    @Override
    public Config toConfig() {
        Config config = super.toConfig();

        config.set("spectators", false);

        return config;
    }
}
