package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import uk.co.mysterymayhem.gravitymod.asm.Ref;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.ObfuscationHelper;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.InsnPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.MethodPatcher;

import java.util.ListIterator;

/**
 * When syncing player position between the client and server, the server moves the player by the difference
 * between their server-side position and position received in a packet sent by the player. But player movement
 * is done in a relative manner, so the arguments supplied to moveEntity must be adjusted for the player's
 * current gravity.
 * <p>
 * In a similar manner, the player has handleFalling called on them, but the vanilla code passes the change
 * in Y position as one of the arguments, this has to be changed to a change in the relative upwards
 * direction of the player.
 * <p>
 * Created by Mysteryem on 2017-01-31.
 */
public class PatchNetHandlerPlayServer extends ClassPatcher {
    private static final String xDiffLocalVarKey = "xDiffLocalVar";
    private static final String yDiffLocalVarKey = "yDiffLocalVar";
    private static final String zDiffLocalVarKey = "zDiffLocalVar";

    public PatchNetHandlerPlayServer() {
        super("net.minecraft.network.NetHandlerPlayServer", 0, ClassWriter.COMPUTE_MAXS);

        // When syncing player position between the client and server, the server moves the player by the difference between their server-side position and
        // position received in a packet sent by the player. But player movement is done in a relative manner, so the arguments supplied to moveEntity must
        // be adjusted for the player's current gravity.
        //
        // In a similar manner, the player has handleFalling called on them, but the vanilla code passes the change in Y position as one of the arguments,
        // this has to be changed to a change in the relative upwards direction of the player.
        this.addMethodPatch(new ProcessPlayer());

        // When the server checks if a player can actually reach a block they're trying to break, instead of getting the player's eye height and adding that
        // to their position, for some reason, Mojang have it hardcoded to xPos, yPos + 1.5, zPos
        //
        // This patch removes the "+ 1.5" and replaces it with "this.playerEntity.getEyeHeight()".
        // The patch is very much needed with this mod, as upwards gravity gives the player a negative eye height for example
        //
        // This behaviour should actually be buggy in vanilla, as sneaking changes the player's eye height slightly
        this.addMethodPatch(
                Ref.INetHandlerPlayServer$processPlayerDigging_name::is,
                ((next, iterator) -> {
                    if (next instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode)next;
                        Object object = ldcInsnNode.cst;
                        if (object instanceof Double) {
                            Double d = (Double)object;
                            if (d == 1.5D) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this.
                                Ref.NetHandlerPlayServer$playerEntity_GET.addTo(iterator); //this.playerEntity
                                Ref.EntityPlayerMP$getEyeHeight.addTo(iterator); //this.playerEntity.getEyeHeight()
                                iterator.add(new InsnNode(Opcodes.F2D)); //(double)this.playerEntity.getEyeHeight()
                                return true;
                            }
                        }
                    }
                    return false;
                }));
    }

    private class ProcessPlayer extends MethodPatcher {

        private int arrayLocalVarIndex = -1;
        private int extraDoubleVar = -1;

        public ProcessPlayer() {
            // The player has handleFalling called on them, but the vanilla code passes the change in Y position as one of the arguments, this has to be
            // changed to a change in the relative upwards direction of the player.
            this.addInsnPatch(this::fixHandleFallingToUseRelativeYPosition);

            // -------------------------------------------------------------------------------------------------------------------------------------------------

            // When syncing player position between the client and server, the server moves the player by the difference between their server-side position
            // and position received in a packet sent by the player. But player movement is done in a relative manner, so the arguments supplied to
            // moveEntity must be adjusted for the player's current gravity.

            // We use a 'patcher' to find the local variables that store the difference between server-side player position and 'packet-side' player position
            // The indices for the x, y and z difference variables are stored in the owning PatchNetHandlerPlayServer's data storage
            InsnPatcher findLocalVarIndices = this.addInsnPatch(this::findXYZDifferenceVariables);

            // The local variable that stores the difference in the y direction is loaded 5 times.
            // For all but the second occurrence, we need to change the value that gets put on the top of the stack to be the _relative_ y difference

            // For the first occurrence we only have to change the 'y difference' to be the 'relative y difference'
            InsnPatcher firstYFound = this.addInsnPatch((node, iterator) -> {
                if (this.isYDiffLoadInsn(node)) {
                    this.replaceAbsoluteYDifferenceWithRelative(iterator);
                    return true;
                }
                return false;
            });

            // On the second occurrence, we have a no-op
            InsnPatcher secondYFound = this.addInsnPatch((node, iterator) -> this.isYDiffLoadInsn(node));

            // The third occurrence requires the same as the first, so we just make a copy of the first one
            InsnPatcher thirdYFound = this.addInsnPatch(firstYFound.copy());

            // The fourth occurrence, the variables for x and y have been modified by the original code. The variable for z gets updated just after, but the
            // updated y variable gets used immediately, but we need the z variable to also be updated in order to calculate the 'relative y difference'
            // correctly.
            //
            // So we update the z variable ahead of normal time using an extra, prepended method
            InsnPatcher fourthYFound = this.addInsnPatch((node, iterator) -> {
                if (this.isYDiffLoadInsn(node)) {
                    this.setZDifferenceEarly(iterator);
                    this.replaceAbsoluteYDifferenceWithRelative(iterator);
                }
                return false;
            });

            // After the fifth occurrence, the variable for y is usually set to zero, but we need to set whichever of the x/y/z variables corresponds to
            // 'relative y' to zero instead
            InsnPatcher fifthYFound = this.addInsnPatch((node, iterator) -> {
                if (this.isYDiffLoadInsn(node)) {
                    this.replaceAbsoluteYDifferenceWithRelative(iterator);
                    this.setRelativeYDiffToZeroInsteadOfAbsoluteYDiff(iterator);
                }
                return false;
            });

            // Child patches only become 'active' once their parent patches have been completed (patcher won't even try to apply inactive patches)
            findLocalVarIndices.addChildPatch(firstYFound);
            firstYFound.addChildPatch(secondYFound);
            secondYFound.addChildPatch(thirdYFound);
            thirdYFound.addChildPatch(fourthYFound);
            fourthYFound.addChildPatch(fifthYFound);
        }

        /**
         * This checks that the current AbstractInsnNode is a DLOAD instruction and is loading the 'difference in y' variable
         *
         * @param node
         * @return
         */
        private boolean isYDiffLoadInsn(AbstractInsnNode node) {
            if (node.getOpcode() == Opcodes.DLOAD/* && node instanceof VarInsnNode*/) {
                VarInsnNode varInsnNode = (VarInsnNode)node;
                return varInsnNode.var == (Integer)PatchNetHandlerPlayServer.this.getData(yDiffLocalVarKey);
            }
            return false;
        }

        /**
         * Instead of loading the local variable that stores the difference in y, we calculate and load the difference in 'relative y'
         *
         * @param iterator
         */
        private void replaceAbsoluteYDifferenceWithRelative(ListIterator<AbstractInsnNode> iterator) {
            int xDiffLocalVar = (Integer)PatchNetHandlerPlayServer.this.getData(xDiffLocalVarKey);
            int yDiffLocalVar = (Integer)PatchNetHandlerPlayServer.this.getData(yDiffLocalVarKey);
            int zDiffLocalVar = (Integer)PatchNetHandlerPlayServer.this.getData(zDiffLocalVarKey);

            //get relative y
            iterator.remove();
            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
            iterator.add(new VarInsnNode(Opcodes.DLOAD, xDiffLocalVar)); //this, xDiff
            iterator.add(new VarInsnNode(Opcodes.DLOAD, yDiffLocalVar)); //this, xDiff, yDiff
            iterator.add(new VarInsnNode(Opcodes.DLOAD, zDiffLocalVar)); //this, xDiff, yDiff, zDiff
            Ref.Hooks$netHandlerPlayServerGetRelativeY.addTo(iterator); //relativeY
        }

        private void setZDifferenceEarly(ListIterator<AbstractInsnNode> iterator) {
            int zDiffLocalVar = (Integer)PatchNetHandlerPlayServer.this.getData(zDiffLocalVarKey);

            // run "d9 = d6 - this.playerEntity.posZ;" early so that d9 is given the correct value
            // for when it is passed to Hooks$netHandlerPlayServerGetRelativeY which will be
            // called instead of loading [yDiffLocalVar]/[d8]/[21]
            iterator.previous(); // the DLOAD [yDiffLocalVar]/[21] instruction

            // Get z from the packet, (instead of tracking the localvariable's index)
            // In which case, you would add this:
            //iterator.add(new VarInsnNode(Opcodes.DLOAD, [zPosFromPacket]));
            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
            iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); //this, packetIn
            Ref.Hooks$netHandlerPlayServerGetPacketZ.addTo(iterator); //packetIn.getZ(this.playerEntity.posZ), same as local variable d6
            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));//'d6', this
            Ref.NetHandlerPlayServer$playerEntity_GET.addTo(iterator);//'d6', this.playerEntity
            Ref.EntityPlayerMP$posZ_GET.addTo(iterator);//'d6', this.playerEntity.posZ
            iterator.add(new InsnNode(Opcodes.DSUB));//'d6' - this.playerEntity.posZ
            iterator.add(new VarInsnNode(Opcodes.DSTORE, zDiffLocalVar));//[empty]
            iterator.next(); // back to the DLOAD [yDiffLocalVar]/[21] instruction
        }

        private void setRelativeYDiffToZeroInsteadOfAbsoluteYDiff(ListIterator<AbstractInsnNode> iterator) {
            int xDiffLocalVar = (Integer)PatchNetHandlerPlayServer.this.getData(xDiffLocalVarKey);
            int yDiffLocalVar = (Integer)PatchNetHandlerPlayServer.this.getData(yDiffLocalVarKey);
            int zDiffLocalVar = (Integer)PatchNetHandlerPlayServer.this.getData(zDiffLocalVarKey);

            // Find the first DCONST_0 instruction, it will be followed be DSTORE [yDiffLocalVar]
            // Go back to before the DCONST_0, save the value of [yDiffLocalVar]
            // Allow [yDiffLocalVar] to be set to zero at per normal
            // Restore [yDiffLocalVar] back to its original value
            // Set the relative y difference to 0 instead
            while (true) {
                if (iterator.next().getOpcode() == Opcodes.DCONST_0) {
                    iterator.previous(); // DCONST_0 again
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, yDiffLocalVar));
                    iterator.add(new VarInsnNode(Opcodes.ASTORE, this.extraDoubleVar));

                    // Skip over the instructions we're basically ignoring
                    iterator.next(); // DCONST_0 again
                    iterator.next(); // DSTORE [yDiffLocalVar]

                    // Put [yDiffLocalVar] back to normal
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, this.extraDoubleVar));
                    iterator.add(new VarInsnNode(Opcodes.ASTORE, yDiffLocalVar));

                    // Load this and the localvariables
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
                    iterator.add(new VarInsnNode(Opcodes.DLOAD, xDiffLocalVar)); //this, xDiff
                    iterator.add(new VarInsnNode(Opcodes.DLOAD, yDiffLocalVar)); //this, xDiff, yDiff
                    iterator.add(new VarInsnNode(Opcodes.DLOAD, zDiffLocalVar)); //this, xDiff, yDiff, zDiff
                    // Call the hook that returns an array of size == 3
                    Ref.Hooks$netHandlerPlayServerSetRelativeYToZero.addTo(iterator); //double[]

                    iterator.add(new VarInsnNode(Opcodes.ASTORE, this.arrayLocalVarIndex)); //[empty]

                    // double[0] -> xDiffLocalVar
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, this.arrayLocalVarIndex)); //double[]
                    iterator.add(new InsnNode(Opcodes.ICONST_0)); //double[], 0
                    iterator.add(new InsnNode(Opcodes.DALOAD)); //double[0]
                    iterator.add(new VarInsnNode(Opcodes.DSTORE, xDiffLocalVar)); //[empty]

                    // double[1] -> yDiffLocalVar
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, this.arrayLocalVarIndex)); //double[]
                    iterator.add(new InsnNode(Opcodes.ICONST_1)); //double[], 1
                    iterator.add(new InsnNode(Opcodes.DALOAD)); //double[1]
                    iterator.add(new VarInsnNode(Opcodes.DSTORE, yDiffLocalVar)); //[empty]

                    // double[2] -> zDiffLocalVar
                    iterator.add(new VarInsnNode(Opcodes.ALOAD, this.arrayLocalVarIndex)); //double[]
                    iterator.add(new InsnNode(Opcodes.ICONST_2)); //double[], 2
                    iterator.add(new InsnNode(Opcodes.DALOAD)); //double[2]
                    iterator.add(new VarInsnNode(Opcodes.DSTORE, zDiffLocalVar)); //[empty]
                    break;
                }
            }
        }

        @Override
        protected void patchMethod(MethodNode methodNode) {
            this.arrayLocalVarIndex = Transformer.addLocalVar(methodNode, ObfuscationHelper.DOUBLE.asArray());
            this.extraDoubleVar = Transformer.addLocalVar(methodNode, ObfuscationHelper.DOUBLE);
            super.patchMethod(methodNode);
        }

        @Override
        protected boolean shouldPatchMethod(MethodNode methodNode) {
            return Ref.INetHandlerPlayServer$processPlayer_name.is(methodNode);
        }

        /**
         * The player has handleFalling called on them, but the vanilla code passes the change
         * in Y position as one of the arguments, this has to be changed to a change in the relative upwards
         * direction of the player.
         *
         * @param node
         * @param iterator
         * @return
         */
        private boolean fixHandleFallingToUseRelativeYPosition(AbstractInsnNode node, ListIterator<AbstractInsnNode> iterator) {
            if (Ref.EntityPlayerMP$handleFalling.is(node)) {
                iterator.previous(); // INVOKEVIRTUAL handleFalling again (iterator.previous() == node)
                iterator.previous(); // INVOKEVIRTUAL isOnGround
                iterator.previous(); // ALOAD 1 (CPacketPlayer method argument)
                AbstractInsnNode previous = iterator.previous(); // Should be DSUB
                if (previous.getOpcode() == Opcodes.DSUB) {
                    iterator.remove();
                    VarInsnNode dload7 = new VarInsnNode(Opcodes.DLOAD, 7);
                    iterator.add(dload7);
                    Ref.Hooks$netHandlerPlayServerHandleFallingYChange.addTo(iterator);
                    iterator.previous(); // Our newly added INVOKESTATIC
                    iterator.previous(); // Our newly added DLOAD 7
                    iterator.previous(); // DLOAD 9
                    previous = iterator.previous(); // GETFIELD posY
                    if (previous instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)previous;
                        if (Ref.EntityPlayerMP$posY_GET.is(fieldInsnNode)) {
                            VarInsnNode dload3 = new VarInsnNode(Opcodes.DLOAD, 3);
                            iterator.remove();
                            iterator.add(dload3);
                            return true;
                        }
                        else {
                            Transformer.die("Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"GETFIELD posY\"");
                        }
                    }
                    else {
                        Transformer.die("Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"GETFIELD posY\"");
                    }
                }
                else {
                    Transformer.die("Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"DSUB\"");
                }
            }
            return false;
        }

        private boolean findXYZDifferenceVariables(AbstractInsnNode node, ListIterator<AbstractInsnNode> iterator) {
            if (Ref.NetHandlerPlayServer$lastGoodX_GET.is(node)) {
                for (int foundCount = 0; foundCount < 3; ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.DSTORE) {
                        VarInsnNode varInsnNode = (VarInsnNode)next;
                        switch (foundCount) {
                            case 0:
                                PatchNetHandlerPlayServer.this.storeData(xDiffLocalVarKey, varInsnNode.var);
                                break;
                            case 1:
                                PatchNetHandlerPlayServer.this.storeData(yDiffLocalVarKey, varInsnNode.var);
                                break;
                            case 2:
                                PatchNetHandlerPlayServer.this.storeData(zDiffLocalVarKey, varInsnNode.var);
                                return true;
                        }
                        foundCount++;
                    }
                }
                Transformer.die("Finding x/y/z difference variables failed");
            }
            return false;
        }
    }
}
