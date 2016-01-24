package com.ithinkrok.minigames.util;

import com.ithinkrok.minigames.event.MinigamesEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.bukkit.event.Listener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by paul on 20/01/16.
 */
@RunWith(DataProviderRunner.class)
public class EventExecutorTest {

    @Mock MinigamesEvent event;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void shouldExecuteAllEventsInTheCorrectOrder() {
        OrderListener listener = new OrderListener();

        EventExecutor.executeEvent(event, listener);

        assertThat(listener.doneFirst && listener.doneLow && listener.doneHigh && listener.doneLast).isTrue();
    }

    private static class OrderListener implements Listener {

        private boolean doneFirst = false;
        private boolean doneLow = false;
        private boolean doneHigh = false;
        private boolean doneLast = false;

        @MinigamesEventHandler(priority = MinigamesEventHandler.INTERNAL_FIRST)
        public void internalFirstFirst(MinigamesEvent event) {
            assertThat(doneFirst || doneLow || doneHigh || doneLast).isFalse();

            doneFirst = true;
        }

        @MinigamesEventHandler(priority = MinigamesEventHandler.LOW)
        public void lowSecond(MinigamesEvent event) {
            assertThat(doneFirst && !(doneLow || doneHigh || doneLast)).isTrue();

            doneLow = true;
        }

        @MinigamesEventHandler(priority = MinigamesEventHandler.HIGH)
        public void highThird(MinigamesEvent event) {
            assertThat((doneFirst && doneLow) && !(doneHigh || doneLast)).isTrue();

            doneHigh = true;
        }

        @MinigamesEventHandler(priority = MinigamesEventHandler.HIGH)
        public void internalLastLast(MinigamesEvent event) {
            assertThat((doneFirst && doneLow && doneHigh) && !doneLast).isTrue();

            doneLast = true;
        }
    }
}