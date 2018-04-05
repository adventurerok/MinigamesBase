package com.ithinkrok.minigames.api.util;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class NameUpdater extends Thread {

    private final static NameUpdater singleton = new NameUpdater();

    private static Queue<UUID> toLookup = new ConcurrentLinkedQueue<>();
    private static ConcurrentHashMap<UUID, Set<Consumer<NameResult>>> callbacks = new ConcurrentHashMap<>();

    //for each UUID we only error once
    private static Set<UUID> badUUIDs = new HashSet<>();

    private static volatile boolean exit = false;


    private NameUpdater() {


        start();
    }


    public static void lookupName(UUID uuid, Consumer<NameResult> callback) {
        callbacks.compute(uuid, (uuid1, consumers) -> {
            if (consumers == null) {
                consumers = new HashSet<>();
                toLookup.add(uuid);
            }

            consumers.add(callback);
            return consumers;
        });
    }


    @Override
    public void run() {
        while (!exit) {
            UUID next = toLookup.poll();

            if (next != null) {
                Set<Consumer<NameResult>> consumers = callbacks.remove(next);
                doLookupNames(next, consumers);
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
                System.out.println("NameUpdater thread interrupted");
                return;
            }
        }
    }


    private void doLookupNames(UUID uuid, Collection<Consumer<NameResult>> consumers) {
        String uuidString = uuid.toString().replace("-", "");
        String url = "https://api.mojang.com/user/profiles/" + uuidString + "/names";

        try {
            String nameJson = IOUtils.toString(new URL(url), Charsets.UTF_8);
            JSONArray nameValue = (JSONArray) JSONValue.parseWithException(nameJson);

            String playerSlot = nameValue.get(nameValue.size() - 1).toString();
            JSONObject nameObject = (JSONObject) JSONValue.parseWithException(playerSlot);

            String name = nameObject.get("name").toString();

            consumers.forEach(nameResultConsumer -> nameResultConsumer.accept(new NameResult(uuid, name)));

        } catch (IOException | ParseException e) {
            if(!badUUIDs.contains(uuid)) {
                System.err.println("Error on name updater thread name lookup for UUID (perhaps invalid) " + uuid);
                e.printStackTrace();
                badUUIDs.add(uuid);
            }
        }
    }


    public static class NameResult {

        public final UUID uuid;
        public final String name;


        private NameResult(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }
    }

}
