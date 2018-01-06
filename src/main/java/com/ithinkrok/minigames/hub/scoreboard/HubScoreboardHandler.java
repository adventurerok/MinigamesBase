package com.ithinkrok.minigames.hub.scoreboard;

import com.ithinkrok.minigames.api.protocol.data.ControllerInfo;
import com.ithinkrok.minigames.api.protocol.data.GameGroupInfo;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardDisplay;
import com.ithinkrok.minigames.api.user.scoreboard.ScoreboardHandler;
import com.ithinkrok.minigames.base.BasePlugin;
import com.ithinkrok.msm.common.economy.Account;
import com.ithinkrok.msm.common.economy.Currency;
import com.ithinkrok.msm.common.economy.result.Balance;
import com.ithinkrok.util.config.Config;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by paul on 18/09/16.
 */
public class HubScoreboardHandler implements ScoreboardHandler {

    private final ControllerInfo controllerInfo;

    private final String titleLocale;
    private final String playersOnlineLocale;
    private final String lobbyInfoLocale;
    private final String gamesInfoLocale;
    private final String playersOnHubLocale;
    private final String balancesLocale;


    public HubScoreboardHandler(Config config) {
        controllerInfo = BasePlugin.getRequestProtocol().getControllerInfo();

        titleLocale = config.getString("title_locale");
        playersOnlineLocale = config.getString("players_online_locale");
        lobbyInfoLocale = config.getString("lobby_info_locale");
        gamesInfoLocale = config.getString("games_info_locale");
        playersOnHubLocale = config.getString("players_on_hub_locale");
        balancesLocale = config.getString("balances_locale");
    }


    @Override
    public void updateScoreboard(User user, ScoreboardDisplay scoreboard) {
        scoreboard.setDisplayLocale(titleLocale);

        int gamesInProgress = 0;
        int lobbyCount = 0;

        int playersInGame = 0;
        int playersInLobby = 0;
        int playersOnline = 0;

        for (GameGroupInfo gameGroupInfo : controllerInfo.getGameGroups(null, Collections.emptyList())) {
            playersOnline += gameGroupInfo.getPlayerCount();

            //Assuming the user is in a hub gamegroup, prevent hubs showing up in the stats
            if (gameGroupInfo.getType().equals(user.getGameGroup().getType())) continue;

            if (gameGroupInfo.isAcceptingPlayers()) {
                //Lobby

                ++lobbyCount;
                playersInLobby += gameGroupInfo.getPlayerCount();
            } else {
                //Game in progress

                ++gamesInProgress;
                playersInGame += gameGroupInfo.getPlayerCount();
            }
        }

        int lc = 0;

        scoreboard.setTextLine(lc++, "");
        scoreboard.setTextLocale(lc++, playersOnlineLocale, playersOnline);
        scoreboard.setTextLocale(lc++, playersOnHubLocale, user.getGameGroup().getUserCount());
        scoreboard.setTextLocale(lc++, gamesInfoLocale, gamesInProgress);
        scoreboard.setTextLocale(lc++, "scoreboard.players_in_games", playersInGame);
        scoreboard.setTextLocale(lc++, lobbyInfoLocale, lobbyCount);
        scoreboard.setTextLocale(lc++, "scoreboard.players_in_lobbies", playersInLobby);

        scoreboard.setTextLine(lc++, "");


        scoreboard.setTextLocale(lc++, balancesLocale);
        Account account = user.getEconomyAccount();
        for (String currencyName : new String[]{"token_copper", "token_silver", "token_gold", "token_emerald",
                "token_ruby"}) {
            Currency currency = account.lookupCurrency(currencyName);
            if (currency == null) continue;

            Optional<Balance> balance = account.getBalance(currency);
            if (balance.isPresent()) {
                String formattedAmount = currency.format(balance.get().getAmount());
                formattedAmount = formattedAmount.replace(" Tokens", "");

                scoreboard.setTextLocale(lc++, "scoreboard.balance", formattedAmount);
            }


        }
    }


    @Override
    public void setupScoreboard(User user, ScoreboardDisplay scoreboard) {
        scoreboard.resetAndDisplay();
        scoreboard.setTextLineCount(19);

        updateScoreboard(user, scoreboard);
    }
}
