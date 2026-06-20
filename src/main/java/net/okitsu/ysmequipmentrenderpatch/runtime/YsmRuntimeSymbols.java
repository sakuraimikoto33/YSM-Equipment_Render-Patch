package net.okitsu.ysmequipmentrenderpatch.runtime;

public final class YsmRuntimeSymbols {
    public static final int SCHEMA_VERSION = 1;
    public static final int ANALYZER_VERSION = 1;

    public int schemaVersion;
    public int analyzerVersion;
    public String ysmVersion;
    public String ysmJarSha256;
    public MethodRef elytraLookup;
    public MethodRef elytraRender;
    public MethodRef customPlayerGetEntity;
    public MethodRef customPlayerGetCurrentModel;
    public MethodRef animatedModelRightWaistBones;
    public MethodRef prepMatrixForLocator;
    public MethodRef animatedModelHeadBones;
    public MethodRef animatedModelAllHeadBone;
    public MethodRef prepMatrixForBone;

    public YsmRuntimeSymbols() {
    }

    public YsmRuntimeSymbols(
            String ysmVersion,
            String ysmJarSha256,
            MethodRef elytraLookup,
            MethodRef elytraRender,
            MethodRef customPlayerGetEntity,
            MethodRef customPlayerGetCurrentModel,
            MethodRef animatedModelRightWaistBones,
            MethodRef prepMatrixForLocator,
            MethodRef animatedModelHeadBones,
            MethodRef animatedModelAllHeadBone,
            MethodRef prepMatrixForBone
    ) {
        this.schemaVersion = SCHEMA_VERSION;
        this.analyzerVersion = ANALYZER_VERSION;
        this.ysmVersion = ysmVersion;
        this.ysmJarSha256 = ysmJarSha256;
        this.elytraLookup = elytraLookup;
        this.elytraRender = elytraRender;
        this.customPlayerGetEntity = customPlayerGetEntity;
        this.customPlayerGetCurrentModel = customPlayerGetCurrentModel;
        this.animatedModelRightWaistBones = animatedModelRightWaistBones;
        this.prepMatrixForLocator = prepMatrixForLocator;
        this.animatedModelHeadBones = animatedModelHeadBones;
        this.animatedModelAllHeadBone = animatedModelAllHeadBone;
        this.prepMatrixForBone = prepMatrixForBone;
    }

    public boolean matches(String expectedYsmVersion, String expectedJarSha256) {
        return this.schemaVersion == SCHEMA_VERSION
                && this.analyzerVersion == ANALYZER_VERSION
                && expectedYsmVersion.equals(this.ysmVersion)
                && expectedJarSha256.equals(this.ysmJarSha256)
                && isComplete();
    }

    public boolean isComplete() {
        return isComplete(this.elytraLookup)
                && isComplete(this.elytraRender)
                && isComplete(this.customPlayerGetEntity)
                && isComplete(this.customPlayerGetCurrentModel)
                && isComplete(this.animatedModelRightWaistBones)
                && isComplete(this.prepMatrixForLocator);
    }

    public boolean hasHeadDollSupportSymbols() {
        return isComplete(this.animatedModelHeadBones);
    }

    private static boolean isComplete(MethodRef methodRef) {
        return methodRef != null
                && methodRef.owner != null
                && methodRef.name != null
                && methodRef.descriptor != null
                && !methodRef.owner.isBlank()
                && !methodRef.name.isBlank()
                && !methodRef.descriptor.isBlank();
    }

    public static final class MethodRef {
        public String owner;
        public String name;
        public String descriptor;

        public MethodRef() {
        }

        public MethodRef(String owner, String name, String descriptor) {
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
        }

        public String ownerClassName() {
            return this.owner.replace('/', '.');
        }
    }
}
