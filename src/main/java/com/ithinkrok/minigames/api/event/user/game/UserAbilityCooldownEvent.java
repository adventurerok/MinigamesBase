package com.ithinkrok.minigames.api.event.user.game;

import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.api.event.user.UserEvent;
import com.ithinkrok.minigames.api.util.SoundEffect;

/**
 * Created by paul on 03/01/16.
 */
public class UserAbilityCooldownEvent extends UserEvent {

    private final String ability;

    private SoundEffect soundEffect;
    private String coolDownMessage;

    public UserAbilityCooldownEvent(User user, String ability, SoundEffect soundEffect, String coolDownMessage) {
        super(user);
        this.ability = ability;
        this.soundEffect = soundEffect;
        this.coolDownMessage = coolDownMessage;
    }

    public String getAbility() {
        return ability;
    }

    public SoundEffect getSoundEffect() {
        return soundEffect;
    }

    public String getCoolDownMessage() {
        return coolDownMessage;
    }

    public void setSoundEffect(SoundEffect soundEffect) {
        this.soundEffect = soundEffect;
    }

    public void setCoolDownMessage(String coolDownMessage) {
        this.coolDownMessage = coolDownMessage;
    }

}
