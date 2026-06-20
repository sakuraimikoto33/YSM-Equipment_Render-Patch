package net.okitsu.ysmequipmentrenderpatch.launch;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class YsmServiceBridge {
    private static final Map<ClassLoader, GameDispatcher> DISPATCHERS = new ConcurrentHashMap<>();

    private YsmServiceBridge() {
    }

    public static Object findElytra(Object entity) {
        GameDispatcher dispatcher = dispatcher(entity);
        return dispatcher == null ? null : dispatcher.findElytra(entity);
    }

    public static boolean isHiddenCuriosElytra(Object entity, Object stack) {
        GameDispatcher dispatcher = dispatcher(entity);
        return dispatcher != null && dispatcher.isHiddenCuriosElytra(entity, stack);
    }

    public static void renderLantern(Object poseStack, Object bufferSource, int packedLight, Object customPlayerEntity, float partialTick) {
        GameDispatcher dispatcher = dispatcher(customPlayerEntity);
        if (dispatcher != null) {
            dispatcher.renderLantern(poseStack, bufferSource, packedLight, customPlayerEntity, partialTick);
        }
    }

    private static GameDispatcher dispatcher(Object context) {
        if (context == null) {
            return null;
        }

        ClassLoader gameClassLoader = context.getClass().getClassLoader();
        if (gameClassLoader == null) {
            return null;
        }
        return DISPATCHERS.computeIfAbsent(gameClassLoader, GameDispatcher::new);
    }

    private static final class GameDispatcher {
        private final ClassLoader gameClassLoader;
        private final ChildFirstPatchClassLoader patchClassLoader;
        private final Method findElytra;
        private final Method isHiddenCuriosElytra;
        private final Method renderLantern;

        private GameDispatcher(ClassLoader gameClassLoader) {
            this.gameClassLoader = gameClassLoader;
            this.patchClassLoader = new ChildFirstPatchClassLoader(gameClassLoader);
            this.findElytra = findMethod(
                    "net.okitsu.ysmequipmentrenderpatch.compat.ElytraEquipmentLookup",
                    "findElytra",
                    "net.minecraft.world.entity.LivingEntity"
            );
            this.isHiddenCuriosElytra = findMethod(
                    "net.okitsu.ysmequipmentrenderpatch.compat.ElytraEquipmentLookup",
                    "isHiddenCuriosElytra",
                    "net.minecraft.world.entity.LivingEntity",
                    "net.minecraft.world.item.ItemStack"
            );
            this.renderLantern = findMethod(
                    "net.okitsu.ysmequipmentrenderpatch.client.LanternYsmRenderer",
                    "render",
                    "com.mojang.blaze3d.vertex.PoseStack",
                    "net.minecraft.client.renderer.MultiBufferSource",
                    int.class,
                    Object.class,
                    float.class
            );
        }

        private Object findElytra(Object entity) {
            return invoke(this.findElytra, entity);
        }

        private boolean isHiddenCuriosElytra(Object entity, Object stack) {
            return Boolean.TRUE.equals(invoke(this.isHiddenCuriosElytra, entity, stack));
        }

        private void renderLantern(Object poseStack, Object bufferSource, int packedLight, Object customPlayerEntity, float partialTick) {
            invoke(this.renderLantern, poseStack, bufferSource, packedLight, customPlayerEntity, partialTick);
        }

        private Method findMethod(String ownerName, String methodName, Object... parameterTypeNames) {
            try {
                Class<?> owner = Class.forName(ownerName, true, this.patchClassLoader);
                Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
                for (int index = 0; index < parameterTypeNames.length; index++) {
                    Object parameterType = parameterTypeNames[index];
                    parameterTypes[index] = parameterType instanceof Class<?> classType
                            ? classType
                            : Class.forName((String) parameterType, false, this.gameClassLoader);
                }

                Method method = owner.getMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                return null;
            }
        }

        private static Object invoke(Method method, Object... arguments) {
            if (method == null) {
                return null;
            }

            try {
                return method.invoke(null, arguments);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                return null;
            }
        }
    }

    private static final class ChildFirstPatchClassLoader extends URLClassLoader {
        private static final String[] CHILD_FIRST_PACKAGES = {
                "net.okitsu.ysmequipmentrenderpatch.client.",
                "net.okitsu.ysmequipmentrenderpatch.compat.",
                "net.okitsu.ysmequipmentrenderpatch.runtime."
        };

        private ChildFirstPatchClassLoader(ClassLoader parent) {
            super(urls(), parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (isChildFirst(name)) {
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) {
                        try {
                            loaded = findClass(name);
                        } catch (ClassNotFoundException exception) {
                            loaded = super.loadClass(name, false);
                        }
                    }
                    if (resolve) {
                        resolveClass(loaded);
                    }
                    return loaded;
                }
            }
            return super.loadClass(name, resolve);
        }

        private static boolean isChildFirst(String name) {
            for (String packageName : CHILD_FIRST_PACKAGES) {
                if (name.startsWith(packageName)) {
                    return true;
                }
            }
            return false;
        }

        private static URL[] urls() {
            CodeSource codeSource = YsmServiceBridge.class.getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) {
                return new URL[0];
            }
            return new URL[]{codeSource.getLocation()};
        }
    }
}
