package net.okitsu.ysmequipmentrenderpatch.launch;

import net.okitsu.ysmequipmentrenderpatch.runtime.YsmRuntimeSymbols;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class YsmSymbolAnalyzer implements Opcodes {
    private static final String YSM_PACKAGE = "com/elfmcys/yesstevemodel/";
    private static final String LIVING_ENTITY_DESC = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String ITEM_STACK_DESC = "Lnet/minecraft/world/item/ItemStack;";
    private static final String POSE_STACK_DESC = "Lcom/mojang/blaze3d/vertex/PoseStack;";
    private static final String MULTI_BUFFER_SOURCE_DESC = "Lnet/minecraft/client/renderer/MultiBufferSource;";
    private static final String INT_LIST_DESC = "Lit/unimi/dsi/fastutil/ints/IntList;";
    private static final String LIST_DESC = "Ljava/util/List;";
    private static final int RIGHT_WAIST_LOCATOR_INDEX = 6;

    private YsmSymbolAnalyzer() {
    }

    static YsmRuntimeSymbols analyze(Path jarPath, String ysmVersion, String jarSha256) throws IOException {
        Map<String, ClassNode> classes = readClasses(jarPath);
        YsmRuntimeSymbols.MethodRef elytraLookup = findElytraLookup(classes)
                .orElseThrow(() -> new IllegalStateException("Could not find YSM Elytra lookup method"));
        YsmRuntimeSymbols.MethodRef elytraRender = findElytraRender(classes, elytraLookup)
                .orElseThrow(() -> new IllegalStateException("Could not find YSM Elytra render layer method"));

        MethodNode renderMethod = method(classes, elytraRender);
        String customPlayerClass = Type.getArgumentTypes(elytraRender.descriptor)[3].getInternalName();
        YsmRuntimeSymbols.MethodRef getEntity = findCustomPlayerEntityGetter(renderMethod, customPlayerClass)
                .orElseThrow(() -> new IllegalStateException("Could not find YSM custom player entity getter"));
        YsmRuntimeSymbols.MethodRef getCurrentModel = findCustomPlayerModelGetter(classes, renderMethod, customPlayerClass)
                .orElseThrow(() -> new IllegalStateException("Could not find YSM current model getter"));
        String animatedModelClass = Type.getReturnType(getCurrentModel.descriptor).getInternalName();
        YsmRuntimeSymbols.MethodRef rightWaistGetter = findRightWaistGetter(classes, animatedModelClass)
                .orElseThrow(() -> new IllegalStateException("Could not find YSM right waist locator getter"));
        YsmRuntimeSymbols.MethodRef prepMatrix = findPrepMatrixForLocator(classes)
                .orElseThrow(() -> new IllegalStateException("Could not find YSM locator matrix helper"));

        return new YsmRuntimeSymbols(
                ysmVersion,
                jarSha256,
                elytraLookup,
                elytraRender,
                getEntity,
                getCurrentModel,
                rightWaistGetter,
                prepMatrix
        );
    }

    private static Map<String, ClassNode> readClasses(Path jarPath) throws IOException {
        Map<String, ClassNode> classes = new HashMap<>();
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(YSM_PACKAGE) || !entry.getName().endsWith(".class")) {
                    continue;
                }

                try (InputStream inputStream = jarFile.getInputStream(entry)) {
                    ClassReader reader = new ClassReader(inputStream);
                    ClassNode classNode = new ClassNode();
                    reader.accept(classNode, ClassReader.SKIP_DEBUG);
                    classes.put(classNode.name, classNode);
                }
            }
        }
        return classes;
    }

    private static Optional<YsmRuntimeSymbols.MethodRef> findElytraLookup(Map<String, ClassNode> classes) {
        String descriptor = "(" + LIVING_ENTITY_DESC + ")" + ITEM_STACK_DESC;
        for (ClassNode classNode : classes.values()) {
            for (MethodNode method : classNode.methods) {
                if ((method.access & ACC_STATIC) == 0 || !method.desc.equals(descriptor)) {
                    continue;
                }
                if (referencesField(method, "net/minecraft/world/item/Items", "ELYTRA")
                        && referencesField(method, "net/minecraft/world/entity/EquipmentSlot", "CHEST")
                        && referencesField(method, "net/minecraft/world/item/ItemStack", "EMPTY")) {
                    return Optional.of(methodRef(classNode, method));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<YsmRuntimeSymbols.MethodRef> findElytraRender(
            Map<String, ClassNode> classes,
            YsmRuntimeSymbols.MethodRef elytraLookup
    ) {
        for (ClassNode classNode : classes.values()) {
            for (MethodNode method : classNode.methods) {
                if (!isYsmRenderMethod(method)) {
                    continue;
                }
                if (calls(method, elytraLookup)
                        && referencesMethodOwner(method, "net/minecraft/client/model/ElytraModel")
                        && referencesString(classNode, "textures/entity/elytra.png")) {
                    return Optional.of(methodRef(classNode, method));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isYsmRenderMethod(MethodNode method) {
        Type[] arguments = Type.getArgumentTypes(method.desc);
        if (Type.getReturnType(method.desc).getSort() != Type.VOID || arguments.length != 10) {
            return false;
        }
        if (!arguments[0].getDescriptor().equals(POSE_STACK_DESC)
                || !arguments[1].getDescriptor().equals(MULTI_BUFFER_SOURCE_DESC)
                || arguments[2].getSort() != Type.INT
                || arguments[3].getSort() != Type.OBJECT
                || !arguments[3].getInternalName().startsWith(YSM_PACKAGE)) {
            return false;
        }
        for (int index = 4; index < arguments.length; index++) {
            if (arguments[index].getSort() != Type.FLOAT) {
                return false;
            }
        }
        return true;
    }

    private static Optional<YsmRuntimeSymbols.MethodRef> findCustomPlayerEntityGetter(
            MethodNode renderMethod,
            String customPlayerClass
    ) {
        for (AbstractInsnNode instruction : renderMethod.instructions) {
            if (instruction instanceof MethodInsnNode methodInsn
                    && methodInsn.owner.equals(customPlayerClass)
                    && methodInsn.desc.startsWith("()")) {
                Type returnType = Type.getReturnType(methodInsn.desc);
                if (returnType.getSort() == Type.OBJECT
                        && ("net/minecraft/world/entity/Entity".equals(returnType.getInternalName())
                        || "net/minecraft/world/entity/LivingEntity".equals(returnType.getInternalName())
                        || "net/minecraft/world/entity/player/Player".equals(returnType.getInternalName()))) {
                    return Optional.of(new YsmRuntimeSymbols.MethodRef(methodInsn.owner, methodInsn.name, methodInsn.desc));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<YsmRuntimeSymbols.MethodRef> findCustomPlayerModelGetter(
            Map<String, ClassNode> classes,
            MethodNode renderMethod,
            String customPlayerClass
    ) {
        for (AbstractInsnNode instruction : renderMethod.instructions) {
            if (instruction instanceof MethodInsnNode methodInsn
                    && methodInsn.owner.equals(customPlayerClass)
                    && methodInsn.desc.startsWith("()")) {
                Type returnType = Type.getReturnType(methodInsn.desc);
                if (returnType.getSort() == Type.OBJECT
                        && looksLikeAnimatedModel(classes.get(returnType.getInternalName()))) {
                    return Optional.of(new YsmRuntimeSymbols.MethodRef(methodInsn.owner, methodInsn.name, methodInsn.desc));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean looksLikeAnimatedModel(ClassNode classNode) {
        if (classNode == null) {
            return false;
        }

        int listGetterCount = 0;
        boolean hasModelDataConstructor = false;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("<init>")
                    && Type.getArgumentTypes(method.desc).length == 1
                    && Type.getArgumentTypes(method.desc)[0].getSort() == Type.OBJECT
                    && Type.getArgumentTypes(method.desc)[0].getInternalName().startsWith(YSM_PACKAGE)) {
                hasModelDataConstructor = true;
            }
            if (method.desc.equals("()" + LIST_DESC)) {
                listGetterCount++;
            }
        }
        return hasModelDataConstructor && listGetterCount >= 8;
    }

    private static Optional<YsmRuntimeSymbols.MethodRef> findRightWaistGetter(
            Map<String, ClassNode> classes,
            String animatedModelClass
    ) {
        ClassNode animatedModel = classes.get(animatedModelClass);
        if (animatedModel == null) {
            return Optional.empty();
        }

        String modelDataClass = findModelDataClass(animatedModel).orElse(null);
        if (modelDataClass == null) {
            return Optional.empty();
        }

        String modelDataWaistField = findModelDataLocatorField(classes.get(modelDataClass), RIGHT_WAIST_LOCATOR_INDEX)
                .orElse(null);
        if (modelDataWaistField == null) {
            return Optional.empty();
        }

        String animatedWaistField = findAnimatedLocatorField(animatedModel, modelDataClass, modelDataWaistField)
                .orElse(null);
        if (animatedWaistField == null) {
            return Optional.empty();
        }

        return findGetterForField(animatedModel, animatedWaistField)
                .map(method -> methodRef(animatedModel, method));
    }

    private static Optional<String> findModelDataClass(ClassNode animatedModel) {
        for (MethodNode method : animatedModel.methods) {
            if (!method.name.equals("<init>")) {
                continue;
            }
            Type[] arguments = Type.getArgumentTypes(method.desc);
            if (arguments.length == 1
                    && arguments[0].getSort() == Type.OBJECT
                    && arguments[0].getInternalName().startsWith(YSM_PACKAGE)) {
                return Optional.of(arguments[0].getInternalName());
            }
        }
        return Optional.empty();
    }

    private static Optional<String> findModelDataLocatorField(ClassNode modelData, int locatorIndex) {
        if (modelData == null) {
            return Optional.empty();
        }

        for (MethodNode method : modelData.methods) {
            if (!method.name.equals("<init>") || !method.desc.contains("[[Ljava/lang/String;")) {
                continue;
            }

            AbstractInsnNode[] instructions = method.instructions.toArray();
            for (int index = 0; index < instructions.length; index++) {
                if (instructions[index] instanceof FieldInsnNode fieldInsn
                        && fieldInsn.getOpcode() == PUTFIELD
                        && fieldInsn.owner.equals(modelData.name)
                        && fieldInsn.desc.equals(INT_LIST_DESC)
                        && previousWindowContainsInt(instructions, index, locatorIndex)
                        && previousWindowContainsOpcode(instructions, index, AALOAD)) {
                    return Optional.of(fieldInsn.name);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<String> findAnimatedLocatorField(
            ClassNode animatedModel,
            String modelDataClass,
            String modelDataWaistField
    ) {
        for (MethodNode method : animatedModel.methods) {
            if (!method.name.equals("<init>")) {
                continue;
            }

            AbstractInsnNode[] instructions = method.instructions.toArray();
            for (int index = 0; index < instructions.length; index++) {
                if (!(instructions[index] instanceof FieldInsnNode putField)
                        || putField.getOpcode() != PUTFIELD
                        || !putField.owner.equals(animatedModel.name)
                        || !putField.desc.equals(LIST_DESC)) {
                    continue;
                }

                for (int previous = Math.max(0, index - 10); previous < index; previous++) {
                    if (instructions[previous] instanceof FieldInsnNode getField
                            && getField.getOpcode() == GETFIELD
                            && getField.owner.equals(modelDataClass)
                            && getField.name.equals(modelDataWaistField)
                            && getField.desc.equals(INT_LIST_DESC)) {
                        return Optional.of(putField.name);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<MethodNode> findGetterForField(ClassNode classNode, String fieldName) {
        for (MethodNode method : classNode.methods) {
            if (!method.desc.equals("()" + LIST_DESC)) {
                continue;
            }
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof FieldInsnNode fieldInsn
                        && fieldInsn.getOpcode() == GETFIELD
                        && fieldInsn.owner.equals(classNode.name)
                        && fieldInsn.name.equals(fieldName)
                        && fieldInsn.desc.equals(LIST_DESC)) {
                    return Optional.of(method);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<YsmRuntimeSymbols.MethodRef> findPrepMatrixForLocator(Map<String, ClassNode> classes) {
        String descriptor = "(" + POSE_STACK_DESC + LIST_DESC + ")Z";
        for (ClassNode classNode : classes.values()) {
            for (MethodNode method : classNode.methods) {
                if ((method.access & ACC_STATIC) != 0 && method.desc.equals(descriptor)) {
                    return Optional.of(methodRef(classNode, method));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean previousWindowContainsInt(AbstractInsnNode[] instructions, int endExclusive, int value) {
        for (int index = Math.max(0, endExclusive - 12); index < endExclusive; index++) {
            Integer instructionValue = intValue(instructions[index]);
            if (instructionValue != null && instructionValue == value) {
                return true;
            }
        }
        return false;
    }

    private static boolean previousWindowContainsOpcode(AbstractInsnNode[] instructions, int endExclusive, int opcode) {
        for (int index = Math.max(0, endExclusive - 12); index < endExclusive; index++) {
            if (instructions[index].getOpcode() == opcode) {
                return true;
            }
        }
        return false;
    }

    private static Integer intValue(AbstractInsnNode instruction) {
        return switch (instruction.getOpcode()) {
            case ICONST_M1 -> -1;
            case ICONST_0 -> 0;
            case ICONST_1 -> 1;
            case ICONST_2 -> 2;
            case ICONST_3 -> 3;
            case ICONST_4 -> 4;
            case ICONST_5 -> 5;
            case BIPUSH, SIPUSH -> ((IntInsnNode) instruction).operand;
            case LDC -> instruction instanceof LdcInsnNode ldcInsn && ldcInsn.cst instanceof Integer value ? value : null;
            default -> null;
        };
    }

    private static boolean calls(MethodNode method, YsmRuntimeSymbols.MethodRef methodRef) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode methodInsn
                    && methodInsn.owner.equals(methodRef.owner)
                    && methodInsn.name.equals(methodRef.name)
                    && methodInsn.desc.equals(methodRef.descriptor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean referencesMethodOwner(MethodNode method, String owner) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode methodInsn && methodInsn.owner.equals(owner)) {
                return true;
            }
        }
        return false;
    }

    private static boolean referencesField(MethodNode method, String owner, String name) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof FieldInsnNode fieldInsn
                    && fieldInsn.owner.equals(owner)
                    && fieldInsn.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean referencesString(ClassNode classNode, String value) {
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof LdcInsnNode ldcInsn && value.equals(ldcInsn.cst)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static MethodNode method(Map<String, ClassNode> classes, YsmRuntimeSymbols.MethodRef methodRef) {
        ClassNode classNode = classes.get(methodRef.owner);
        if (classNode == null) {
            throw new IllegalStateException("Missing owner class: " + methodRef.owner);
        }
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(methodRef.name) && method.desc.equals(methodRef.descriptor)) {
                return method;
            }
        }
        throw new IllegalStateException("Missing method: " + methodRef.owner + "." + methodRef.name + methodRef.descriptor);
    }

    private static YsmRuntimeSymbols.MethodRef methodRef(ClassNode classNode, MethodNode method) {
        return new YsmRuntimeSymbols.MethodRef(classNode.name, method.name, method.desc);
    }
}
