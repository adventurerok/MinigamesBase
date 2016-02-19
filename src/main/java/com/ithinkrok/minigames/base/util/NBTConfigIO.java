package com.ithinkrok.minigames.base.util;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.config.MemoryConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 19/02/16.
 */
public class NBTConfigIO {

    public static Config loadToConfig(CompoundTag compoundTag, Config config) {
        return loadToConfig(compoundTag.getValue(), config);
    }

    public static Config loadToConfig(CompoundMap compoundMap, Config config) {
        for(Map.Entry<String, Tag<?>> entry : compoundMap.entrySet()) {
            Object value = entry.getValue().getValue();

            config.set(entry.getKey(), toConfigObject(entry.getKey(), value, config));
        }

        return config;
    }

    public static Object toConfigObject(String name, Object value, Config config) {
        if(value instanceof CompoundMap) {
            return loadToConfig((CompoundMap) value, config.getConfigOrEmpty(name));
        } else if(value instanceof List<?>) {
            List<Object> result = new ArrayList<>();

            Config temp = new MemoryConfig();

            for(Object listItem : (Iterable<?>)value) {
                result.add(toConfigObject("", listItem, temp));
            }

            return result;
        } else return value;
    }
}
