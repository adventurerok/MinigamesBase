package com.ithinkrok.minigames.api.economy;

import com.ithinkrok.msm.common.economy.AbstractEconomyContext;
import com.ithinkrok.msm.common.economy.Currency;
import com.ithinkrok.msm.common.economy.CurrencyType;
import com.ithinkrok.util.config.Config;

import java.util.HashMap;
import java.util.Map;

public class GameContext extends AbstractEconomyContext {

    private final Map<String, GameCurrency> currencyMap = new HashMap<>();

    public GameContext() {
        super(CurrencyType.MINIGAME_SPECIFIC);
    }

    @Override
    protected Currency lookupLocalCurrency(String name) {
        return currencyMap.get(name);
    }


    public void registerCurrency(String name, Config config) {
        currencyMap.put(name, new GameCurrency(this, name, config));
    }
}
