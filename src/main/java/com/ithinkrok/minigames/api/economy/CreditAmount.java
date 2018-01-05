package com.ithinkrok.minigames.api.economy;

import java.math.BigDecimal;

public class CreditAmount {

    private final Credit credit;
    private final BigDecimal amount;


    public CreditAmount(Credit credit, BigDecimal amount) {
        this.credit = credit;
        this.amount = amount;
    }


    public Credit getCredit() {
        return credit;
    }


    public BigDecimal getAmount() {
        return amount;
    }
}
