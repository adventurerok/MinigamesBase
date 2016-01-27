package com.ithinkrok.minigames.base.metadata;

import com.ithinkrok.minigames.base.event.user.game.UserInGameChangeEvent;

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
