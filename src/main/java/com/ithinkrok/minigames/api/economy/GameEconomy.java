package com.ithinkrok.minigames.api.economy;

import com.ithinkrok.msm.common.economy.Currency;
import com.ithinkrok.msm.common.economy.CurrencyType;
import com.ithinkrok.msm.common.economy.Economy;
import com.ithinkrok.msm.common.economy.provider.EconomyProvider;
import com.ithinkrok.msm.common.economy.provider.LookupEconomyProvider;
import com.ithinkrok.msm.common.economy.provider.MemoryEconomyProvider;

import java.util.Collections;
import java.util.Set;

public class GameEconomy implements Economy {

    private final GameContext context = new GameContext();

    private EconomyProvider serverProvider;

    private final MemoryEconomyProvider memoryProvider = new MemoryEconomyProvider(context);
    private final GameEconomyProvider economyProvider = new GameEconomyProvider();


    @Override
    public EconomyProvider getProvider() {
        return economyProvider;
    }

    @Override
    public GameContext getContext() {
        return context;
    }

    public void setParent(Economy parent) {
        this.context.setParent(parent.getContext());
        this.serverProvider = parent.getProvider();
    }

    private final class GameEconomyProvider extends LookupEconomyProvider {

        @Override
        protected EconomyProvider lookupProviderForCurrency(Currency currency) {
            if(currency.getCurrencyType().equals(CurrencyType.MINIGAME_SPECIFIC)) {
                return memoryProvider;
            } else {
                return serverProvider;
            }
        }

        @Override
        public Set<Currency> getManagedCurrencies() {
            return Collections.emptySet();
        }
    }
}
