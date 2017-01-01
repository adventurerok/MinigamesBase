package com.ithinkrok.minigames.api.event.user;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.MinigamesEvent;
import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 01/01/17.
 */
public interface UserEvent extends MinigamesEvent {

    User getUser();

    GameGroup getGameGroup();
}
