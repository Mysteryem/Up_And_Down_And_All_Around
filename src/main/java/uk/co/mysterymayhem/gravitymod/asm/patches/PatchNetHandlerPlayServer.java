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
public class PatchNetHandlerPlayServer implements Function<byte[], byte[]> {
    //TODO: processPlayer patch is still using hardcoded localvariable indices
    @Override
    public byte[] apply(byte[] bytes) {
        int methodPatches = 0;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            // When syncing player position between the client and server, the server moves the player by the difference
            // between their server-side position and position received in a packet sent by the player. But player movement
            // is done in a relative manner, so the arguments supplied to moveEntity must be adjusted for the player's
            // current gravity.
            //
            // In a similar manner, the player has handleFalling called on them, but the vanilla code passes the change
            // in Y position as one of the arguments, this has to be changed to a change in the relative upwards
            // direction of the player.
            if (Transformer.INetHandlerPlayServer$processPlayer_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.INetHandlerPlayServer$processPlayer_name);

                boolean foundHandleFalling = false;

                boolean foundDifferenceLocalVars = false;
                int xDiffLocalVar = -1;
                int yDiffLocalVar = -1;
                int zDiffLocalVar = -1;
                int yDiffLoadCount = 0;
                boolean patchedXYZDiffLocalVars = false;

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        // processPlayer calls handleFalling(...) with the player's change in Y position
                        // We insert a hook that calculates the player's relative Y change and deletes the instructions
                        // that calculate the change in Y position (this.playerEntity.posY - d3)
                        if (!foundHandleFalling && Transformer.EntityPlayerMP$handleFalling.is(methodInsnNode)) {
                            iterator.previous(); // INVOKEVIRTUAL handleFalling again
                            iterator.previous(); // INVOKEVIRTUAL isOnGround
                            iterator.previous(); // ALOAD 1 (CPacketPlayer method argument)
                            next = iterator.previous(); // Should be DSUB
                            if (next.getOpcode() == Opcodes.DSUB) {
                                iterator.remove();
                                VarInsnNode dload7 = new VarInsnNode(Opcodes.DLOAD, 7);
                                iterator.add(dload7);
                                Transformer.Hooks$netHandlerPlayServerHandleFallingYChange.addTo(iterator);
                                iterator.previous(); // Our newly added INVOKESTATIC
                                iterator.previous(); // Our newly added DLOAD 7
                                iterator.previous(); // DLOAD 9
                                next = iterator.previous(); // GETFIELD posY
                                if (next instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                                    if (Transformer.EntityPlayerMP$posY_GET.is(fieldInsnNode)) {
                                        VarInsnNode dload3 = new VarInsnNode(Opcodes.DLOAD, 3);
                                        iterator.remove();
                                        iterator.add(dload3);
                                        foundHandleFalling = true;
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
                    }
                    else if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        // We use the lastGoodX field access as a basis for finding the local variable indices we need
                        if (!foundDifferenceLocalVars && Transformer.NetHandlerPlayServer$lastGoodX_GET.is(fieldInsnNode)) {
                            foundDifferenceLocalVars = true;
                            for (int foundCount = 0; foundCount < 3; ) {
                                next = iterator.next();
                                if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.DSTORE) {
                                    VarInsnNode varInsnNode = (VarInsnNode)next;
                                    switch (foundCount) {
                                        case 0:
                                            xDiffLocalVar = varInsnNode.var;
                                            break;
                                        case 1:
                                            yDiffLocalVar = varInsnNode.var;
                                            break;
                                        case 2:
                                            zDiffLocalVar = varInsnNode.var;
                                            break;
                                    }
                                    foundCount++;
                                }
                            }
                        }
                    }
                    else if (next instanceof VarInsnNode) {
                        VarInsnNode varInsnNode = (VarInsnNode)next;
                        // Some parts of processPlayer need the change in X/Y/Z local variables to be absolute, while
                        // some need them relative, so we change the instructions where they get loaded as necessary.
                        if (foundDifferenceLocalVars
                                && !patchedXYZDiffLocalVars
                                && varInsnNode.var == yDiffLocalVar
                                && varInsnNode.getOpcode() == Opcodes.DLOAD) {
                            yDiffLoadCount++;
                            switch (yDiffLoadCount) {
                                case 4://d8 > -0.5D

                                    // run "d9 = d6 - this.playerEntity.posZ;" early so that d9 is given the correct value
                                    // for when it is passed to Hooks$netHandlerPlayServerGetRelativeY which will be
                                    // called instead of loading [yDiffLocalVar]/[d8]/[21]
                                    iterator.previous(); // the DLOAD [yDiffLocalVar]/[21] instruction

                                    // Get z from the packet, (instead of tracking the localvariable's index)
                                    // In which case, you would add this:
                                    //iterator.add(new VarInsnNode(Opcodes.DLOAD, [zPosFromPacket]));
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); //this, packetIn
                                    Transformer.Hooks$netHandlerPlayServerGetPacketZ.addTo(iterator); //packetIn.getZ(this.playerEntity.posZ), same as local variable d6
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));//'d6', this
                                    Transformer.NetHandlerPlayServer$playerEntity_GET.addTo(iterator);//'d6', this.playerEntity
                                    Transformer.EntityPlayerMP$posZ_GET.addTo(iterator);//'d6', this.playerEntity.posZ
                                    iterator.add(new InsnNode(Opcodes.DSUB));//'d6' - this.playerEntity.posZ
                                    iterator.add(new VarInsnNode(Opcodes.DSTORE, zDiffLocalVar));//[empty]
                                    iterator.next(); // back to the DLOAD [yDiffLocalVar]/[21] instruction

                                case 1://if (this.playerEntity.onGround && !packetIn.isOnGround() && d8 > 0.0D)
                                case 3://double d12 = d8;
                                case 5://d8 < 0.5D
                                    //get relative y
                                    iterator.remove();
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
                                    iterator.add(new VarInsnNode(Opcodes.DLOAD, xDiffLocalVar)); //this, xDiff
                                    iterator.add(new VarInsnNode(Opcodes.DLOAD, yDiffLocalVar)); //this, xDiff, yDiff
                                    iterator.add(new VarInsnNode(Opcodes.DLOAD, zDiffLocalVar)); //this, xDiff, yDiff, zDiff
                                    Transformer.Hooks$netHandlerPlayServerGetRelativeY.addTo(iterator); //relativeY
                                    break;
                                //case 2://this.playerEntity.moveEntity(d7, d8, d9);
                                // no change, as we're going to call moveEntityAbsolute hook, or the method will take
                                // care of the absolute arguments on its own when using the MotionStack (NYI)
                                // Either way, we'll delegate this to the default case
                                default:
                                    break;
                            }

                            // After patching the 5th load, (assuming we jump inside the if statement), vanilla code does d8 = 0.0D;
                            // We need to set the local variable which coincides with the player's relative y axis to
                            // zero, instead of always the absolute y axis
                            if (yDiffLoadCount == 5) {
                                while (true) {
                                    if (iterator.next().getOpcode() == Opcodes.DCONST_0) {
                                        iterator.remove(); // remove the first instruction
                                        iterator.next(); // DSTORE [yDiffLocalVar]
                                        iterator.remove(); // remove the DSTORE instruction

                                        // Load this and the localvariables
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
                                        iterator.add(new VarInsnNode(Opcodes.DLOAD, xDiffLocalVar)); //this, xDiff
                                        iterator.add(new VarInsnNode(Opcodes.DLOAD, yDiffLocalVar)); //this, xDiff, yDiff
                                        iterator.add(new VarInsnNode(Opcodes.DLOAD, zDiffLocalVar)); //this, xDiff, yDiff, zDiff
                                        // Call the hook that returns an array of size == 3
                                        Transformer.Hooks$netHandlerPlayServerSetRelativeYToZero.addTo(iterator); //double[]

                                        // So we don't need to create any extra local variables, we duplicate the array reference
                                        // on the top of the stack
                                        iterator.add(new InsnNode(Opcodes.DUP)); //double[], double[]
                                        iterator.add(new InsnNode(Opcodes.DUP)); //double[], double[], double[]

                                        // double[0] -> xDiffLocalVar
                                        iterator.add(new InsnNode(Opcodes.ICONST_0)); //double[], double[], double[], 0
                                        iterator.add(new InsnNode(Opcodes.DALOAD)); //double[], double[], double[0]
                                        iterator.add(new VarInsnNode(Opcodes.DSTORE, xDiffLocalVar)); //double[], double[]

                                        // double[1] -> yDiffLocalVar
                                        iterator.add(new InsnNode(Opcodes.ICONST_1)); //double[], double[], 1
                                        iterator.add(new InsnNode(Opcodes.DALOAD)); //double[], double[1]
                                        iterator.add(new VarInsnNode(Opcodes.DSTORE, yDiffLocalVar)); //double[]

                                        // double[2] -> zDiffLocalVar
                                        iterator.add(new InsnNode(Opcodes.ICONST_2)); //double[], 2
                                        iterator.add(new InsnNode(Opcodes.DALOAD)); //double[2]
                                        iterator.add(new VarInsnNode(Opcodes.DSTORE, zDiffLocalVar)); //[empty]

                                        patchedXYZDiffLocalVars = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                Transformer.dieIfFalse(patchedXYZDiffLocalVars, "Could not find 5 DLOADs of local variable " + yDiffLocalVar);
                Transformer.dieIfFalse(foundHandleFalling, "Could not find \"this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());\"");

                Transformer.logPatchComplete(Transformer.INetHandlerPlayServer$processPlayer_name);
                methodPatches++;
            }
            // When the server checks if a player can actually reach a block they're trying to break, instead of getting
            // the player's eye height and adding that to their position, for some reason, Mojang have it hardcoded to
            // xPos, yPos + 1.5, zPos
            //
            // This patch removes the "+ 1.5" and replaces it with "this.playerEntity.getEyeHeight()".
            // The patch is very much needed with this mod, as upwards gravity gives the player a negative eye height
            // for example
            //
            // This behaviour should actually be buggy in vanilla, as sneaking changes the player's eye height slightly
            else if (Transformer.INetHandlerPlayServer$processPlayerDigging_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.INetHandlerPlayServer$processPlayerDigging_name);

                boolean patched1point5ToGetEyeHeight = false;

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode)next;
                        Object object = ldcInsnNode.cst;
                        if (object instanceof Double) {
                            Double d = (Double)object;
                            if (d == 1.5D) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this.
                                Transformer.NetHandlerPlayServer$playerEntity_GET.addTo(iterator); //this.playerEntity
                                Transformer.EntityPlayerMP$getEyeHeight.addTo(iterator); //this.playerEntity.getEyeHeight()
                                iterator.add(new InsnNode(Opcodes.F2D)); //(double)this.playerEntity.getEyeHeight()
                                patched1point5ToGetEyeHeight = true;
                                break;
                            }
                        }
                    }
                }
                Transformer.dieIfFalse(patched1point5ToGetEyeHeight, "Could not find \"1.5D\"");

                Transformer.logPatchComplete(Transformer.INetHandlerPlayServer$processPlayerDigging_name);
                methodPatches++;
            }
        }

        Transformer.dieIfFalse(methodPatches == 2, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
