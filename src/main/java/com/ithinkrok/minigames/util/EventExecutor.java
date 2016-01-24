package com.ithinkrok.minigames.util;

import com.ithinkrok.minigames.event.MinigamesEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by paul on 01/01/16.
 */
public class EventExecutor {

    private static Map<Class<? extends Listener>, ListenerHandler> listenerHandlerMap = new HashMap<>();

    public static void executeEvent(MinigamesEvent event, Listener... listeners) {
        executeListeners(event, getMethodExecutorMap(event, listeners));
    }

    private static void executeListeners(MinigamesEvent event, SortedMap<MethodExecutor, Listener> map) {
        for (Map.Entry<MethodExecutor, Listener> entry : map.entrySet()) {
            try {
                entry.getKey().execute(entry.getValue(), event);
            } catch (EventException e) {
                System.out.println("Failed while calling event listener: " + entry.getValue().getClass());
                e.printStackTrace();
            }
        }
    }

    private static SortedMap<MethodExecutor, Listener> getMethodExecutorMap(MinigamesEvent event,
                                                                            Listener... listeners) {
        SortedMap<MethodExecutor, Listener> map = new TreeMap<>();

        for (Listener listener : listeners) {
            if (listener == null) continue;
            for (MethodExecutor methodExecutor : getMethodExecutors(listener, event)) {
                map.put(methodExecutor, listener);
            }
        }

        return map;
    }

    private static Collection<MethodExecutor> getMethodExecutors(Listener listener, MinigamesEvent event) {
        ListenerHandler handler = listenerHandlerMap.get(listener.getClass());

        if (handler == null) {
            handler = new ListenerHandler(listener.getClass());
            listenerHandlerMap.put(listener.getClass(), handler);
        }

        return handler.getMethodExecutors(event);
    }

    @SafeVarargs
    public static void executeEvent(MinigamesEvent event, Collection<Listener>... listeners) {
        executeListeners(event, getMethodExecutorMap(event, listeners));
    }

    @SafeVarargs
    private static SortedMap<MethodExecutor, Listener> getMethodExecutorMap(MinigamesEvent event,
                                                                            Collection<Listener>... listeners) {
        SortedMap<MethodExecutor, Listener> map = new TreeMap<>();

        for (Collection<Listener> listenerGroup : listeners) {
            addToMethodExecutorMap(event, listenerGroup, map);
        }

        return map;
    }

    private static void addToMethodExecutorMap(MinigamesEvent event, Collection<Listener> listenerGroup,
                                               SortedMap<MethodExecutor, Listener> map) {
        for (Listener listener : listenerGroup) {
            if (listener == null) continue;
            for (MethodExecutor methodExecutor : getMethodExecutors(listener, event)) {
                map.put(methodExecutor, listener);
            }
        }
    }

    public static void executeEvent(MinigamesEvent event, Collection<Collection<Listener>> listeners) {
        executeListeners(event, getMethodExecutorMap(event, listeners));
    }

    private static SortedMap<MethodExecutor, Listener> getMethodExecutorMap(MinigamesEvent event,
                                                                            Collection<Collection<Listener>> listeners) {
        SortedMap<MethodExecutor, Listener> map = new TreeMap<>();

        for (Collection<Listener> listenerGroup : listeners) {
            addToMethodExecutorMap(event, listenerGroup, map);
        }

        return map;
    }

    private static class ListenerHandler {
        private Class<? extends Listener> listenerClass;

        private Map<Class<? extends MinigamesEvent>, List<MethodExecutor>> eventMethodsMap = new HashMap<>();

        public ListenerHandler(Class<? extends Listener> listenerClass) {
            this.listenerClass = listenerClass;
        }

        public Collection<MethodExecutor> getMethodExecutors(MinigamesEvent event) {
            List<MethodExecutor> eventMethods = eventMethodsMap.get(event.getClass());

            if (eventMethods == null) {
                eventMethods = new ArrayList<>();

                for (Method method : listenerClass.getMethods()) {
                    if (method.getParameterCount() != 1) continue;
                    if (!method.isAnnotationPresent(MinigamesEventHandler.class)) continue;
                    if (!method.getParameterTypes()[0].isInstance(event)) continue;

                    //Allows the usage of private classes as listeners
                    method.setAccessible(true);

                    eventMethods.add(new MethodExecutor(method,
                            method.getAnnotation(MinigamesEventHandler.class).ignoreCancelled()));
                }

                Collections.sort(eventMethods);

                eventMethodsMap.put(event.getClass(), eventMethods);
            }

            return eventMethods;
        }
    }

    private static class MethodExecutor implements Comparable<MethodExecutor> {
        private Method method;
        private boolean ignoreCancelled;

        public MethodExecutor(Method method, boolean ignoreCancelled) {
            this.method = method;
            this.ignoreCancelled = ignoreCancelled;
        }

        public void execute(Listener listener, MinigamesEvent event) throws EventException {
            if (ignoreCancelled && (event instanceof Cancellable) && ((Cancellable) event).isCancelled()) return;

            try {
                method.invoke(listener, event);
            } catch (Exception e) {
                throw new EventException(e, "Failed while calling event method: " + method.getName());
            }
        }

        @Override
        public int compareTo(MethodExecutor o) {
            int priorityCompare = Integer.compare(method.getAnnotation(MinigamesEventHandler.class).priority(), o
                    .method.getAnnotation(MinigamesEventHandler.class).priority());

            if (priorityCompare != 0) return priorityCompare;

            //TODO possible speed improvement here
            return method.toString().compareTo(o.method.toString());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MethodExecutor that = (MethodExecutor) o;

            if (ignoreCancelled != that.ignoreCancelled) return false;
            return method.equals(that.method);

        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            result = 31 * result + (ignoreCancelled ? 1 : 0);
            return result;
        }
    }
}
