package com.ithinkrok.minigames.api.economy;

import com.ithinkrok.minigames.api.user.User;

import java.util.UUID;

public interface Rewarder {

    /**
     * Gives the specified user their score reward for playing in this game. Can only be called once per game.
     */
    void giveParticipationReward(User user);

    /**
     * Adds the specified amounts to the score reward pools for these users.
     *
     * Please note the AMOUNT is in pool shares, not actually amount of the currency.
     */
    default void addScoreReward(User user, CreditAmount... amountsPerType) {
        addScoreReward(user.getUuid(), amountsPerType);
    }


    /**
     * Can be used to add an additional score reward to a user who disconnected mid game.
     */
    void addScoreReward(UUID user, CreditAmount... amountsPerType);


    /**
     * Gives the user immediate rewards of the type.
     *
     * The amount can still be modified by server wide boosters and rules such as disabling giving of a certain type.
     *
     * @return If any reward was given
     */
    boolean giveImmediateReward(User user, CreditAmount... amountsPerType);


    /**
     * Give all the score rewards for this GameGroup.
     *
     * Should only be called when the game is over and the winner is known (and credited with a score reward :P)
     */
    void giveAllScoreRewards();

}
