package com.ithinkrok.minigames.api.sign;

import com.google.common.collect.ClassToInstanceMap;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.event.user.world.UserEditSignEvent;
import com.ithinkrok.util.config.Config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 20/02/16.
 */
public final class InfoSigns {

    private static final Map<String, PlacedSignCreator> loadedSignCreatorMap = new HashMap<>();

    private InfoSigns() {

    }

    public static void registerSignType(String signTopLine, Class<? extends InfoSign> signClass,
                                        PlacedSignConstructor creator) {
        loadedSignCreatorMap.put(signTopLine, new PlacedSignCreator(creator, signClass));
    }

    public static InfoSign createInfoSign(UserEditSignEvent event, ClassToInstanceMap<SignController> signControllers) {
        PlacedSignCreator creator = loadedSignCreatorMap.get(event.getLine(0));

        if (creator == null) return null;

        try {
            SignController controller = getSignControllerForSign(signControllers, creator.getSignClass());

            return creator.createSign(event, controller);
        } catch (Exception ignored) {
            event.getUser().sendLocale("info_sign.invalid");
            return null;
        }
    }

    public static InfoSign loadInfoSign(GameGroup gameGroup, Config config,
                                        ClassToInstanceMap<SignController> signControllers) {
        String className = config.getString("class");

        //The class names may have changed due to refactoring
        className = mapOldClassNameToNewClassName(className);

        try {
            Class<?> clazz = Class.forName(className);
            Class<? extends InfoSign> signClass = clazz.asSubclass(InfoSign.class);

            SignController controller = getSignControllerForSign(signControllers, signClass);

            Constructor<? extends InfoSign> constructor =
                    signClass.getConstructor(GameGroup.class, Config.class, SignController.class);

            return constructor.newInstance(gameGroup, config, controller);
        } catch (Exception e) {
            System.out.println("Error loading InfoSign config " + className);
            e.printStackTrace();
            return null;
        }
    }

    public static SignController getSignControllerForSign(ClassToInstanceMap<SignController> signControllers,
                                                          Class<? extends InfoSign> signClass)
            throws ReflectiveOperationException {

        Class<? extends SignController> controllerClass = getSignControllerClass(signClass);

        SignController controller = null;
        if (controllerClass != null) {
            controller = signControllers.getInstance(controllerClass);
            if (controller == null) {
                controller = controllerClass.newInstance();
            }
        }
        return controller;
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

    private static Class<? extends SignController> getSignControllerClass(Class<?> signClass)
            throws ReflectiveOperationException {
        if (!InfoSign.class.isAssignableFrom(signClass)) return null;

        try {
            Method method = signClass.getMethod("getControllerClass");

            if (!Modifier.isStatic(method.getModifiers())) {
                return getSignControllerClass(signClass.getSuperclass());
            }

            Object result = method.invoke(null);
            if (!(result instanceof Class<?>)) {
                return getSignControllerClass(signClass.getSuperclass());
            }

            Class<?> resultClass = (Class<?>) result;
            if (!SignController.class.isAssignableFrom(resultClass)) {
                return getSignControllerClass(signClass.getSuperclass());
            }

            return resultClass.asSubclass(SignController.class);
        } catch (ReflectiveOperationException ignored) {
            return getSignControllerClass(signClass.getSuperclass());
        }
    }

    public interface PlacedSignConstructor {
        InfoSign createSign(UserEditSignEvent event, SignController signController);
    }

    private static class PlacedSignCreator {

        PlacedSignConstructor constructor;
        Class<? extends InfoSign> signClass;

        public PlacedSignCreator(PlacedSignConstructor constructor, Class<? extends InfoSign> signClass) {
            this.constructor = constructor;
            this.signClass = signClass;
        }

        InfoSign createSign(UserEditSignEvent event, SignController signController) {
            return constructor.createSign(event, signController);
        }

        Class<? extends InfoSign> getSignClass() {
            return signClass;
        }
    }
}
