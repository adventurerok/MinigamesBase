package com.ithinkrok.minigames.api.economy;

public enum Credit {

    COPPER("token_copper"),
    SILVER("token_silver"),
    GOLD("token_gold");


    private final String currency;


    Credit(String currency) {

        this.currency = currency;
    }


    public String getCurrency() {
        return currency;
    }
}
