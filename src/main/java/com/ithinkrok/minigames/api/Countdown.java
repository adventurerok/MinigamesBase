package com.ithinkrok.minigames.api;

import com.ithinkrok.minigames.api.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.api.event.game.CountdownMessageEvent;
import com.ithinkrok.minigames.api.task.GameTask;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.lang.LanguageLookup;

/**
 * Created by paul on 04/01/16.
 */
public class Countdown implements Nameable {

    private final String name;
    private final String localeStub;

    private GameTask task;
    private int secondsRemaining;

    public Countdown(String name, String localeStub, int secondsRemaining) {
        this.name = name;
        this.localeStub = localeStub;
        this.secondsRemaining = secondsRemaining + 1;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public void setSecondsRemaining(int seconds) {
        this.secondsRemaining = seconds;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormattedName() {
        return name;
    }

    public void start(GameGroup gameGroup) {
        String startMessage = gameGroup.getLocale(localeStub + ".start", secondsRemaining - 1);
        if (startMessage != null) gameGroup.sendMessage(startMessage);

        task = gameGroup.repeatInFuture(task -> {
            --secondsRemaining;

            doCountdownMessage(gameGroup);

            for (User user : gameGroup.getUsers()) {
                user.setXpLevel(secondsRemaining);
            }

            if (secondsRemaining > 0) return;

            CountdownFinishedEvent event = new CountdownFinishedEvent(gameGroup, Countdown.this);
            gameGroup.gameEvent(event);

            //The event can change the amount of time left in the countdown
            if (secondsRemaining > 0) return;

            task.finish();
        }, 20, 20);
    }

    private void doCountdownMessage(GameGroup gameGroup) {
        LanguageLookup lookup = gameGroup.getLanguageLookup();
        String message = null;

        if (secondsRemaining > 30) {
            if (secondsRemaining % 30 != 0) return;

            if (secondsRemaining % 60 == 0) {
                message = lookup.getLocale(localeStub + ".minutes", secondsRemaining / 60);
            } else {
                message =
                        lookup.getLocale(localeStub + ".minutes_seconds", secondsRemaining / 60, secondsRemaining % 60);
            }
        } else {
            switch (secondsRemaining) {
                case 30:
                case 10:
                    message = lookup.getLocale(localeStub + ".seconds", secondsRemaining);
                    break;
                case 5:
                case 4:
                case 3:
                case 2:
                case 1:
                    message = lookup.getLocale(localeStub + ".final", secondsRemaining);
                    break;
                case 0:
                    message = lookup.getLocale(localeStub + ".now");
            }
        }

        CountdownMessageEvent event = new CountdownMessageEvent(gameGroup, this, message);
        gameGroup.gameEvent(event);

        if (message != null) {
            gameGroup.sendMessage(message);
            for (User user : gameGroup.getUsers()) {
                user.showAboveHotbarMessage(message);
            }
        }
    }

    public void cancel() {
        task.cancel();
    }

    public boolean isFinished() {
        return task.getTaskState() == GameTask.TaskState.FINISHED ||
                task.getTaskState() == GameTask.TaskState.CANCELLED;
    }
}
