package net.okitsu.ysmequipmentrenderpatch.runtime;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;
import java.nio.file.Path;

public final class YsmRuntimeSymbolsReflective {
    private static volatile ResolvedMethods resolvedMethods;

    private YsmRuntimeSymbolsReflective() {
    }

    public static ResolvedMethods methods() {
        ResolvedMethods local = resolvedMethods;
        if (local == null) {
            local = resolve();
            resolvedMethods = local;
        }
        return local;
    }

    private static ResolvedMethods resolve() {
        Path cachePath = YsmRuntimeSymbolCache.cachePath(Minecraft.getInstance().gameDirectory.toPath());
        YsmRuntimeSymbols symbols = YsmRuntimeSymbolCache.read(cachePath).orElse(null);
        if (symbols == null || !symbols.isComplete()) {
            return ResolvedMethods.EMPTY;
        }

        ClassLoader classLoader = YsmRuntimeSymbolsReflective.class.getClassLoader();
        ResolvedMethods methods = new ResolvedMethods(
                Reflector.findMethod(symbols.customPlayerGetEntity, classLoader),
                Reflector.findMethod(symbols.customPlayerGetCurrentModel, classLoader),
                Reflector.findMethod(symbols.animatedModelRightWaistBones, classLoader),
                Reflector.findMethod(symbols.prepMatrixForLocator, classLoader),
                Reflector.findMethod(symbols.animatedModelHeadBones, classLoader),
                Reflector.findMethod(symbols.animatedModelAllHeadBone, classLoader),
                Reflector.findMethod(symbols.prepMatrixForBone, classLoader)
        );
        return methods.isComplete() ? methods : ResolvedMethods.EMPTY;
    }

    public record ResolvedMethods(
            Method getEntity,
            Method getCurrentModel,
            Method rightWaistBones,
            Method prepMatrixForLocator,
            Method headBones,
            Method allHeadBone,
            Method prepMatrixForBone
    ) {
        private static final ResolvedMethods EMPTY = new ResolvedMethods(null, null, null, null, null, null, null);

        public boolean isComplete() {
            return this.getEntity != null
                    && this.getCurrentModel != null
                    && this.rightWaistBones != null
                    && this.prepMatrixForLocator != null;
        }

        public boolean hasHeadDollSupport() {
            return isComplete()
                    && this.headBones != null;
        }
    }
}
