package com.ithinkrok.minigames.api.item.attributes;

import java.util.HashMap;
import java.util.Map;

public enum Slot {

    MAIN_HAND("mainhand"), OFF_HAND("offhand"), FEET("feet"), LEGS("legs"), CHEST("chest"), HEAD("head");

    private static final Map<String, Slot> fromName = new HashMap<>();

    static {
        for (Slot slot : values()) {
            fromName.put(slot.name, slot);
        }

    }

    private final String name;

    Slot(String name) {
        this.name = name;
    }

    public static Slot getFromName(String name) {
        Slot result = fromName.get(name);

        if(result == null) {
            try{
                return valueOf(name.toUpperCase());
            } catch(Exception ignored) {
                return null;
            }
        } else return result;
    }

    /**
     * Get the predefined, global and unique name of this slot.
     *
     * @return The name
     */
    public String getName() {
        return this.name;
    }

}
