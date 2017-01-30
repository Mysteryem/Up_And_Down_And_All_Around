package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;

import java.util.ListIterator;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class PatchEntityPlayerSP implements Function<byte[], byte[]> {

    @Override
    public byte[] apply(byte[] bytes) {
        int methodPatches = 0;
        int expectedMethodPatches = 5;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        int numReplacements = 0;

        for (MethodNode methodNode : classNode.methods) {
            // onUpdateWalkingPlayer makes the assumption that the minY of the player's bounding box is the same as their y position
            //
            // We remove the usage of axisalignedbb.minY and replace it with this.posY
            if (Transformer.EntityPlayerSP$onUpdateWalkingPlayer_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.EntityPlayerSP$onUpdateWalkingPlayer_name);

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (Transformer.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
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
                                        Transformer.EntityPlayerSP$posY_GET.replace(fieldInsnNode);
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
                Transformer.logPatchComplete(Transformer.EntityPlayerSP$onUpdateWalkingPlayer_name);
                methodPatches++;
            }
            // onLivingUpdate calls EntityPlayerSP::pushOutOfBlocks a bunch of times with arguments that look like they won't respect
            //      non-standard gravity, the inserted Hook currently just performs the vanilla behaviour
            else if (Transformer.EntityLivingBase$onLivingUpdate_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.EntityLivingBase$onLivingUpdate_name);

                boolean error = true;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (Transformer.EntityPlayerSP$getEntityBoundingBox.is(next)) {
                        next = iterator.next();
                        if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.ASTORE) {
                            int axisalignedbb_var = ((VarInsnNode)next).var;

                            VarInsnNode aLoad6 = new VarInsnNode(Opcodes.ALOAD, axisalignedbb_var);
                            TypeInsnNode instanceofGravityAxisAlignedBB = new TypeInsnNode(Opcodes.INSTANCEOF, Transformer.GravityAxisAlignedBB.toString());
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
                            Transformer.Hooks$pushEntityPlayerSPOutOfBlocks.addTo(iterator);
                            // Add the unconditional jump
                            iterator.add(gotoJumpInsnNode);

                            next = iterator.next();

                            if (next instanceof LabelNode) {
                                ifeqJumpInsnNode.label = (LabelNode)next;

                                next = iterator.next();

                                if (next instanceof LineNumberNode) {
                                    // ClassWriter is going to compute the frames, the commented out code is untested
//                                    FrameNode frameAppendNode = new FrameNode(Opcodes.F_APPEND, 1, new Object[]{"net/minecraft/util/math/AxisAlignedBB"}, 0, new Object[0]);
//                                    iterator.add(frameAppendNode);

                                    LabelNode labelForGotoJumpInsnNode = null;

                                    for (; iterator.hasNext(); ) {
                                        next = iterator.next();
                                        if (next instanceof MethodInsnNode) {
                                            MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                                            if (Transformer.EntityPlayer$getFoodStats.is(methodInsnNode)) {
                                                for (; iterator.hasPrevious(); ) {
                                                    AbstractInsnNode previous = iterator.previous();
                                                    if (previous instanceof LabelNode) {
                                                        labelForGotoJumpInsnNode = (LabelNode)previous;
                                                        // ClassWriter is going to compute the frames, the commented out code is untested
//                                                        for(;iterator.hasNext();) {
//                                                            next = iterator.next();
//                                                            if (next instanceof VarInsnNode) {
//                                                                varInsnNode = (VarInsnNode)next;
//                                                                if (varInsnNode.getOpcode() == Opcodes.ALOAD && varInsnNode.var == 0) {
//                                                                    FrameNode frameSameNode = new FrameNode(Opcodes.F_SAME, 0, new Object[0], 0, new Object[0]);
//                                                                    break;
//                                                                }
//                                                            }
//                                                        }
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    Transformer.dieIfFalse(labelForGotoJumpInsnNode != null, "Couldn't find correct label for jump instruction");
                                    gotoJumpInsnNode.label = labelForGotoJumpInsnNode;
                                    error = false;
                                    break;
                                }
                            }
                            break;
                        }
                        else {
                            Transformer.die("Unexpected instruction after EntityPlayerSP::getEntityBoundingBox");
                        }
                    }
                }
                Transformer.dieIfFalse(!error, "Patch failed");
                methodPatches++;
                Transformer.logPatchComplete(Transformer.EntityLivingBase$onLivingUpdate_name);
            }
            // isHeadSpaceFree checks upwards, it's changed to check upwards relative to the player (the method is small,
            //      I deleted the original entirely for the time being.
            else if (Transformer.EntityPlayerSP$isHeadspaceFree_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.EntityPlayerSP$isHeadspaceFree_name);
                InsnList instructions = methodNode.instructions;
                AbstractInsnNode label = instructions.get(0);
                AbstractInsnNode lineNumber = instructions.get(1);
                AbstractInsnNode labelEnd = instructions.getLast();
                AbstractInsnNode returnNode = labelEnd.getPrevious();

                //TODO: Rewrite this so that it doesn't straight up delete all the existing instructions
                instructions.clear();
                instructions.add(label);
                instructions.add(lineNumber);
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                //TODO: Introduce a way to grab the localvariable indices
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
                Transformer.Hooks$isHeadspaceFree.addTo(instructions);
                instructions.add(new InsnNode(Opcodes.IRETURN));
                instructions.add(labelEnd);
                methodPatches++;
                Transformer.logPatchComplete(Transformer.EntityPlayerSP$isHeadspaceFree_name);
            }
            // updateAutoJump assumes the player has normal gravity in a lot of places, a few patches may not be neccessary now
            //      that player pitch and yaw is absolute, but it will likely still be the largest patch by far // TODO: 2016-10-19 Update for absolute pitch/yaw.
            //
            //      DEBUG_AUTO_JUMP instead makes the method call super.updateAutoJump(...), which doesn't exist normally, but
            //      EntityPlayerWithGravity (the class we insert into the hierarchy) can be given its own updateAutoJump method.
            //      This can copy the vanilla code with changes that take into account the gravity direction. This method is then
            //      used to create the patches for the vanilla method
            else if (Transformer.EntityPlayerSP$updateAutoJump_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.EntityPlayerSP$updateAutoJump_name);
                int addVectorReplacements = 0;
                int blockPosUPCount = 0;
                int blockPosUPCountI = 0;
                boolean foundFirstBlockPosGetY = false;
                //newPATCH #1
                boolean patch1Complete = false;
                //newPATCH #2
                boolean patch2Complete = false;
                //newPATCH #3
                boolean patch3Complete = false;
                //newPATCH #4
                boolean patch4Complete = false;
                //newPATCH #4.5
                boolean patch4Point5Complete = false;
                int patch4Point5ReplacementCount = 0;
                //newPATCH #5 and #6.5
                boolean patch5Complete = false;
                int vec3d12_var = -1;
                boolean patch5Point5Complete = false;
                boolean patch6Complete = false;
                boolean patch7Complete = false;
                boolean patch8Complete = false;
                boolean patch9Complete = false;
                int axisAlignedBBmaxYCount = 0;
                int axisAlignedBBminYCount = 0;

                // Used during development
                if (Transformer.DEBUG_AUTO_JUMP) {
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof LineNumberNode) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.FLOAD, 1));
                            iterator.add(new VarInsnNode(Opcodes.FLOAD, 2));
                            Transformer.AbstractClientPlayer$updateAutoJump_SPECIAL.addTo(iterator);
                            iterator.add(new InsnNode(Opcodes.RETURN));
                        }
                    }
                }
                else {
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof MethodInsnNode) {
                            MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                            //newPATCH #4
                            if (patch3Complete
                                    && !patch4Complete
                                    && Transformer.EntityPlayerSP$getEntityBoundingBox.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.next();
                                iterator.remove(); // GETFIELD net/minecraft/util/math/AxisAlignedBB.minY : D
                                Transformer.Hooks$getOriginRelativePosY.addTo(iterator);
                                iterator.next(); // DLOAD ? (likely 7)
                                iterator.next(); // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Have to pass 'this' to adjustVec as well
                                Transformer.Hooks$adjustVec.addTo(iterator);
                                patch4Complete = true;
                            }
                            //newPatch #5
                            else if (patch4Point5Complete
                                    && !patch5Complete
                                    && Transformer.Vec3d$scale.is(methodInsnNode)) {
                                next = iterator.next();
                                if (next instanceof VarInsnNode) {
                                    // Should also be ASTORE
                                    vec3d12_var = ((VarInsnNode)next).var;
                                    patch5Complete = true;
                                }
                                else {
                                    Transformer.die("Expected a VarInsnNode after first usage of Vec3d::scale");
                                }
                            }
                            else if (patch5Complete
                                    && !patch5Point5Complete
                                    && Transformer.EntityPlayerSP$getForward.is(methodInsnNode)) {
                                Transformer.Hooks$getRelativeLookVec.replace(methodInsnNode);
                                patch5Point5Complete = true;
                            }
                            //PATCH #1
                            else if (Transformer.Vec3d$addVector.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$addAdjustedVector.addTo(iterator);
                                addVectorReplacements++;
                            }
                            //newPATCH #8
                            else if (patch7Complete
                                    && !patch8Complete
                                    && Transformer.AxisAlignedBB$INIT.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$constructNewGAABBFrom2Vec3d.addTo(iterator);
                                patch8Complete = true;
                            }
                            //PATCH #3
                            else if (Transformer.BlockPos$up_NO_ARGS.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeUpblockPos_NO_ARGS.addTo(iterator);
                                blockPosUPCount++;
                            }
                            else if (Transformer.BlockPos$up_INT_ARG.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeUpblockPos_INT_ARG.addTo(iterator);
                                blockPosUPCountI++;
                            }
                            //PATCH #4
                            else if (!foundFirstBlockPosGetY
                                    && Transformer.BlockPos$getY.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeYOfBlockPos.addTo(iterator);
                                foundFirstBlockPosGetY = true;
                            }

                        }
                        else if (next instanceof TypeInsnNode
                                && next.getOpcode() == Opcodes.NEW) {
                            TypeInsnNode typeInsnNode = (TypeInsnNode)next;
                            //newPATCH #1
                            if (!patch1Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals(Transformer.Vec3d.toString())) {
                                iterator.remove();
                                while (!patch1Complete) {
                                    next = iterator.next();
                                    if (next instanceof MethodInsnNode && next.getOpcode() == INVOKESPECIAL) {
                                        iterator.remove();
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        Transformer.Hooks$getBottomOfEntity.addTo(iterator);
                                        patch1Complete = true;
                                    }
                                    else {
                                        iterator.remove();
                                    }
                                }
                            }
                            //newPATCH #7
                            else if (!patch7Complete
                                    && patch6Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals(Transformer.AxisAlignedBB.toString())) {
                                iterator.remove();
                                next = iterator.next();
                                if (next instanceof InsnNode && next.getOpcode() == Opcodes.DUP) {
                                    iterator.remove();
                                    patch7Complete = true;
                                }
                                else {
                                    //what
                                    Transformer.die("DUP instruction not found after expected NEW AxisAlignedBB");
                                }
                            }
                            //newPATCH #6
                            else if (patch5Point5Complete
                                    && !patch6Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals(Transformer.BlockPos.toString())) {
                                iterator.remove();
                                while (!patch6Complete) {
                                    next = iterator.next();
                                    if (next instanceof MethodInsnNode && next.getOpcode() == INVOKESPECIAL) {
                                        iterator.remove();
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        Transformer.Hooks$getBlockPosAtTopOfPlayer.addTo(iterator);

                                        //newPATCH #6.5
                                        iterator.next(); //ASTORE ? (probably 17)
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, vec3d12_var));
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        Transformer.Hooks$adjustVec.addTo(iterator);
                                        iterator.add(new VarInsnNode(Opcodes.ASTORE, vec3d12_var));

                                        patch6Complete = true;
                                    }
                                    else {
                                        iterator.remove();
                                    }
                                }
                            }
                            //newPATCH #9
                            else if (patch8Complete
                                    && !patch9Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals(Transformer.Vec3d.toString())) {
                                iterator.next(); // DUP
                                iterator.next(); // DCONST_0
                                next = iterator.next();
                                if (next.getOpcode() == Opcodes.DCONST_1) {
                                    iterator.remove();
                                    iterator.add(new InsnNode(Opcodes.DCONST_0));
                                }
                                else {
                                    Transformer.die("Expecting DCONST_0 followed by DCONST_1, but instead got " + next);
                                }
                                iterator.next(); // DCONST_0
                                iterator.next(); // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V

                                iterator.add(new InsnNode(Opcodes.DCONST_0));
                                iterator.add(new InsnNode(Opcodes.DCONST_1));
                                iterator.add(new InsnNode(Opcodes.DCONST_0));
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$addAdjustedVector.addTo(iterator);

                                patch9Complete = true;
                            }

                        }
                        else if (next instanceof FieldInsnNode) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                            //newPATCH #2
                            if (patch1Complete
                                    && !patch2Complete
                                    && Transformer.EntityPlayerSP$posX_GET.is(fieldInsnNode)) {
                                Transformer.Hooks$getOriginRelativePosX.replace(iterator);
                                patch2Complete = true;
                            }
                            //newPATCH #3
                            else if (patch2Complete
                                    && !patch3Complete
                                    && Transformer.EntityPlayerSP$posZ_GET.is(fieldInsnNode)) {
                                Transformer.Hooks$getOriginRelativePosZ.replace(iterator);
                                patch3Complete = true;
                            }
                            else if (patch4Complete
                                    && !patch4Point5Complete
                                    && Transformer.EntityPlayerSP$rotationYaw_GET.is(fieldInsnNode)) {
                                Transformer.Hooks$getRelativeYaw.replace(iterator);
                                patch4Point5ReplacementCount++;
                                if (patch4Point5ReplacementCount >= 2) {
                                    patch4Point5Complete = true;
                                }
                            }
                            //newPATCH #10
                            else if (patch9Complete
                                    && axisAlignedBBmaxYCount < 2
                                    && Transformer.AxisAlignedBB$maxY_GET.is(fieldInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeTopOfBB.addTo(iterator);
                                axisAlignedBBmaxYCount++;
                            }
                            //newPATCH #11
                            else if (axisAlignedBBmaxYCount == 2
                                    && axisAlignedBBminYCount < 2
                                    && Transformer.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeBottomOfBB.addTo(iterator);
                                axisAlignedBBminYCount++;
                            }
                        }
                    }
//                    addVectorReplacements
//                    axisAlignedBBmaxYCount
//                    axisAlignedBBminYCount
//                    blockPosUPCount
//                    blockPosUPCountI
                    Transformer.log("addVector: " + addVectorReplacements
                            + ", maxY: " + axisAlignedBBmaxYCount
                            + ", minY: " + axisAlignedBBminYCount
                            + ", up(): " + blockPosUPCount
                            + ", up(int): " + blockPosUPCountI);
                }

                if (!patch9Complete && !Transformer.DEBUG_AUTO_JUMP) {
                    Transformer.die("Unknown error occurred while patching EntityPlayerSP");
                }
                Transformer.logPatchComplete(Transformer.EntityPlayerSP$updateAutoJump_name);
                methodPatches++;
            }
            // moveEntity passes the change in X and Z movement to the updateAutoJump method, we need to pass the change in
            //      X and Z relative to the player's view instead
            //
            //      DEBUG_AUTO_JUMP instead calls super to help with debugging EntityPlayerWithGravity::updateAutoJump
            else if (Transformer.Entity$moveEntity_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.Entity$moveEntity_name);
                if (Transformer.DEBUG_AUTO_JUMP) {
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof LineNumberNode) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 1));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 3));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 5));
                            Transformer.AbstractClientPlayer$moveEntity_SPECIAL.addTo(iterator);
                            iterator.add(new InsnNode(Opcodes.RETURN));
                        }
                    }
                }
                else {
                    int prevRelativeXPos_var = -1;
                    int prevRelativeZPos_var = -1;

                    outerfor:
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.ALOAD) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.next(); // GETFIELD net/minecraft/client/entity/EntityPlayerSP.posX : D
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            Transformer.EntityPlayerSP$posY_GET.addTo(iterator);
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            Transformer.EntityPlayerSP$posZ_GET.addTo(iterator);
                            Transformer.Hooks$inverseAdjustXYZ.addTo(iterator);
                            iterator.add(new InsnNode(Opcodes.DUP));
                            iterator.add(new InsnNode(Opcodes.ICONST_0));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            //next is DSTORE 7
                            next = iterator.next();
                            if (next instanceof VarInsnNode) {
                                prevRelativeXPos_var = ((VarInsnNode)next).var;
                            }
                            else {
                                Transformer.die("Was expecting DSTORE _ after GETFIELD posX, instead got " + next);
                            }

                            while (true) {
                                next = iterator.next();
                                if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.ALOAD) {
                                    //ALOAD 0
                                    iterator.remove();
                                    iterator.next(); //GETFIELD net/minecraft/client/entity/EntityPlayerSP.posZ : D
                                    iterator.remove();
                                    iterator.add(new InsnNode(Opcodes.ICONST_2));
                                    iterator.add(new InsnNode(Opcodes.DALOAD));
                                    //next is DSTORE 9
                                    next = iterator.next();
                                    if (next instanceof VarInsnNode) {
                                        prevRelativeZPos_var = ((VarInsnNode)next).var;
                                    }
                                    else {
                                        Transformer.die("Was expecting DSTORE _ after GETFIELD posZ, instead got " + next);
                                    }

                                    while (true) {
                                        next = iterator.next();
                                        if (next instanceof FieldInsnNode) {
                                            FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                                            if (Transformer.EntityPlayerSP$posX_GET.is(fieldInsnNode)) {
                                                iterator.previous();
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                iterator.next(); // same GETFIELD instruction
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                Transformer.EntityPlayerSP$posY_GET.addTo(iterator);
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                Transformer.EntityPlayerSP$posZ_GET.addTo(iterator);
                                                Transformer.Hooks$inverseAdjustXYZ.addTo(iterator);
                                                iterator.add(new InsnNode(Opcodes.DUP));
                                                iterator.add(new InsnNode(Opcodes.ICONST_2));
                                                iterator.add(new InsnNode(Opcodes.DALOAD));
                                                iterator.add(new VarInsnNode(Opcodes.DLOAD, prevRelativeZPos_var));
                                                iterator.add(new InsnNode(Opcodes.DSUB));
                                                iterator.add(new VarInsnNode(Opcodes.DSTORE, prevRelativeZPos_var));
                                                iterator.add(new InsnNode(Opcodes.ICONST_0));
                                                iterator.add(new InsnNode(Opcodes.DALOAD));
                                                iterator.add(new VarInsnNode(Opcodes.DLOAD, prevRelativeXPos_var));
                                                iterator.add(new InsnNode(Opcodes.DSUB));
                                                iterator.add(new InsnNode(Opcodes.D2F));
                                                iterator.add(new VarInsnNode(Opcodes.DLOAD, prevRelativeZPos_var));
                                                iterator.add(new InsnNode(Opcodes.D2F));

                                                while (true) {
                                                    next = iterator.next();
                                                    if (next.getOpcode() == INVOKEVIRTUAL) {
                                                        //INVOKEVIRTUAL net/minecraft/client/entity/EntityPlayerSP.func_189810_i (FF)V // Auto-jump method
                                                        break outerfor;
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
                        }
                    }
                    /*
                    // access flags 0x1
                      public moveEntity(DDD)V
                       L0
                        LINENUMBER 602 L0
                        ALOAD 0
                        ALOAD 0
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posX : D
                        ALOAD 0
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posY : D
                        ALOAD 0
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posZ : D
                        INVOKESTATIC uk/co/mysterymayhem/gravitymod/asm/Hooks.inverseAdjustXYZ (Lnet/minecraft/entity/Entity;DDD)[D
                        DUP
                        ICONST_0
                        DALOAD
                        DSTORE 7
                       L1
                        LINENUMBER 604 L2
                        ICONST_2
                        DALOAD
                        DSTORE 9
                       L2
                        LINENUMBER 605 L3
                        ALOAD 0
                        DLOAD 1
                        DLOAD 3
                        DLOAD 5
                        INVOKESPECIAL net/minecraft/entity/player/EntityPlayer.moveEntity (DDD)V
                       L3
                        LINENUMBER 606 L4
                        ALOAD 0
                          this
                        ALOAD 0
                          this, this
                        ALOAD 0
                          this, this, this
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posX : D
                          this, this, posX
                        ALOAD 0
                          this, this, posX, this
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posY : D
                          this, this, posX, posY
                        ALOAD 0
                          this, this, posX, posY, this
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posZ : D
                          this, this, posX, posY, posZ
                        INVOKESTATIC uk/co/mysterymayhem/gravitymod/asm/Hooks.inverseAdjustXYZ (Lnet/minecraft/entity/Entity;DDD)[D
                          this, doubles
                        DUP
                          this, doubles, doubles
                        ICONST_2
                          this, doubles, doubles, 2
                        DALOAD
                          this, doubles, doubles[2](newRelZ)
                        DLOAD 9
                          this, doubles, doubles[2](newRelZ), d1(oldRelZ)
                        DSUB
                          this, doubles, doubles[2](newRelZ)-d1(oldRelZ)
                        DSTORE 9
                          this, doubles
                        ICONST_0
                          this, doubles, 0
                        DALOAD
                          this, doubles[0](newRelX)
                        DLOAD 7
                          this, doubles[0](newRelX), d0(oldRelX)
                        DSUB
                          this, doubles[0](newRelX)-d0(oldRelX)
                        D2F
                          this, (float)doubles[0](newRelX)-d0(oldRelX)
                        DLOAD 9
                          this, (float)doubles[0](newRelX)-d0(oldRelX), doubles[2](newRelZ)-d1(oldRelZ)
                        D2F
                          this, (float)doubles[0](newRelX)-d0(oldRelX), (float)doubles[2](newRelZ)-d1(oldRelZ)
                        INVOKEVIRTUAL uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.func_189810_i (FF)V
                          <empty>
                       L4
                        LINENUMBER 608 L6
                        RETURN
                       L5
                     */
                }
                Transformer.logPatchComplete(Transformer.Entity$moveEntity_name);
                methodPatches++;
            }
            // TODO: Patch pushOutOfBlocks
        }

        Transformer.dieIfFalse(methodPatches == expectedMethodPatches, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);

        return classWriter.toByteArray();

    }
}
