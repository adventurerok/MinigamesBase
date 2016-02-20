package com.ithinkrok.minigames.api.user;

import java.util.UUID;

/**
 * Created by paul on 03/01/16.
 */
public interface UserResolver {

    User getUser(UUID uuid);
}
