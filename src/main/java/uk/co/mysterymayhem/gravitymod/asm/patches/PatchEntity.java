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
public class PatchEntity implements Function<byte[], byte[]> {

    @Override
    public byte[] apply(byte[] bytes) {
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            // moveEntity moves the player by the arguments supplied to it or until the player collides with a block
            // in the world.
            // It also:
            // performs the player's automatic step-up (half slabs etc.),
            // keeps the player from falling off of blocks while sneaking,
            // determines if the player has collided with blocks and/or is on the ground,
            // updates the fall state (increment damage on landing if still in the air, or cause fall damage if hitting the ground),
            // increments the player's walked distance and plays step sounds if the distance is great enough,
            // and decrements the number of ticks the player should be on fire for
            //
            // We make almost everything work with non-vanilla gravity by allowing the player's motion fields to be relative
            // and then having the player's bounding box move in a relative manner through use of the GravityAxisAlignedBB class.
            //
            // There are a few cases where instead of calling an instance method on the player's bounding box (which will
            // be a GravityAxisAlignedBB), it calls the instance method on a bounding box found in the world. These are the
            // calculateX/Y/ZOffset methods, we replace their calls with static hooks that effectively run the same code, but
            // on the player's bounding box instead of those found in the world.
            if (Transformer.Entity$moveEntity_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.Entity$moveEntity_name);
                int numReplaced = 0;
                boolean newBlockPosFound = false;
                boolean blockPos$downFound = false;
                boolean Blocks$LADDERFound = false;

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (Transformer.AxisAlignedBB$calculateXOffset.is(methodInsnNode)) {
                            Transformer.Hooks$reverseXOffset.replace(methodInsnNode);
                            numReplaced++;
                        }
                        else if (Transformer.AxisAlignedBB$calculateYOffset.is(methodInsnNode)) {
                            Transformer.Hooks$reverseYOffset.replace(methodInsnNode);
                            numReplaced++;
                        }
                        else if (Transformer.AxisAlignedBB$calculateZOffset.is(methodInsnNode)) {
                            Transformer.Hooks$reverseZOffset.replace(methodInsnNode);
                            numReplaced++;
                        }
                        // To work out what block the player has landed/walked on, the vanilla method creates a BlockPos
                        // about 0.2 of a block below the player's position. We need to adjust this code so that the new
                        // BlockPos is _relatively_ about 0.2 below the player's position.
                        //
                        // In bytecode, new instances are created by first running a NEW instruction, then loading the
                        // arguments for the constructor onto the stack and then calling the constructor. We need to remove
                        // the vanilla bytecode from the NEW instruction to the INVOKESPECIAL instruction (the constructor call).
                        //
                        // I've also chosen to remove the instructions that prepare the local variables that are passed as
                        // arguments to the constructor in question, this isn't necessarry and can be removed if needed.
                        else if (!newBlockPosFound && Transformer.BlockPos$INIT.is(methodInsnNode)) {
                            // Starts with a simple 1:1 replacement
                            Transformer.Hooks$getImmutableBlockPosBelowEntity.replace(methodInsnNode);
                            iterator.previous(); // Our INNVOKESTATIC instruction

                            // Repeatedly remove the previous instruction until it's GETFIELD Entity.posX
                            // Remove that too
                            // If we were to call iterator.previous() again, we would get the ALOAD 0 that would normally
                            // be consumed by the GETFIELD instruction we just removed
                            // And it just so happens that our INVOKESTATIC instruction needs to have ALOAD 0 before it
                            // So we cut out a bunch of no longer used instructions and inserted our own instruction
                            // (hopefully it's fine to have local variables that are never used in bytecode)
                            for (; iterator.hasPrevious() && !newBlockPosFound; ) {
                                AbstractInsnNode previous = iterator.previous();
                                if (Transformer.Entity$posX_GET.is(previous)) {
                                    newBlockPosFound = true;
                                }
                                //TODO: Do the labels and line numbers need to be left in? Just the labels?
//                                if (previous instanceof LineNumberNode) {
//                                    continue;
//                                }
                                if (previous instanceof LabelNode) {
                                    continue;
                                }
                                iterator.remove();
                            }

                            if (!newBlockPosFound) {
                                Transformer.die("Failed to find " + Transformer.Entity$posX_GET + " prior to " + Transformer.BlockPos$INIT);
                            }
                        }
                        // TODO: Change/remove this
                        // If the block ~0.2 below (relative) the player's feet is air, then vanilla checks if the block
                        // below that is a fence/wall/fence gate
                        else if (newBlockPosFound && !blockPos$downFound && Transformer.BlockPos$down_NO_ARGS.is(methodInsnNode)) {
                            Transformer.Hooks$getRelativeDownBlockPos.replace(methodInsnNode);
                            iterator.previous();
                            VarInsnNode aload0 = new VarInsnNode(Opcodes.ALOAD, 0);
                            iterator.add(aload0);
                            blockPos$downFound = true;
                        }
                    }
                    // When the player moves, minecraft tracks how far they've moved and uses it to work out the distance
                    // they've walked, which is used to play step sounds and water 'sloshing' sounds and it might be used
                    // to do walking animations too.
                    //
                    // We fix this by loading the local variables that store the change in X/Y/Z, adjusting them and then
                    // storing the adjusted values back into the local variables
                    else if (next instanceof FieldInsnNode
                            && newBlockPosFound && blockPos$downFound
                            /*&& !Blocks$LADDERFound*/) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (!Blocks$LADDERFound && Transformer.Blocks$LADDER_GET.is(fieldInsnNode)) {
                            iterator.previous(); // returns the same GETSTATIC instruction
                            iterator.previous(); // should be ALOAD 29

                            int countToUndo = 0;
                            final int posZVar; //34/77
                            while (true) {
                                countToUndo++;
                                AbstractInsnNode previous = iterator.previous();
                                if (previous.getOpcode() == Opcodes.DSTORE) {
                                    posZVar = ((VarInsnNode)previous).var;
                                    break;
                                }
                            }
                            for (; countToUndo > 0; countToUndo--) {
                                iterator.next();
                            }
                            final int posYVar = posZVar - 2; //32/75
                            final int posXVar = posYVar - 2; //30/73

                            //TODO: Replace DUPs with adding a new local variable
                            // Start adding instructions just before the ALOAD 29 instruction
                            /*
                              Instruction // Top of stack
                                ALOAD 0   // this
                                DLOAD 30  // this,d12
                                DLOAD 32  // this,d12,d13
                                DLOAD 34  // this,d12,d13,d14
                                INVOKESTATIC uk/co/mysterymayhem/gravitymod/asm/Hooks.inverseAdjustXYZ (Lnet/minecraft/entity/Entity;DDD)[D
                                          // doubles
                                DUP       // doubles,doubles
                                DUP       // doubles,doubles,doubles
                                ICONST_0  // doubles,doubles,doubles,0
                                DALOAD    // doubles,doubles,doubles[0]
                                DSTORE 30 // doubles,doubles
                                ICONST_1  // doubles,doubles,1
                                DALOAD    // doubles,doubles[1]
                                DSTORE 32 // doubles
                                ICONST_2  // doubles,2
                                DALOAD    // doubles[2]
                                DSTORE 34 // [empty]
                            */
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, posXVar));//30
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, posYVar));//32
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, posZVar));//34
                            Transformer.Hooks$inverseAdjustXYZ.addTo(iterator);
                            iterator.add(new InsnNode(Opcodes.DUP));
                            iterator.add(new InsnNode(Opcodes.DUP));
                            iterator.add(new InsnNode(Opcodes.ICONST_0));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, posXVar));//30
                            iterator.add(new InsnNode(Opcodes.ICONST_1));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, posYVar));//32
                            iterator.add(new InsnNode(Opcodes.ICONST_2));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, posZVar));//34
                            Blocks$LADDERFound = true;
                        }
                    }
                }

                Transformer.dieIfFalse(numReplaced > 0, "Could not find any AxisAlignedBB:calculate[XYZ]Offset methods");
                Transformer.dieIfFalse(newBlockPosFound, "Failed to find new instance creation of BlockPos");
                Transformer.dieIfFalse(blockPos$downFound, "Failed to find usage of BlockPos::down");
                Transformer.dieIfFalse(Blocks$LADDERFound, "Failed to find usage of Blocks.LADDER");
                Transformer.logPatchComplete(Transformer.Entity$moveEntity_name);
                methodPatches++;
            }
            // Movement of the player is based off of their yaw and their forwards and strafing input
            // We need to get the relative yaw when calculating the resultant movement due to strafe/forwards input
            else if (Transformer.Entity$moveRelative_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.Entity$moveRelative_name);
                Transformer.patchMethodUsingAbsoluteRotations(methodNode, Transformer.GET_ROTATIONYAW);
                Transformer.logPatchComplete(Transformer.Entity$moveRelative_name);
                methodPatches++;
            }
        }

        Transformer.dieIfFalse(methodPatches == 2, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
