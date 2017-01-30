package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;

import java.util.ListIterator;
import java.util.function.Function;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class PatchNetHandlerPlayClient implements Function<byte[], byte[]> {

    @Override
    public byte[] apply(byte[] bytes) {
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);


        for (MethodNode methodNode : classNode.methods) {
            // Mojang seems to assume that the bottom of a player's bounding box is equivalent to a player's posY a lot
            // of the time, which is pretty wrong in my opinion (and breaks things when players have non-standard bounding
            // boxes). So I replaced the access of minY of the player's bounding box with
            // their Y position
            if (Transformer.INetHandlerPlayClient$handlePlayerPosLook_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.INetHandlerPlayClient$handlePlayerPosLook_name);
                boolean foundAtLeastOneMinY = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (Transformer.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
                            Transformer.EntityPlayer$posY_GET.replace(iterator);
                            iterator.previous(); // the instruction we just replaced the old one with
                            iterator.previous(); // getEntityBoundingBox()
                            iterator.remove();
                            foundAtLeastOneMinY = true;
                        }
                    }
                }
                Transformer.dieIfFalse(foundAtLeastOneMinY, "Could not find \"playerEntity.geEntityBoundingBox().minY\" in " + Transformer.INetHandlerPlayClient$handlePlayerPosLook_name);
                Transformer.logPatchComplete(Transformer.INetHandlerPlayClient$handlePlayerPosLook_name);
                methodPatches++;
            }
        }

        Transformer.dieIfFalse(methodPatches == 1, classNode);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
