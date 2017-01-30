package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;

import java.util.ListIterator;
import java.util.function.Function;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class PatchEntityRenderer implements Function<byte[], byte[]> {

    @Override
    public byte[] apply(byte[] bytes) {
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            // Minecraft checks if the player is looking at an entity by expanding their bounding box and then effectively
            // raytracing those found entities (I think it just does some angle and distance calculations).
            //
            // The issue is that the player's bounding box is a GravityAxisAlignedBB.
            //
            // The vanilla method gets the player's look vector and then adds it's x, y and z coordinates (multiplied by a scalar)
            // to the player's bounding box. The player's look is absolute, but the player's bounding box moves in a relative fashion.
            //
            // Either we have to get the relative look of the player or we have to get a bounding box that doesn't move in a relative fashion.
            //
            // I have chosen to insert a hook that calls the instance method GravityAxisAlignedBB::toVanilla if the bounding box in question
            // is a GravityAxisAlignedBB. The hook returns a vanilla AxisAlignedBB with the same shape/position as the existing GravityAxisAlignedBB.
            // This vanilla AxisAlignedBB will then move in an absolute fashion
            if (Transformer.EntityRenderer$getMouseOver_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.EntityRenderer$getMouseOver_name);
                outerfor:
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    // This is a pretty unique method call, so we'll use it as the basis for finding the bytecode to patch
                    if (Transformer.WorldClient$getEntitiesInAABBexcluding.is(next)) {
                        // Not strictly necessary, but the first previous() call will return the getEntitiesInAABBexcluding method we just found
                        iterator.previous();
                        while (iterator.hasPrevious()) {
                            next = iterator.previous();
                            if (Transformer.Entity$getEntityBoundingBox.is(next)) {
                                Transformer.Hooks$getVanillaEntityBoundingBox.replace(iterator);
                                Transformer.logPatchComplete(Transformer.EntityRenderer$getMouseOver_name);
                                methodPatches++;
                                break outerfor;
                            }
                        }
                        Transformer.die("Failed to find " + Transformer.WorldClient$getEntitiesInAABBexcluding + " in " + classNode.name + "::" + Transformer.EntityRenderer$getMouseOver_name);
                    }
                }
            }
            else if (Transformer.EntityRenderer$drawNameplate_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.EntityRenderer$drawNameplate_name);
                boolean patchComplete = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (Transformer.GlStateManager$disableLighting_name.is(methodInsnNode.name)) {
                            iterator.previous();
                            // boolean isThirdPersonFrontal argument, '8' should be consistent unless the method's description changes
                            iterator.add(new VarInsnNode(Opcodes.ILOAD, 8));
                            Transformer.Hooks$runNameplateCorrection.addTo(iterator);
                            Transformer.logPatchComplete(Transformer.EntityRenderer$drawNameplate_name);
                            patchComplete = true;
                            methodPatches++;
                            break;
                        }
                    }
                }
                Transformer.dieIfFalse(patchComplete, "Failed to find " + Transformer.GlStateManager$disableLighting_name + " in " + classNode.name + "::" + Transformer.EntityRenderer$drawNameplate_name);
            }
        }

        Transformer.dieIfFalse(methodPatches == 2, classNode);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
