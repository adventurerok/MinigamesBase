package com.ithinkrok.minigames.api.sign;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.minigames.hub.sign.GameChooseSign;
import com.ithinkrok.minigames.hub.sign.JoinLobbySign;
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

    public interface PlacedSignCreator {
        InfoSign createSign(UserEditSignEvent event);
    }

    private static final Map<String, PlacedSignCreator> loadedSignCreatorMap = new HashMap<>();


    public static void registerSignType(String signTopLine, PlacedSignCreator creator) {
        loadedSignCreatorMap.put(signTopLine, creator);
    }

    public static InfoSign createInfoSign(UserEditSignEvent event) {
        PlacedSignCreator creator = loadedSignCreatorMap.get(event.getLine(0));

        if(creator == null) return null;

        try {
            return creator.createSign(event);
        } catch (Exception ignored) {
            event.getUser().sendLocale("info_sign.invalid");
            return null;
        }
    }

    public static InfoSign loadInfoSign(GameGroup gameGroup, Config config) {
        String className = config.getString("class");

        //The class names may have changed due to refactoring
        className = mapOldClassNameToNewClassName(className);

        try {
            Class<?> clazz = Class.forName(className);

            Class<? extends InfoSign> signClazz = clazz.asSubclass(InfoSign.class);

            Constructor<? extends InfoSign> constructor = signClazz.getConstructor(GameGroup.class, Config.class);

            return constructor.newInstance(gameGroup, config);
        } catch (Exception e) {
            InfoSign sign = loadLegacySign(gameGroup, config);
            if(sign != null) return sign;

            System.out.println("Error loading InfoSign config " + className);
            e.printStackTrace();
            return null;
        }
    }

    private static String mapOldClassNameToNewClassName(String className) {
        switch (className) {
            case "com.ithinkrok.minigames.hub.HighScoreSign":
                return "com.ithinkrok.minigames.hub.sign.HighScoreSign";
            case "com.ithinkrok.minigames.hub.JoinLobbySign":
                return "com.ithinkrok.minigames.hub.sign.JoinLobbySign";
            case "com.ithinkrok.minigames.hub.GameChooseSign":
                return "com.ithinkrok.minigames.hub.sign.GameChooseSign";
            default:
                return className;
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
