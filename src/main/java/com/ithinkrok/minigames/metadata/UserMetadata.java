package com.ithinkrok.minigames.metadata;

import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;

/**
 * Created by paul on 04/01/16.
 */
public abstract class UserMetadata extends Metadata {

    public abstract boolean removeOnInGameChange(UserInGameChangeEvent event);

    @Override
    public Class<? extends UserMetadata> getMetadataClass() {
        return getClass();
    }
}
