package net.okitsu.ysmequipmentrenderpatch.launch;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.okitsu.ysmequipmentrenderpatch.runtime.YsmRuntimeSymbols;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.HashSet;
import java.util.Set;

final class YsmEquipmentTransformer implements ITransformer<ClassNode>, Opcodes {
    private static final String SERVICE_BRIDGE_OWNER = "net/okitsu/ysmequipmentrenderpatch/launch/YsmServiceBridge";
    private static final String ITEM_STACK_OWNER = "net/minecraft/world/item/ItemStack";
    private static final String ITEM_STACK_DESC = "Lnet/minecraft/world/item/ItemStack;";

    private final YsmRuntimeSymbols symbols;

    YsmEquipmentTransformer(YsmRuntimeSymbols symbols) {
        this.symbols = symbols;
    }

    @Override
    public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        if (input.name.equals(this.symbols.elytraLookup.owner)) {
            patchElytraLookup(input);
        }
        if (input.name.equals(this.symbols.elytraRender.owner)) {
            patchRenderLayer(input);
        }
        return input;
    }

    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public Set<Target<ClassNode>> targets() {
        Set<Target<ClassNode>> targets = new HashSet<>();
        targets.add(Target.targetClass(this.symbols.elytraLookup.owner.replace('/', '.')));
        targets.add(Target.targetClass(this.symbols.elytraRender.owner.replace('/', '.')));
        return targets;
    }

    @Override
    public TargetType<ClassNode> getTargetType() {
        return TargetType.CLASS;
    }

    private void patchElytraLookup(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(this.symbols.elytraLookup.name)
                    && method.desc.equals(this.symbols.elytraLookup.descriptor)) {
                insertEquipmentElytraOverride(method);
                replaceHiddenCuriosReturns(method);
            }
        }
    }

    private static void insertEquipmentElytraOverride(MethodNode method) {
        int bridgeResultLocal = method.maxLocals++;
        LabelNode continueOriginal = new LabelNode();
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new MethodInsnNode(
                INVOKESTATIC,
                SERVICE_BRIDGE_OWNER,
                "findElytra",
                "(Ljava/lang/Object;)Ljava/lang/Object;",
                false
        ));
        instructions.add(new VarInsnNode(ASTORE, bridgeResultLocal));
        instructions.add(new VarInsnNode(ALOAD, bridgeResultLocal));
        instructions.add(new JumpInsnNode(IFNULL, continueOriginal));
        instructions.add(new VarInsnNode(ALOAD, bridgeResultLocal));
        instructions.add(new TypeInsnNode(CHECKCAST, ITEM_STACK_OWNER));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, ITEM_STACK_OWNER, "isEmpty", "()Z", false));
        instructions.add(new JumpInsnNode(IFNE, continueOriginal));
        instructions.add(new VarInsnNode(ALOAD, bridgeResultLocal));
        instructions.add(new TypeInsnNode(CHECKCAST, ITEM_STACK_OWNER));
        instructions.add(new InsnNode(ARETURN));
        instructions.add(continueOriginal);
        method.instructions.insert(instructions);
    }

    private static void replaceHiddenCuriosReturns(MethodNode method) {
        int returnLocal = method.maxLocals++;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() != ARETURN) {
                continue;
            }

            LabelNode keepOriginal = new LabelNode();
            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(ASTORE, returnLocal));
            replacement.add(new VarInsnNode(ALOAD, 0));
            replacement.add(new VarInsnNode(ALOAD, returnLocal));
            replacement.add(new MethodInsnNode(
                    INVOKESTATIC,
                    SERVICE_BRIDGE_OWNER,
                    "isHiddenCuriosElytra",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                    false
            ));
            replacement.add(new JumpInsnNode(IFEQ, keepOriginal));
            replacement.add(new FieldInsnNode(GETSTATIC, ITEM_STACK_OWNER, "EMPTY", ITEM_STACK_DESC));
            replacement.add(new InsnNode(ARETURN));
            replacement.add(keepOriginal);
            replacement.add(new VarInsnNode(ALOAD, returnLocal));
            replacement.add(new InsnNode(ARETURN));

            method.instructions.insert(instruction, replacement);
            method.instructions.remove(instruction);
        }
    }

    private void patchRenderLayer(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(this.symbols.elytraRender.name)
                    && method.desc.equals(this.symbols.elytraRender.descriptor)) {
                insertLanternRenderer(method);
            }
        }
    }

    private static void insertLanternRenderer(MethodNode method) {
        int poseStackLocal = argumentLocal(method, 0);
        int bufferSourceLocal = argumentLocal(method, 1);
        int packedLightLocal = argumentLocal(method, 2);
        int customPlayerLocal = argumentLocal(method, 3);
        int partialTickLocal = argumentLocal(method, 6);

        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() != RETURN) {
                continue;
            }

            InsnList injected = new InsnList();
            injected.add(new VarInsnNode(ALOAD, poseStackLocal));
            injected.add(new VarInsnNode(ALOAD, bufferSourceLocal));
            injected.add(new VarInsnNode(ILOAD, packedLightLocal));
            injected.add(new VarInsnNode(ALOAD, customPlayerLocal));
            injected.add(new VarInsnNode(FLOAD, partialTickLocal));
            injected.add(new MethodInsnNode(
                    INVOKESTATIC,
                    SERVICE_BRIDGE_OWNER,
                    "renderLantern",
                    "(Ljava/lang/Object;Ljava/lang/Object;ILjava/lang/Object;F)V",
                    false
            ));
            method.instructions.insertBefore(instruction, injected);
        }
    }

    private static int argumentLocal(MethodNode method, int argumentIndex) {
        int local = (method.access & ACC_STATIC) == 0 ? 1 : 0;
        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        for (int index = 0; index < argumentIndex; index++) {
            local += argumentTypes[index].getSize();
        }
        return local;
    }
}
