package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.task.GameTask;

/**
 * Created by paul on 04/01/16.
 */
public class Countdown {

    private final String name;
    private final String localeStub;

    private GameTask task;
    private int secondsRemaining;

    public void setSecondsRemaining(int seconds) {
        this.secondsRemaining = seconds;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public String getName() {
        return name;
    }

    public Countdown(String name, String localeStub, int secondsRemaining) {
        this.name = name;
        this.localeStub = localeStub;
        this.secondsRemaining = secondsRemaining + 1;
    }

    public void start(GameGroup gameGroup) {
        String startMessage = gameGroup.getLocale(localeStub + ".start", secondsRemaining - 1);
        if(startMessage != null) gameGroup.sendMessage(startMessage);

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

    public void cancel() {
        task.cancel();
    }

    private void doCountdownMessage(Messagable messagable) {
        LanguageLookup lookup = messagable.getLanguageLookup();
        String message = null;

        if (secondsRemaining > 30) {
            if (secondsRemaining % 60 != 0) return;
            message = lookup.getLocale(localeStub + ".minutes", secondsRemaining / 60);
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

        if(message != null) messagable.sendMessage(message);
    }

    public boolean isFinished() {
        return task.getTaskState() == GameTask.TaskState.FINISHED ||
                task.getTaskState() == GameTask.TaskState.CANCELLED;
    }
}
