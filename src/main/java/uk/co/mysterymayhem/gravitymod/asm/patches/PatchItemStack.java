package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;

import java.util.ListIterator;
import java.util.function.Function;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class PatchItemStack implements Function<byte[], byte[]> {
    // Upon using items, we generally need to ensure the player has absolute motion fields, while there are events that
    // are fired before items are used, there aren't any events fired immediately after the item is used. I need a way
    // to put the player's rotation fields back to normal after the item has finished processing, so I've added what is
    // effectively some events around the item use methods in the ItemStack class.
    @Override
    public byte[] apply(byte[] bytes) {
        int methodPatches = 0;
        final int expectedMethodPatches = 3;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            if (Transformer.ItemStack$onItemUse_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.ItemStack$onItemUse_name);

                int insertedLocalIndex = Transformer.addLocalVar(methodNode, Transformer.ItemStackAndBoolean);

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (Transformer.Item$onItemUse.is(next)) {
                        iterator.previous();
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // this, EntityPlayer
                        Transformer.Hooks$onItemUsePre.addTo(iterator);
                        iterator.add(new VarInsnNode(Opcodes.ASTORE, insertedLocalIndex));
                        iterator.next(); // Item$onItemUse
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, insertedLocalIndex)); // origItemStackCopy
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // this, EntityPlayer
                        Transformer.Hooks$onItemUsePost.addTo(iterator);
                        methodPatches++;
                        Transformer.logPatchComplete(Transformer.ItemStack$onItemUse_name);
                        break;
                    }
                }
            }
            else if (Transformer.ItemStack$useItemRightClick_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.ItemStack$useItemRightClick_name);

                int insertedLocalIndex = Transformer.addLocalVar(methodNode, Transformer.ItemStackAndBoolean);

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (Transformer.Item$onItemRightClick.is(next)) {
                        iterator.previous();
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityPlayer
                        Transformer.Hooks$onItemRightClickPre.addTo(iterator);
                        iterator.add(new VarInsnNode(Opcodes.ASTORE, insertedLocalIndex));
                        iterator.next(); // Item$onItemRightClick
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, insertedLocalIndex)); // origItemStackCopy
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityPlayer
                        Transformer.Hooks$onItemRightClickPost.addTo(iterator);
                        methodPatches++;
                        Transformer.logPatchComplete(Transformer.ItemStack$useItemRightClick_name);
                        break;
                    }
                }
            }
            else if (Transformer.ItemStack$onPlayerStoppedUsing_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.ItemStack$onPlayerStoppedUsing_name);

                int insertedLocalIndex = Transformer.addLocalVar(methodNode, Transformer.ItemStackAndBoolean);

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (Transformer.Item$onPlayerStoppedUsing.is(next)) {
                        iterator.previous();
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityLivingBase
                        Transformer.Hooks$onPlayerStoppedUsingPre.addTo(iterator);
                        iterator.add(new VarInsnNode(Opcodes.ASTORE, insertedLocalIndex));
                        iterator.next(); // Item$onPlayerStoppedUsing
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, insertedLocalIndex)); // origItemStackCopy
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityLivingBase
                        Transformer.Hooks$onPlayerStoppedUsingPost.addTo(iterator);
                        methodPatches++;
                        Transformer.logPatchComplete(Transformer.ItemStack$onPlayerStoppedUsing_name);
                        break;
                    }
                }
            }
            if (methodPatches == 3) {
                break;
            }
        }

        Transformer.dieIfFalse(methodPatches == expectedMethodPatches, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
