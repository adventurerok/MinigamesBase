package com.ithinkrok.minigames.base.economy;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.economy.Credit;
import com.ithinkrok.minigames.api.economy.CreditAmount;
import com.ithinkrok.minigames.api.economy.Rewarder;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.msm.common.economy.Currency;
import com.ithinkrok.msm.common.economy.EconomyContext;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.math.Calculator;
import com.ithinkrok.util.math.ExpressionCalculator;
import com.ithinkrok.util.math.SingleValueVariables;
import com.ithinkrok.util.math.Variables;
import com.ithinkrok.util.math.expression.SimplifyMode;

import java.math.BigDecimal;
import java.util.*;

public class MCColonyRewarder implements Rewarder {

    private final GameGroup gameGroup;

    private final Map<Credit, Currency> creditCurrencies = new EnumMap<>(Credit.class);

    /**
     * Apply to all, including score (score is handled server side)
     */
    private final Map<Credit, Calculator> creditMultipliers = new EnumMap<>(Credit.class);

    private final Map<Credit, Calculator> participationRewards = new EnumMap<>(Credit.class);

    private final Set<UUID> participationRewardsGiven = new HashSet<>();

    /**
     * Number of players used to calculate reward amount
     */
    private int maxPlayers; //TODO this value

    /**
     * Participation due to the type of game
     */
    private BigDecimal gameParticipationMultiplier = BigDecimal.ONE;


    /**
     * McColony wide multiplier for participating
     */
    private BigDecimal serverParticipationMultiplier = BigDecimal.ONE;

    /**
     * Server multiplier for immediate rewards
     */
    private BigDecimal serverImmediateMultiplier = BigDecimal.ONE;


    public MCColonyRewarder(GameGroup gameGroup) {
        this.gameGroup = gameGroup;

        EconomyContext ecoContext = gameGroup.getEconomy().getContext();

        for (Credit credit : Credit.values()) {
            creditCurrencies.put(credit, ecoContext.lookupCurrency(credit.getCurrency()));
        }

        loadRewardsConfig(gameGroup.getSharedObjectOrEmpty("rewards"));

        //TODO we need to listen for some events in the GameGroup
    }


    private void loadRewardsConfig(Config rewards) {
        Config multConfig = rewards.getConfigOrEmpty("mult");
        Config participationConfig = rewards.getConfigOrEmpty("participation");

        for (Credit credit : Credit.values()) {
            Currency currency = creditCurrencies.get(credit);
            SimplifyMode simplifyMode = SimplifyMode.decimal(currency.getMathContext());

            String multExpression = multConfig.getString(credit.name().toLowerCase());
            creditMultipliers.put(credit, new ExpressionCalculator(multExpression, simplifyMode));

            String participationExpression = participationConfig.getString(credit.name().toLowerCase());
            participationRewards.put(credit, new ExpressionCalculator(participationExpression, simplifyMode));
        }

    }


    @Override
    public void giveParticipationReward(User user) {
        if(participationRewardsGiven.contains(user.getUuid())) {
            //don't reward them twice
            return;
        }

        for (Credit credit : Credit.values()) {
            Currency currency = creditCurrencies.get(credit);
            Calculator typeMultCalc = creditMultipliers.get(credit);
            BigDecimal typeMult = typeMultCalc.calculateDecimal(variables(), currency.getMathContext());

            Calculator partCalc = participationRewards.get(credit);
            BigDecimal partReward = partCalc.calculateDecimal(variables(), currency.getMathContext());

            BigDecimal amountTemp = partReward.multiply(typeMult, currency.getMathContext());
            amountTemp = amountTemp.multiply(gameParticipationMultiplier, currency.getMathContext());
            amountTemp = amountTemp.multiply(serverParticipationMultiplier, currency.getMathContext());

            BigDecimal amount = amountTemp;

            if(amount.compareTo(BigDecimal.ZERO) > 0) {
                user.getEconomyAccount().deposit(currency, amount, "minigame participation", result -> {
                    participationRewardsGiven.add(user.getUuid());
                    user.sendLocale("reward.participation.get", amount);
                });
            }
        }
    }


    @Override
    public void addScoreReward(UUID user, CreditAmount... amountsPerType) {
        //TODO message controller
    }


    @Override
    public boolean giveImmediateReward(User user, CreditAmount... amountsPerType) {
        boolean givenReward = false;

        for (CreditAmount ca : amountsPerType) {
            Currency currency = creditCurrencies.get(ca.getCredit());
            Calculator typeMultCalc = creditMultipliers.get(ca.getCredit());
            BigDecimal typeMult = typeMultCalc.calculateDecimal(variables(), currency.getMathContext());

            BigDecimal amountTemp = ca.getAmount().multiply(typeMult, currency.getMathContext());
            amountTemp = amountTemp.multiply(serverImmediateMultiplier);

            //We need to be "effectively final"
            BigDecimal amount = amountTemp;

            if(amount.compareTo(BigDecimal.ZERO) > 0) {
                user.getEconomyAccount().deposit(currency, amount, "minigame reward", result -> {
                    user.sendLocale("reward.immediate.get", currency.format(amount));
                });
                givenReward = true;
            }
        }

        return givenReward;
    }

    private Variables variables() {
        return new SingleValueVariables(maxPlayers);
    }


    @Override
    public void giveAllScoreRewards() {
        //TODO message controller
    }
}
