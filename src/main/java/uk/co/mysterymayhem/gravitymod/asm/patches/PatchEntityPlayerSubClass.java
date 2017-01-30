package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;

import java.util.ListIterator;
import java.util.function.Function;

/**
 * Inserts UpAndDown's EntityPlayerWithGravity class into EntityPlayerMP's and AbstractClientPlayer's hierarchy.
 * This is done to significantly lower the amount of ASM required and make it easier to code and debug the mod.
 *
 * Created by Mysteryem on 2017-01-30.
 */
public class PatchEntityPlayerSubClass implements Function<byte[], byte[]> {

    @Override
    public byte[] apply(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        classNode.superName = Transformer.classReplacement;

        for (MethodNode methodNode : classNode.methods) {
            for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                AbstractInsnNode next = iterator.next();
                switch (next.getType()) {
                    case (AbstractInsnNode.TYPE_INSN):
                        TypeInsnNode typeInsnNode = (TypeInsnNode)next;
                        if (typeInsnNode.desc.equals(Transformer.classToReplace)) {
                            typeInsnNode.desc = Transformer.classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.FIELD_INSN):
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (fieldInsnNode.owner.equals(Transformer.classToReplace)) {
                            fieldInsnNode.owner = Transformer.classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.METHOD_INSN):
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (methodInsnNode.owner.equals(Transformer.classToReplace)) {
                            methodInsnNode.owner = Transformer.classReplacement;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        Transformer.log("Injected super class into " + classNode.name);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
