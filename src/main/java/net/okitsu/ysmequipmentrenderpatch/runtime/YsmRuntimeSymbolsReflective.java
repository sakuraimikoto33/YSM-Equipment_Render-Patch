package net.okitsu.ysmequipmentrenderpatch.runtime;

import net.okitsu.ysmequipmentrenderpatch.YsmEquipmentRenderPatch;
import net.okitsu.ysmmapping.api.MappingSnapshot;
import net.okitsu.ysmmapping.api.YsmMappingApi;
import net.okitsu.ysmmapping.api.YsmMethodSymbol;
import net.okitsu.ysmmapping.api.YsmSymbolKey;
import net.okitsu.ysmmapping.api.YsmSymbols;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public final class YsmRuntimeSymbolsReflective {
    private static final List<YsmSymbolKey<?>> SYMBOLS = List.of(
            YsmSymbols.CUSTOM_PLAYER_ENTITY_GETTER,
            YsmSymbols.CUSTOM_PLAYER_CURRENT_MODEL_GETTER,
            YsmSymbols.ANIMATED_MODEL_RIGHT_WAIST_BONES_GETTER,
            YsmSymbols.RENDER_UTILS_PREP_MATRIX_FOR_LOCATOR,
            YsmSymbols.ANIMATED_MODEL_HEAD_BONES_GETTER
    );
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
        try {
            MappingSnapshot snapshot = YsmMappingApi.resolve(YsmEquipmentRenderPatch.MOD_ID, SYMBOLS);
            ClassLoader classLoader = YsmRuntimeSymbolsReflective.class.getClassLoader();
            ResolvedMethods methods = new ResolvedMethods(
                    Reflector.findMethod(snapshot.require(YsmSymbols.CUSTOM_PLAYER_ENTITY_GETTER), classLoader),
                    Reflector.findMethod(snapshot.require(YsmSymbols.CUSTOM_PLAYER_CURRENT_MODEL_GETTER), classLoader),
                    Reflector.findMethod(snapshot.require(YsmSymbols.ANIMATED_MODEL_RIGHT_WAIST_BONES_GETTER), classLoader),
                    Reflector.findMethod(snapshot.require(YsmSymbols.RENDER_UTILS_PREP_MATRIX_FOR_LOCATOR), classLoader),
                    Reflector.findMethod(optional(snapshot, YsmSymbols.ANIMATED_MODEL_HEAD_BONES_GETTER), classLoader)
            );
            return methods.isComplete() ? methods : ResolvedMethods.EMPTY;
        } catch (IOException | RuntimeException exception) {
            return ResolvedMethods.EMPTY;
        }
    }

    private static YsmMethodSymbol optional(
            MappingSnapshot snapshot,
            YsmSymbolKey<YsmMethodSymbol> key
    ) {
        try {
            return snapshot.require(key);
        } catch (IllegalStateException exception) {
            return null;
        }
    }

    public record ResolvedMethods(
            Method getEntity,
            Method getCurrentModel,
            Method rightWaistBones,
            Method prepMatrixForLocator,
            Method headBones
    ) {
        private static final ResolvedMethods EMPTY = new ResolvedMethods(null, null, null, null, null);

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
