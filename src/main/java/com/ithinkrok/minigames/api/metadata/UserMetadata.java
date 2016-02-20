package com.ithinkrok.minigames.api.metadata;

import com.ithinkrok.minigames.api.event.user.game.UserInGameChangeEvent;

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
