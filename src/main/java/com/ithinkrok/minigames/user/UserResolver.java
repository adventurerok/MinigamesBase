package com.ithinkrok.minigames.user;

import com.ithinkrok.minigames.User;

import java.util.UUID;

/**
 * Created by paul on 03/01/16.
 */
public interface UserResolver {

    User getUser(UUID uuid);
}
