package com.ithinkrok.minigames.api.database;

import java.util.UUID;

public final class NameResult {

    public final UUID uuid;
    public final String name;


    public NameResult(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
}
