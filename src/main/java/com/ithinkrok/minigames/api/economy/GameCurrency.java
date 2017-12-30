package com.ithinkrok.minigames.api.economy;

import com.ithinkrok.msm.common.economy.AbstractCurrency;
import com.ithinkrok.msm.common.economy.CurrencyType;
import com.ithinkrok.msm.common.economy.EconomyContext;
import com.ithinkrok.util.config.Config;

public class GameCurrency extends AbstractCurrency {


    private final GameContext context;

    public GameCurrency(GameContext context, String name, Config config) {
        super(name, config);
        this.context = context;
    }

    @Override
    public String getCurrencyType() {
        return CurrencyType.MINIGAME_SPECIFIC;
    }

    @Override
    public EconomyContext getContext() {
        return context;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
