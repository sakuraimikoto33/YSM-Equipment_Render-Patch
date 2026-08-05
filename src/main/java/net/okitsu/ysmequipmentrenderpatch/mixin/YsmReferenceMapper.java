package net.okitsu.ysmequipmentrenderpatch.mixin;

import net.okitsu.ysmmapping.api.mixin.YsmMappingReferenceMapper;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;

public final class YsmReferenceMapper extends YsmMappingReferenceMapper {
    public YsmReferenceMapper(MixinEnvironment environment, IReferenceMapper delegate) {
        super(environment, delegate);
    }
}
