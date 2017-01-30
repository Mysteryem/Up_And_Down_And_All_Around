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
public class PatchEntityLivingBase implements Function<byte[], byte[]> {

    @Override
    public byte[] apply(byte[] bytes) {
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            //FIXME: Elytra flying doesn't work! Why not?!
            if (Transformer.EntityLivingBase$moveEntityWithHeading_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.EntityLivingBase$moveEntityWithHeading_name);

                boolean replaced_getLookVec = false;

                boolean replaced_rotationPitch = false;
                boolean BlockPos$PooledMutableBlockPos$retainFound = false;
                boolean BlockPos$PooledMutableBlockPos$setPosFound = false;
                int numEntityLivingBase$isOffsetPositionInLiquidFound = 0;
                MethodInsnNode previousisOffsetPositionInLiquidMethodInsnNode = null;
                boolean GETFIELD_limbSwingAmount_found = false;
                boolean PUTFIELD_limbSwing_found = false;


                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        // Replaces getLookVec in the Elytra movement with a relative version
                        if (!replaced_getLookVec && Transformer.EntityLivingBase$getLookVec.is(methodInsnNode)) {
                            Transformer.Hooks$getRelativeLookVec.replace(methodInsnNode);
                            replaced_getLookVec = true;
                        }
                        // In normal movement, a PooledMutableBlockPos is create 1.0 below the player's position. We
                        // need to change it to be relative below.
                        //
                        // The original code that is called is removed
                        else if (replaced_rotationPitch
                                && !BlockPos$PooledMutableBlockPos$retainFound
                                && Transformer.BlockPos$PooledMutableBlockPos$retain.is(methodInsnNode)) {
                            Transformer.Hooks$getBlockPostBelowEntity.replace(methodInsnNode);

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
                                BlockPos$PooledMutableBlockPos$retainFound = true;
                            }
                            else {
                                Transformer.die("Failed to find 3 uses of ALOAD 0 before inserted \"" + Transformer.Hooks$getBlockPostBelowEntity + "\" in " + Transformer.EntityLivingBase$moveEntityWithHeading_name);
                            }
                        }
                        // In normal movement, if the player is on the ground, the mutable BlockPos gets set back to 1.0
                        // below the player, again, we set it to be relatively below the player
                        else if (BlockPos$PooledMutableBlockPos$retainFound
                                && !BlockPos$PooledMutableBlockPos$setPosFound
                                && Transformer.BlockPos$PooledMutableBlockPos$setPos.is(methodInsnNode)) {
                            Transformer.Hooks$setPooledMutableBlockPosToBelowEntity.replace(methodInsnNode);

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
                                BlockPos$PooledMutableBlockPos$setPosFound = true;
                            }
                            else {
                                Transformer.die("Failed to find 3 uses of ALOAD 0 before inserted \"" + Transformer.Hooks$setPooledMutableBlockPosToBelowEntity + "\" in " + Transformer.EntityLivingBase$moveEntityWithHeading_name);
                            }
                        }
                        // For these liquid calculations, the passed arguments are (motionX, motionY + change in posY, motionZ)
                        // The motion fields are all relative, but the change in posY is absolute, so we need to get the change
                        // in whatever direction positive Y is from the player's perspective. Both the initial posY and the
                        // later posY are modified
                        else if (BlockPos$PooledMutableBlockPos$setPosFound
                                && numEntityLivingBase$isOffsetPositionInLiquidFound < 2
                                && Transformer.EntityLivingBase$isOffsetPositionInLiquid.is(methodInsnNode)) {
                            if (methodInsnNode == previousisOffsetPositionInLiquidMethodInsnNode) {
                                continue;
                            }
                            previousisOffsetPositionInLiquidMethodInsnNode = methodInsnNode;
                            int numReplacementsMade = 0;
                            while (iterator.hasPrevious() && numReplacementsMade < 2) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)previous;
                                    if (Transformer.EntityLivingBase$posY_GET.is(fieldInsnNode)) {
                                        // Replace the GETFIELD with an INVOKESTATIC of the Hook
                                        Transformer.Hooks$getRelativePosY.replace(iterator);
                                        numReplacementsMade++;
                                    }
                                }
                            }
                            if (numReplacementsMade == 2) {
                                numEntityLivingBase$isOffsetPositionInLiquidFound++;
                            }
                        }
                    }
                    else if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        // Replaces access of the player's pitch with a get-relative-pitch hook, in the Elytra movement
                        if (replaced_getLookVec
                                && !replaced_rotationPitch
                                && Transformer.EntityLivingBase$rotationPitch_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePitch.replace(iterator);
                            replaced_rotationPitch = true;
                        }
                        // Limb swing (arms+legs swinging as you walk around) is based on change in X and Z position
                        // instead of messing with the existing code, we can insert a hook that changes the player's
                        // position fields to act as if they're relative, so that when the vanilla code calculates
                        // the change in X and Z position, it's instead the relative X and Z movement the player made
                        else if (numEntityLivingBase$isOffsetPositionInLiquidFound == 2
                                && !GETFIELD_limbSwingAmount_found
                                && Transformer.EntityLivingBase$limbSwingAmount_GET.is(fieldInsnNode)) {
                            iterator.previous(); // The GETFIELD instruction
                            iterator.previous(); // ALOAD 0
                            Transformer.Hooks$makePositionRelative.addTo(iterator);
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            GETFIELD_limbSwingAmount_found = true;
                        }
                        // Once the limb swing has been set properly, we need to put the player's position fields back to
                        // normal
                        else if (GETFIELD_limbSwingAmount_found
                                && !PUTFIELD_limbSwing_found
                                && Transformer.EntityLivingBase$limbSwing_PUT.is(fieldInsnNode)) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            Transformer.Hooks$makePositionAbsolute.addTo(iterator);
                            PUTFIELD_limbSwing_found = true;
                        }
                    }
                }

                Transformer.dieIfFalse(replaced_getLookVec, "Failed to replace getLookVec");
                Transformer.dieIfFalse(replaced_rotationPitch, "Failed to replace rotationPitch");
                Transformer.dieIfFalse(BlockPos$PooledMutableBlockPos$retainFound, "Failed to find BlockPos$PooledMutableBlockPos::retain");
                Transformer.dieIfFalse(BlockPos$PooledMutableBlockPos$setPosFound, "Failed to find BlockPos$PooledMutableBlockPos::setPos");
                Transformer.dieIfFalse(numEntityLivingBase$isOffsetPositionInLiquidFound == 2, "Found " + numEntityLivingBase$isOffsetPositionInLiquidFound + " EntityLivingBase::isOffsetPositionInLiquidFound, expected 2");
                Transformer.dieIfFalse(GETFIELD_limbSwingAmount_found, "Failed to find GETFIELD limbSwingAmount");
                Transformer.dieIfFalse(PUTFIELD_limbSwing_found, "Failed to find PUTFIELD limbSwing");

                Transformer.logPatchComplete(Transformer.EntityLivingBase$moveEntityWithHeading_name);
                methodPatches++;
            }
            // This method is pretty badly named in my opinion, it seems to set up the turn of the player's head or body
            // (I'm not sure), however it's rendering based and the rotation needs to be made relative. Only the
            // rotationYaw field is accessed, so that is all that is changed
            else if (Transformer.EntityLivingBase$updateDistance_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.EntityLivingBase$updateDistance_name);
                Transformer.patchMethodUsingAbsoluteRotations(methodNode, Transformer.GET_ROTATIONYAW);
                Transformer.logPatchComplete(Transformer.EntityLivingBase$updateDistance_name);
                methodPatches++;
            }
            // For some of the vanilla code that also seems to be to do with head turning, the vanilla code uses the
            // change in X and Z pos from last tick to the current tick, as well as the player's rotationYaw.
            //
            // We replace these field accesses with hooks that get the relative equivalents to them
            else if (Transformer.Entity$onUpdate_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.Entity$onUpdate_name);
                boolean patchComplete = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (Transformer.EntityLivingBase$posX_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePosX.replace(iterator);
                        }
                        else if (Transformer.EntityLivingBase$prevPosX_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePrevPosX.replace(iterator);
                        }
                        else if (Transformer.EntityLivingBase$posZ_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePosZ.replace(iterator);
                        }
                        else if (Transformer.EntityLivingBase$prevPosZ_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePrevPosZ.replace(iterator);
                        }
                        else if (Transformer.EntityLivingBase$rotationYaw_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativeYaw.replace(iterator);
                            patchComplete = true;
                            break;
                        }
                    }
                }
                Transformer.dieIfFalse(patchComplete, "Failed to patch " + Transformer.Entity$onUpdate_name);
                Transformer.logPatchComplete(Transformer.Entity$onUpdate_name);
                methodPatches++;
            }
            // When sprinting, the player's rotationYaw is used to determine the boost in forwards movement to add
            // This needs to be made relative to match the direction the player is actually sprinting in
            else if (Transformer.EntityLivingBase$jump_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.EntityLivingBase$jump_name);
                Transformer.patchMethodUsingAbsoluteRotations(methodNode, Transformer.GET_ROTATIONYAW);
                Transformer.logPatchComplete(Transformer.EntityLivingBase$jump_name);
                methodPatches++;
            }
        }

        Transformer.dieIfFalse(methodPatches == 4, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
