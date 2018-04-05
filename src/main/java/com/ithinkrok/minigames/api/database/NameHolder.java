package com.ithinkrok.minigames.api.database;

import java.time.Instant;
import java.util.UUID;

public interface NameHolder {

    UUID getPlayerUUID();

    String getPlayerName();

    Instant getNameKnownAt();

}
