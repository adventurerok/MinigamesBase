package com.ithinkrok.minigames.item;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 02/01/16.
 */
public class IdentifierMap<I extends Identifiable> {

    private Map<String, I> namedMap = new HashMap<>();
    private Map<Integer, I> idMap = new HashMap<>();

    public void put(String name, I value) {
        namedMap.put(name, value);
        idMap.put(value.getIdentifier(), value);
    }

    public I get(String name) {
        return namedMap.get(name);
    }

    public I get(int identifier) {
        return idMap.get(identifier);
    }

    public void clear() {
        namedMap.clear();
        idMap.clear();
    }
}
