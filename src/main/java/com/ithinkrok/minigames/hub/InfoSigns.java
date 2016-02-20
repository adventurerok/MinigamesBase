package com.ithinkrok.minigames.hub;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.util.config.Config;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 20/02/16.
 */
public final class InfoSigns {

    private InfoSigns() {

    }

    private interface PlacedSignCreator {
        InfoSign createSign(UserEditSignEvent event);
    }

    private static final Map<String, PlacedSignCreator> loadedSignCreatorMap = new HashMap<>();


    static {
        loadedSignCreatorMap.put("%lobby_sign%", JoinLobbySign::new);
        loadedSignCreatorMap.put("%choose_sign%", GameChooseSign::new);
    }

    public static InfoSign createInfoSign(UserEditSignEvent event) {
        PlacedSignCreator creator = loadedSignCreatorMap.get(event.getLine(0));

        if(creator == null) return null;

        return creator.createSign(event);
    }

    public static InfoSign loadInfoSign(GameGroup gameGroup, Config config) {
        String className = config.getString("class");

        try {
            Class<?> clazz = Class.forName(className);

            Class<? extends InfoSign> signClazz = clazz.asSubclass(InfoSign.class);

            Constructor<? extends InfoSign> constructor = signClazz.getConstructor(GameGroup.class, Config.class);

            return constructor.newInstance(gameGroup, config);
        } catch (ReflectiveOperationException | ClassCastException e) {
            InfoSign sign = loadLegacySign(gameGroup, config);
            if(sign != null) return sign;

            System.out.println("Error loading InfoSign config " + className);
            e.printStackTrace();
            return null;
        }
    }

    private static InfoSign loadLegacySign(GameGroup gameGroup, Config config) {
        if(!config.contains("type") || !config.contains("spectators")) return null;

        if(config.getBoolean("spectators")) {
            return new GameChooseSign(gameGroup, config);
        } else {
            return new JoinLobbySign(gameGroup, config);
        }
    }
}
