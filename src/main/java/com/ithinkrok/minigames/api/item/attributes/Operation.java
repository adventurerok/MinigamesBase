package com.ithinkrok.minigames.api.item.attributes;

public enum Operation {

    ADDITIVE(0),
    MULTIPLICATIVE_ADDITIVE(1),
    MULTIPLICATIVE(2);

    private final int id;

    Operation(int id) {

        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Operation getFromId(int id) {
        return values()[id];
    }
}
