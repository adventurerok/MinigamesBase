package com.ithinkrok.minigames.api.event.game;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.MinigamesEvent;

/**
 * Created by paul on 01/01/17.
 */
public interface GameEvent extends MinigamesEvent {

    GameGroup getGameGroup();
}
