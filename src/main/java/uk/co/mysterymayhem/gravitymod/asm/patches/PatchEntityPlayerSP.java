package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import uk.co.mysterymayhem.gravitymod.asm.Ref;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.InsnPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.MethodPatcher;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Created by Mysteryem on 2017-02-01.
 */
public class PatchEntityPlayerSP extends ClassPatcher {
    public PatchEntityPlayerSP() {
        super("net.minecraft.client.entity.EntityPlayerSP", 0, ClassWriter.COMPUTE_FRAMES);

        // onUpdateWalkingPlayer makes the assumption that the minY of the player's bounding box is the same as their y position
        //
        // We remove all uses of axisalignedbb.minY and replace them with this.posY
        this.addMethodPatch(Ref.EntityPlayerSP$onUpdateWalkingPlayer_name::is, methodNode -> {
            int numReplacements = 0;
            for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                AbstractInsnNode next = iterator.next();
                if (next instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                    if (Ref.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
                        if (iterator.hasPrevious()) {
                            iterator.previous();
                            AbstractInsnNode previous = iterator.previous();
                            if (previous instanceof VarInsnNode) {
                                VarInsnNode varInsnNode = (VarInsnNode)previous;
                                if (varInsnNode.var == 3) {
                                    /*
                                    Replace
                                    axisalignedbb.minY
                                    with
                                    this.posY
                                    in
                                    EntityPlayerSP::onUpdateWalkingPlayer
                                     */
                                    varInsnNode.var = 0;
                                    Ref.EntityPlayerSP$posY_GET.replace(fieldInsnNode);
                                    numReplacements++;

                                    // We went back two with .previous(), so go forwards again the same amount
                                    iterator.next();
                                    iterator.next();
                                }
                            }
                        }
                    }
                }
            }
            Transformer.dieIfFalse(numReplacements != 0, "Failed to find any instances of \"axisalignedbb.minY\"");
        });

        // onLivingUpdate calls EntityPlayerSP::pushOutOfBlocks a bunch of times with arguments that look like they won't respect
        //      non-standard gravity, the inserted Hook currently just performs the vanilla behaviour
        this.addMethodPatch(Ref.EntityLivingBase$onLivingUpdate_name::is, this::onLivingUpdatePatch);

        // isHeadSpaceFree checks upwards, it's changed to check upwards relative to the player (the method is small,
        //      I deleted the original entirely for the time being.
        this.addMethodPatch(Ref.EntityPlayerSP$isHeadspaceFree_name::is, methodNode -> {
            InsnList instructions = methodNode.instructions;
            AbstractInsnNode label = instructions.get(0);
            AbstractInsnNode lineNumber = instructions.get(1);
            AbstractInsnNode labelEnd = instructions.getLast();
            // Don't actually need the returnNode
//            AbstractInsnNode returnNode = labelEnd.getPrevious();

            //TODO: Rewrite this so that it doesn't straight up delete all the existing instructions
            instructions.clear();
            instructions.add(label);
            instructions.add(lineNumber);
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            //TODO: Introduce a way to grab the localvariable indices
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
            Ref.Hooks$isHeadspaceFree.addTo(instructions);
            instructions.add(new InsnNode(Opcodes.IRETURN));
            instructions.add(labelEnd);
        });

        // There are some methods in updateAutoJump that we need to replace with gravity-direction-aware methods
        // There are a bunch of them throughout, so we do a blanket replacement for simplicity
        this.addMethodPatch(Ref.EntityPlayerSP$updateAutoJump_name::is, methodNode -> {
            int addVectorReplacements = 0;
            int blockPosUPCount = 0;
            int blockPosUPCountI = 0;

            for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); /**/) {
                AbstractInsnNode node = iterator.next();
                if (Ref.Vec3d$addVector.is(node)) {
                    iterator.remove();
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    Ref.Hooks$addAdjustedVector.addTo(iterator);
                    addVectorReplacements++;
                }
                else if (Ref.BlockPos$up_NO_ARGS.is(node)) {
                    iterator.remove();
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    Ref.Hooks$getRelativeUpblockPos_NO_ARGS.addTo(iterator);
                    blockPosUPCount++;
                }
                else if (Ref.BlockPos$up_INT_ARG.is(node)) {
                    iterator.remove();
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    Ref.Hooks$getRelativeUpblockPos_INT_ARG.addTo(iterator);
                    blockPosUPCountI++;
                }
            }

            Transformer.log("updateAutoJump Replacement counts: addVector: " + addVectorReplacements
                    + ", up(): " + blockPosUPCount
                    + ", up(int): " + blockPosUPCountI);
        });

        // The updateAutoJump method has so many patches.
        // I somehow managed to get it to work correctly with mostly trial and error.
        // I don't understand some parts of the method in the deobfuscated, normal vanilla code.
        // Hopefully updateAutoJump won't get changed much in future Minecraft versions meaning I won't have to scratch my head over and over to get it to
        // work when I begin trying to port this mod to a future version
        if (Transformer.DEBUG_AUTO_JUMP) {
            // Use debug patch that re-routes control back to EntityPlayerWithGravity
            this.addMethodPatch(Ref.EntityPlayerSP$updateAutoJump_name::is, (node, iterator) -> {
                if (node instanceof LineNumberNode) {
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    iterator.add(new VarInsnNode(Opcodes.FLOAD, 1));
                    iterator.add(new VarInsnNode(Opcodes.FLOAD, 2));
                    Ref.AbstractClientPlayer$updateAutoJump_SPECIAL.addTo(iterator);
                    iterator.add(new InsnNode(Opcodes.RETURN));
                    return true;
                }
                return false;
            });
        }
        else {
            // So many patches that it gets its own class
            this.addMethodPatch(new UpdateAutoJump());
        }

        // moveEntity passes the change in X and Z movement to the updateAutoJump method, we need to pass the change in
        //      X and Z relative to the player's view instead
        //
        //      DEBUG_AUTO_JUMP instead calls super to help with debugging EntityPlayerWithGravity::updateAutoJump
        if (Transformer.DEBUG_AUTO_JUMP) {
            // Replace method with super call for debugging purposes
            this.addMethodPatch(Ref.Entity$moveEntity_name::is, (node, iterator) -> {
                if (node instanceof LineNumberNode) {
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    iterator.add(new VarInsnNode(Opcodes.DLOAD, 1));
                    iterator.add(new VarInsnNode(Opcodes.DLOAD, 3));
                    iterator.add(new VarInsnNode(Opcodes.DLOAD, 5));
                    Ref.AbstractClientPlayer$moveEntity_SPECIAL.addTo(iterator);
                    iterator.add(new InsnNode(Opcodes.RETURN));
                    return true;
                }
                return false;
            });
        }
        else {
            // Change moveEntity so that it calls updateAutoJump with the change in relative X and Z instead of the change in X and Z
            // TODO: Look into splitting this into a bunch of smaller patches
            // TODO: Look into making this work by additionally storing 'before posY' to a new localvariable instead of changing the values that get stored
            //       into 'before posX' and 'before posZ'. We would then be able to pass all 3 old variables and 'this' to a Hook method and either calculate
            //       the differences in X and Z and pass them back into this method, which we would then direct into the updateAutoJump method call, or the
            //       hook may be able to call updateAutoJump itself and instead we'll just return from the current method early (updateAutoJump would have to
            //       be called via reflection as we can't safely 'underride' the method in EntityPlayerWithGravity as it won't be reobfuscated, so it can
            //       only work in either a dobfuscated or normal environment and not both).
            // This method starts by storing the current x, y and z positions of the player
            this.addMethodPatch(Ref.Entity$moveEntity_name::is, this::moveEntityPatch);
        }
    }

    private boolean moveEntityPatch(AbstractInsnNode node, ListIterator<AbstractInsnNode> iterator) {
        if (node.getOpcode() == Opcodes.ALOAD && node instanceof VarInsnNode) {
            int prevRelativeXPos_var = -1;
            int prevRelativeZPos_var = -1;
            // ALOAD 0                                          // this
            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));    // this, this
            iterator.next(); // GETFIELD this.posX              // this, this.posX
            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));    // this, this.posX, this
            Ref.EntityPlayerSP$posY_GET.addTo(iterator);        // this, this.posX, this.posY
            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));    // this, this.posX, this.posY, this
            Ref.EntityPlayerSP$posZ_GET.addTo(iterator);        // this, this.posX, this.posY, this.posZ
            Ref.Hooks$inverseAdjustXYZ.addTo(iterator);         // doubles
            iterator.add(new InsnNode(Opcodes.DUP));            // doubles, doubles
            iterator.add(new InsnNode(Opcodes.ICONST_0));       // doubles, doubles, 0
            iterator.add(new InsnNode(Opcodes.DALOAD));         // doubles, doubles[0]
            //next is DSTORE 7
            node = iterator.next();                             // doubles (pseudo-relative posX stored to local var '7')
            if (node instanceof VarInsnNode) {
                prevRelativeXPos_var = ((VarInsnNode)node).var;
            }
            else {
                Transformer.die("Was expecting DSTORE _ after GETFIELD posX, instead got " + node);
            }

            while (true) {
                node = iterator.next();
                // Beforehand, the code would do 'localVar7 = this.posZ'
                // Instead of re-calling Hooks.inverseAdjustXYZ and gathering the arguments again, we duplicated the result on the stack, so we can get
                // the pseudo-relative posZ from it
                if (node instanceof VarInsnNode && node.getOpcode() == Opcodes.ALOAD) {
                    // node == ALOAD 0                                                          // doubles, this
//                            iterator.remove(); // disabled removal of existing code
                    iterator.next(); // GETFIELD posZ                                           // doubles, this.posZ
//                            iterator.remove(); // disabled removal of existing code
                    //next is DSTORE 9
                    node = iterator.next();                                                     // doubles (this.posZ stored to local var '9')
                    if (node instanceof VarInsnNode) {
                        prevRelativeZPos_var = ((VarInsnNode)node).var;
                    }
                    else {
                        Transformer.die("Was expecting DSTORE _ after GETFIELD posZ, instead got " + node);
                    }

                    iterator.add(new InsnNode(Opcodes.ICONST_2));                               // doubles, 2
                    iterator.add(new InsnNode(Opcodes.DALOAD));                                 // doubles[2]
                    iterator.add(new VarInsnNode(Opcodes.DSTORE, prevRelativeZPos_var));        // [empty] (pseudo-relative posZ stored to local var '9'

                    while (true) {
                        node = iterator.next();
                        // We skip a bunch of instructions where super.moveEntity is called with the arguments that were passed to this method.
                        // super.moveEntity updates this.posX/Y/Z to new values.
                        // The difference between the new values and the old values (x and z only) then gets passed to the updateAutoJump method.
                        // We need to pass the difference between new and old _relative_ x and z values instead.
                        //
                        // The inserted code is done Z first, so that the stack ends up how we want
                        //
                        // We stop once we hit the next GETFIELD posX instruction.
                        // Before it, are two ALOAD 0 instructions, giving us a stack of:       // this, this
                        if (Ref.EntityPlayerSP$posX_GET.is(node)) {
                            iterator.previous(); // We're going to insert another ALOAD 0 before the GETFIELD posX
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));                    // this, this, this
                            iterator.next(); // same GETFIELD instruction                       // this, this, this.posX
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));                    // this, this, this.posX, this
                            Ref.EntityPlayerSP$posY_GET.addTo(iterator);                        // this, this, this.posX, this.posY
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));                    // this, this, this.posX, this.posY, this
                            Ref.EntityPlayerSP$posZ_GET.addTo(iterator);                        // this, this, this.posX, this.posY, this.posZ
                            Ref.Hooks$inverseAdjustXYZ.addTo(iterator);                         // this, doubles
                            iterator.add(new InsnNode(Opcodes.DUP));                            // this, doubles, doubles
                            iterator.add(new InsnNode(Opcodes.ICONST_2));                       // this, doubles, doubles, 2
                            iterator.add(new InsnNode(Opcodes.DALOAD));                         // this, doubles, newRelPosZ
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, prevRelativeZPos_var)); // this, doubles, newRelPosZ, oldRelPosZ
                            iterator.add(new InsnNode(Opcodes.DSUB));                           // this, doubles, (newRelPosZ - oldRelPosZ)
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, prevRelativeZPos_var));// this, doubles (stored posZ difference)
                            iterator.add(new InsnNode(Opcodes.ICONST_0));                       // this, doubles, 0
                            iterator.add(new InsnNode(Opcodes.DALOAD));                         // this, newRelPosX
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, prevRelativeXPos_var)); // this, newRelPosX, oldRelPosX
                            iterator.add(new InsnNode(Opcodes.DSUB));                           // this, (newRelPosX - oldRelPosX)
                            iterator.add(new InsnNode(Opcodes.D2F));                            // this, (float)xDiff
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, prevRelativeZPos_var)); // this, (float)xDiff, zDiff
                            iterator.add(new InsnNode(Opcodes.D2F));                            // this, (float)xDiff, (float)zDiff

                            // We then delete all existing instructions (it would calculate the difference in X and Z, convert them to floats and then
                            // call updateAutoJump
                            while (true) {
                                node = iterator.next();
                                if (node.getOpcode() == INVOKEVIRTUAL) {
                                    // Once we hit the method call, we're done (the method also returns afterwards)
                                    //INVOKEVIRTUAL EntityPlayerSP.updateAutoJump (FF)V         // [empty]
                                    return true;
                                }
                                else {
                                    iterator.remove();
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean onLivingUpdatePatch(AbstractInsnNode node, ListIterator<AbstractInsnNode> iterator) {
        if (Ref.EntityPlayerSP$getEntityBoundingBox.is(node)) {
            node = iterator.next();
            if (node instanceof VarInsnNode && node.getOpcode() == Opcodes.ASTORE) {
                int axisalignedbb_var = ((VarInsnNode)node).var;

                VarInsnNode aLoad6 = new VarInsnNode(Opcodes.ALOAD, axisalignedbb_var);
                TypeInsnNode instanceofGravityAxisAlignedBB = new TypeInsnNode(Opcodes.INSTANCEOF, Ref.GravityAxisAlignedBB.toString());
                JumpInsnNode ifeqJumpInsnNode = new JumpInsnNode(Opcodes.IFEQ, null);
                VarInsnNode aLoad0 = new VarInsnNode(Opcodes.ALOAD, 0);
//                            VarInsnNode aLoad6_2 = new VarInsnNode(Opcodes.ALOAD, axisalignedbb_var);
                JumpInsnNode gotoJumpInsnNode = new JumpInsnNode(Opcodes.GOTO, null);

                // Load the localvariable which holds the value previously returned by this.getEntityBoundingBox()
                iterator.add(aLoad6);
                // Add 'if (axisalignedbb instanceof GravityAxisAlignedBB)'
                iterator.add(instanceofGravityAxisAlignedBB);
                // Add the conditional jump instruction
                iterator.add(ifeqJumpInsnNode);
                // Load 'this'
                iterator.add(aLoad0);
                // Load the localvariable which holds the value previously...
                iterator.add(aLoad6.clone(null));
                // Add our static hooks call
                Ref.Hooks$pushEntityPlayerSPOutOfBlocks.addTo(iterator);
                // Add the unconditional jump
                iterator.add(gotoJumpInsnNode);

                node = iterator.next();

                if (node instanceof LabelNode) {
                    ifeqJumpInsnNode.label = (LabelNode)node;

                    node = iterator.next();

                    if (node instanceof LineNumberNode) {
                        // ClassWriter is going to compute the frames, the commented out code is untested
//                        FrameNode frameAppendNode = new FrameNode(Opcodes.F_APPEND, 1, new Object[]{"net/minecraft/util/math/AxisAlignedBB"}, 0, new Object[0]);
//                        iterator.add(frameAppendNode);

                        LabelNode labelForGotoJumpInsnNode = null;

                        while (iterator.hasNext()) {
                            node = iterator.next();
                            if (node instanceof MethodInsnNode) {
                                MethodInsnNode methodInsnNode = (MethodInsnNode)node;
                                if (Ref.EntityPlayer$getFoodStats.is(methodInsnNode)) {
                                    while (iterator.hasPrevious()) {
                                        AbstractInsnNode previous = iterator.previous();
                                        if (previous instanceof LabelNode) {
                                            labelForGotoJumpInsnNode = (LabelNode)previous;
                                            // ClassWriter is going to compute the frames, the commented out code is untested
//                                            for(;iterator.hasNext();) {
//                                                node = iterator.next();
//                                                if (node instanceof VarInsnNode) {
//                                                    varInsnNode = (VarInsnNode)node;
//                                                    if (varInsnNode.getOpcode() == Opcodes.ALOAD && varInsnNode.var == 0) {
//                                                        FrameNode frameSameNode = new FrameNode(Opcodes.F_SAME, 0, new Object[0], 0, new Object[0]);
//                                                        break;
//                                                    }
//                                                }
//                                            }
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        Transformer.dieIfFalse(labelForGotoJumpInsnNode != null, "Couldn't find correct label for jump instruction");
                        gotoJumpInsnNode.label = labelForGotoJumpInsnNode;
                        return true;
                    }
                }
            }
            Transformer.die("Unexpected instruction after EntityPlayerSP::getEntityBoundingBox");
        }
        return false;
    }

    private class UpdateAutoJump extends MethodPatcher {

        public UpdateAutoJump() {
            // Normal patches

            // Root node
            // aka foundFirstBlockPosGetY
            // No children
            this.addInsnPatch((node, iterator) -> {
                if (Ref.BlockPos$getY.is(node)) {
                    iterator.remove();
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    Ref.Hooks$getRelativeYOfBlockPos.addTo(iterator);
                    return true;
                }
                return false;
            });

            // Root node
            InsnPatcher patch1 = this.addInsnPatch((node, iterator) -> {
                if (node.getOpcode() == Opcodes.NEW && node instanceof TypeInsnNode && ((TypeInsnNode)node).desc.equals(Ref.Vec3d.toString())) {
                    iterator.remove();
                    while (true) {
                        node = iterator.next();
                        if (node instanceof MethodInsnNode && node.getOpcode() == INVOKESPECIAL) {
                            iterator.remove();
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            Ref.Hooks$getBottomOfEntity.addTo(iterator);
                            return true;
                        }
                        else {
                            iterator.remove();
                        }
                    }
                }
                return false;
            });

            InsnPatcher patch2 = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityPlayerSP$posX_GET.is(node)) {
                    Ref.Hooks$getOriginRelativePosX.replace(iterator);
                    return true;
                }
                return false;
            });

            InsnPatcher patch3 = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityPlayerSP$posZ_GET.is(node)) {
                    Ref.Hooks$getOriginRelativePosZ.replace(iterator);
                    return true;
                }
                return false;
            });

            InsnPatcher patch4 = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityPlayerSP$getEntityBoundingBox.is(node)) {
                    iterator.remove();
                    iterator.next();
                    iterator.remove(); // GETFIELD net/minecraft/util/math/AxisAlignedBB.minY : D
                    Ref.Hooks$getOriginRelativePosY.addTo(iterator);
                    iterator.next(); // DLOAD ? (likely 7)
                    iterator.next(); // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Have to pass 'this' to adjustVec as well
                    Ref.Hooks$adjustVec.addTo(iterator);
                    return true;
                }
                return false;
            });

            InsnPatcher patch5 = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityPlayerSP$rotationYaw_GET.is(node)) {
                    Ref.Hooks$getRelativeYaw.replace(iterator);
                    return true;
                }
                return false;
            });

            InsnPatcher patch5Repeat = this.addInsnPatch(patch5.copy());

            final String vec3d12_var_key = "vec3d12_var";

            InsnPatcher patch6 = this.addInsnPatch((node, iterator) -> {
                if (Ref.Vec3d$scale.is(node)) {
                    node = iterator.next();
                    if (node instanceof VarInsnNode) {
                        // Should also be ASTORE
                        PatchEntityPlayerSP.this.storeData(vec3d12_var_key, ((VarInsnNode)node).var);
                        return true;
                    }
                    else {
                        Transformer.die("Expected a VarInsnNode after first usage of Vec3d::scale");
                    }
                }
                return false;
            });

            InsnPatcher patch7 = this.addInsnPatch((node, iterator) -> {
                if (Ref.EntityPlayerSP$getForward.is(node)) {
                    Ref.Hooks$getRelativeLookVec.replace(iterator);
                    return true;
                }
                return false;
            });

            InsnPatcher patch8 = this.addInsnPatch((node, iterator) -> {
                if (node.getOpcode() == Opcodes.NEW && node instanceof TypeInsnNode && ((TypeInsnNode)node).desc.equals(Ref.BlockPos.toString())) {
                    int vec3d12_var = (Integer)PatchEntityPlayerSP.this.getData(vec3d12_var_key);

                    iterator.remove();
                    while (true) {
                        node = iterator.next();
                        if (node instanceof MethodInsnNode && node.getOpcode() == INVOKESPECIAL) {
                            iterator.remove();
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            Ref.Hooks$getBlockPosAtTopOfPlayer.addTo(iterator);

                            //newPATCH #6.5
                            iterator.next(); //ASTORE ? (probably 17)
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, vec3d12_var));
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            Ref.Hooks$adjustVec.addTo(iterator);
                            iterator.add(new VarInsnNode(Opcodes.ASTORE, vec3d12_var));

                            return true;
                        }
                        else {
                            iterator.remove();
                        }
                    }
                }
                return false;
            });

            InsnPatcher patch9 = this.addInsnPatch((node, iterator) -> {
                if (node.getOpcode() == Opcodes.NEW && node instanceof TypeInsnNode && ((TypeInsnNode)node).desc.equals(Ref.AxisAlignedBB.toString())) {
                    iterator.remove();
                    node = iterator.next();
                    if (node instanceof InsnNode && node.getOpcode() == Opcodes.DUP) {
                        iterator.remove();
                        return true;
                    }
                    else {
                        //what
                        Transformer.die("DUP instruction not found after expected NEW AxisAlignedBB");
                    }
                }
                return false;
            });

            InsnPatcher patch10 = this.addInsnPatch((node, iterator) -> {
                if (Ref.AxisAlignedBB$INIT.is(node)) {
                    iterator.remove();
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    Ref.Hooks$constructNewGAABBFrom2Vec3d.addTo(iterator);
                    return true;
                }
                return false;
            });

            InsnPatcher patch11 = this.addInsnPatch((node, iterator) -> {
                if (node.getOpcode() == Opcodes.NEW && node instanceof TypeInsnNode && ((TypeInsnNode)node).desc.equals(Ref.Vec3d.toString())) {
                    iterator.next(); // DUP
                    iterator.next(); // DCONST_0
                    node = iterator.next();
                    if (node.getOpcode() == Opcodes.DCONST_1) {
                        iterator.remove();
                        iterator.add(new InsnNode(Opcodes.DCONST_0));
                    }
                    else {
                        Transformer.die("Expecting DCONST_0 followed by DCONST_1, but instead got " + node);
                    }
                    iterator.next(); // DCONST_0
                    iterator.next(); // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V

                    iterator.add(new InsnNode(Opcodes.DCONST_0));
                    iterator.add(new InsnNode(Opcodes.DCONST_1));
                    iterator.add(new InsnNode(Opcodes.DCONST_0));
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    Ref.Hooks$addAdjustedVector.addTo(iterator);

                    return true;
                }
                return false;
            });

            InsnPatcher patch12 = this.addInsnPatch((node, iterator) -> {
                if (Ref.AxisAlignedBB$maxY_GET.is(node)) {
                    iterator.remove();
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    Ref.Hooks$getRelativeTopOfBB.addTo(iterator);
                    return true;
                }
                return false;
            });

            InsnPatcher patch12Repeat = this.addInsnPatch(patch12.copy());

            InsnPatcher patch13 = this.addInsnPatch((node, iterator) -> {
                if (Ref.AxisAlignedBB$minY_GET.is(node)) {
                    iterator.remove();
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    Ref.Hooks$getRelativeBottomOfBB.addTo(iterator);
                    return true;
                }
                return false;
            });

            InsnPatcher patch13Repeat = this.addInsnPatch(patch13.copy());

            // Patch relations (all just sequential)
            InsnPatcher.sequentialOrder(
                    patch1,
                    patch2,
                    patch3,
                    patch4,
                    patch5,
                    patch5Repeat,
                    patch6,
                    patch7,
                    patch8,
                    patch9,
                    patch10,
                    patch11,
                    patch12,
                    patch12Repeat,
                    patch13,
                    patch13Repeat
            );
        }

        @Override
        protected boolean shouldPatchMethod(MethodNode methodNode) {
            return Ref.EntityPlayerSP$updateAutoJump_name.is(methodNode);
        }
    }
}
