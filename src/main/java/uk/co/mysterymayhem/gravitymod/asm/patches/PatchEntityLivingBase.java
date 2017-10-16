package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import uk.co.mysterymayhem.gravitymod.asm.Ref;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.InsnPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.MethodPatcher;

import java.util.ListIterator;

/**
 * Created by Mysteryem on 2017-02-02.
 */
public class PatchEntityLivingBase extends ClassPatcher {
    public PatchEntityLivingBase() {
        super("net.minecraft.entity.EntityLivingBase", 0, ClassWriter.COMPUTE_MAXS);

        // FIXME: Elytra flying doesn't work
        // I didn't write a comment for patching this method.
        // Go look at the comments for each patch added in its constructor
        this.addMethodPatch(new Travel());

        // This method is pretty badly named in my opinion, it seems to set up the turn of the player's head or body
        // (I'm not sure), however it's rendering based and the rotation needs to be made relative. Only the
        // rotationYaw field is accessed, so that is all that is changed
        this.addMethodPatch(Ref.EntityLivingBase$updateDistance_name::is,
                methodNode -> Transformer.patchMethodUsingAbsoluteRotations(methodNode, Transformer.GET_ROTATIONYAW));

        // For some of the vanilla code that also seems to be to do with head turning, the vanilla code uses the
        // change in X and Z pos from last tick to the current tick, as well as the player's rotationYaw.
        //
        // We replace these field accesses with hooks that get the relative equivalents to them
        this.addMethodPatch(Ref.Entity$onUpdate_name::is, methodNode -> {
            for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                AbstractInsnNode next = iterator.next();
                if (next instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                    if (Ref.EntityLivingBase$posX_GET.is(fieldInsnNode)) {
                        Ref.Hooks$getRelativePosX.replace(iterator);
                    }
                    else if (Ref.EntityLivingBase$prevPosX_GET.is(fieldInsnNode)) {
                        Ref.Hooks$getRelativePrevPosX.replace(iterator);
                    }
                    else if (Ref.EntityLivingBase$posZ_GET.is(fieldInsnNode)) {
                        Ref.Hooks$getRelativePosZ.replace(iterator);
                    }
                    else if (Ref.EntityLivingBase$prevPosZ_GET.is(fieldInsnNode)) {
                        Ref.Hooks$getRelativePrevPosZ.replace(iterator);
                    }
                    else if (Ref.EntityLivingBase$rotationYaw_GET.is(fieldInsnNode)) {
                        Ref.Hooks$getRelativeYaw.replace(iterator);
                        break;
                    }
                }
            }
        });

        // When sprinting, the player's rotationYaw is used to determine the boost in forwards movement to add
        // This needs to be made relative to match the direction the player is actually sprinting in
        this.addMethodPatch(
                Ref.EntityLivingBase$jump_name::is,
                methodNode -> Transformer.patchMethodUsingAbsoluteRotations(methodNode, Transformer.GET_ROTATIONYAW)
        );
    }

    private class Travel extends MethodPatcher {

        Travel() {

            //FIXME: Elyta flying doesn't work!
            // Replaces getLookVec in the Elytra movement with a relative version
            InsnPatcher replaceGetLookVec = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityLivingBase$getLookVec.is(node)) {
                    Ref.Hooks$getRelativeLookVec.replace(iterator);
                    return true;
                }
                return false;
            });

            //FIXME: Elyta flying doesn't work!
            // Replaces access of the player's pitch with a get-relative-pitch hook, in the Elytra movement
            InsnPatcher replaceRotationPitch = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityLivingBase$rotationPitch_GET.is(node)) {
                    Ref.Hooks$getRelativePitch.replace(iterator);
                    return true;
                }
                return false;
            });

            // In normal movement, a PooledMutableBlockPos is create 1.0 below the player's position. We
            // need to change it to be relative below.
            //
            // The original code that is called is removed
            InsnPatcher replaceBlockPos$PooledMutableBlockPos$retain = this.addInsnPatch((node, iterator) -> {
                if (Ref.BlockPos$PooledMutableBlockPos$retain.is(node)) {
                    Ref.Hooks$getBlockPostBelowEntity.replace(iterator);

                    iterator.previous(); //Our replacment INVOKESTATIC

                    // Deletes everything before the INVOKESTATIC until we've reached our third ALOAD
                    int aload0foundCount = 0;
                    while (iterator.hasPrevious() && aload0foundCount < 3) {
                        AbstractInsnNode previous = iterator.previous();
                        if (previous instanceof VarInsnNode) {
                            VarInsnNode varInsnNode = (VarInsnNode)previous;
                            if (varInsnNode.getOpcode() == Opcodes.ALOAD
                                    && varInsnNode.var == 0) {
                                aload0foundCount++;
                            }
                        }
                        if (aload0foundCount < 3) {
                            iterator.remove();
                        }
                    }
                    if (aload0foundCount == 3) {
                        return true;
                    }
                    else {
                        Transformer.die("Failed to find 3 uses of ALOAD 0 before inserted \"" + Ref.Hooks$getBlockPostBelowEntity + "\" in " + Ref.EntityLivingBase$travel_name);
                    }
                }
                return false;
            });

            // In normal movement, if the player is on the ground, the mutable BlockPos gets set back to 1.0
            // below the player, again, we set it to be relatively below the player
            InsnPatcher replaceBlockPos$PooledMutableBlockPos$setPos = this.addInsnPatch((node, iterator) -> {
                if (Ref.BlockPos$PooledMutableBlockPos$setPos.is(node)) {
                    Ref.Hooks$setPooledMutableBlockPosToBelowEntity.replace(iterator);

                    iterator.previous(); //Our replacement INVOKESTATIC

                    // Interestingly, we have to find the same number of ALOAD 0 instructions as the previous
                    // Deletes everything before the INVOKESTATIC until we've reached our third ALOAD
                    int aload0foundCount = 0;
                    while (iterator.hasPrevious() && aload0foundCount < 3) {
                        AbstractInsnNode previous = iterator.previous();
                        if (previous instanceof VarInsnNode) {
                            VarInsnNode varInsnNode = (VarInsnNode)previous;
                            if (varInsnNode.getOpcode() == Opcodes.ALOAD
                                    && varInsnNode.var == 0) {
                                aload0foundCount++;
                            }
                        }
                        if (aload0foundCount < 3) {
                            iterator.remove();
                        }
                    }
                    if (aload0foundCount == 3) {
                        return true;
                    }
                    else {
                        Transformer.die("Failed to find 3 uses of ALOAD 0 before inserted \"" + Ref.Hooks$setPooledMutableBlockPosToBelowEntity + "\" in " + Ref.EntityLivingBase$travel_name);
                    }
                }
                return false;
            });

            final String previousisOffsetPositionInLiquidMethodInsnNode = "previousisOffsetPositionInLiquidMethodInsnNode";

            // For these liquid calculations, the passed arguments are (motionX, motionY + change in posY, motionZ)
            // The motion fields are all relative, but the change in posY is absolute, so we need to get the change
            // in whatever direction positive Y is from the player's perspective. Both the initial posY and the
            // later posY are modified
            InsnPatcher replaceGetPosX = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityLivingBase$isOffsetPositionInLiquid.is(node)) {

                    PatchEntityLivingBase.this.storeData(previousisOffsetPositionInLiquidMethodInsnNode, node);
                    int numReplacementsMade = 0;
                    while (iterator.hasPrevious() && numReplacementsMade < 2) {
                        AbstractInsnNode previous = iterator.previous();
                        if (previous instanceof FieldInsnNode) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode)previous;
                            if (Ref.EntityLivingBase$posY_GET.is(fieldInsnNode)) {
                                // Replace the GETFIELD with an INVOKESTATIC of the Hook
                                Ref.Hooks$getRelativePosY.replace(iterator);
                                numReplacementsMade++;
                            }
                        }
                    }
                    if (numReplacementsMade == 2) {
                        // TODO: Try this code whilst removing the 'skip' patch that comes after this patch
//                        while (true) {
//                            if (iterator.next() == foundNode) {
//                                return true;
//                            }
//                        }
                        return true;
                    }
                    Transformer.die("ERRROR");
                }
                return false;
            });

            // We go backwards in the previous patch, such that we'll come across the same instruction
            InsnPatcher skipPreviouslyFoundIsOffsetPositionInLiquid = this.addInsnPatch(
                    (node, iterator) -> {
                        if (Ref.EntityLivingBase$isOffsetPositionInLiquid.is(node)
                                && PatchEntityLivingBase.this.getData(previousisOffsetPositionInLiquidMethodInsnNode) == node) {
                            iterator.next();
                            return true;
                        }
                        return false;
                    });

            InsnPatcher replaceGetPosX_Second = this.addInsnPatch(replaceGetPosX.copy());

            // Limb swing (arms+legs swinging as you walk around) is based on change in X and Z position
            // instead of messing with the existing code, we can insert a hook that changes the player's
            // position fields to act as if they're relative, so that when the vanilla code calculates
            // the change in X and Z position, it's instead the relative X and Z movement the player made
            InsnPatcher makePositionRelativeBeforeLimbSwing = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityLivingBase$limbSwingAmount_GET.is(node)) {
                    iterator.previous(); // The GETFIELD instruction
                    iterator.previous(); // ALOAD 0
                    Ref.Hooks$makePositionRelative.addTo(iterator);
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    return true;
                }
                return false;
            });

            // Once the limb swing has been set properly, we need to put the player's position fields back to
            // normal
            InsnPatcher makePositionNormalAgainAfterLimbSwing = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityLivingBase$limbSwing_PUT.is(node)) {
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    Ref.Hooks$makePositionAbsolute.addTo(iterator);
                    return true;
                }
                return false;
            });

            // Patch order
            InsnPatcher.sequentialOrder(
                    replaceGetLookVec,
                    replaceRotationPitch,
                    replaceBlockPos$PooledMutableBlockPos$retain,
                    replaceBlockPos$PooledMutableBlockPos$setPos,
                    replaceGetPosX,
                    skipPreviouslyFoundIsOffsetPositionInLiquid,
                    replaceGetPosX_Second,
                    makePositionRelativeBeforeLimbSwing,
                    makePositionNormalAgainAfterLimbSwing
            );
        }

        @Override
        protected boolean shouldPatchMethod(MethodNode methodNode) {
            return Ref.EntityLivingBase$travel_name.is(methodNode);
        }
    }
}
