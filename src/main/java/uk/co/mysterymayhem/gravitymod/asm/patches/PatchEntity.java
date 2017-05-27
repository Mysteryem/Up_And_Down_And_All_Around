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

/**
 * Created by Mysteryem on 2017-02-02.
 */
public class PatchEntity extends ClassPatcher {
    public PatchEntity() {
        super("net.minecraft.entity.Entity", 0, ClassWriter.COMPUTE_FRAMES);

        // Movement of the player is based off of their yaw and their forwards and strafing input
        // We need to get the relative yaw when calculating the resultant movement due to strafe/forwards input
        this.addMethodPatch(Ref.Entity$moveRelative_name::is,
                methodNode -> Transformer.patchMethodUsingAbsoluteRotations(methodNode, Transformer.GET_ROTATIONYAW));

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
        this.addMethodPatch(Ref.Entity$move_name::is, methodNode -> {
            int numReplaced = 0;
            for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); /**/) {
                AbstractInsnNode node = iterator.next();
                if (node instanceof MethodInsnNode) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode)node;
                    if (Ref.AxisAlignedBB$calculateXOffset.is(methodInsnNode)) {
                        Ref.Hooks$reverseXOffset.replace(methodInsnNode);
                        numReplaced++;
                    }
                    else if (Ref.AxisAlignedBB$calculateYOffset.is(methodInsnNode)) {
                        Ref.Hooks$reverseYOffset.replace(methodInsnNode);
                        numReplaced++;
                    }
                    else if (Ref.AxisAlignedBB$calculateZOffset.is(methodInsnNode)) {
                        Ref.Hooks$reverseZOffset.replace(methodInsnNode);
                        numReplaced++;
                    }
                }
            }
            if (numReplaced < 1) {
                Transformer.die("Failed to find any calculateX/Y/Z method instructions in " + Ref.Entity$move_name.getDeobf());
            }
        });

        MethodPatcher secondMoveEntityPatch = this.addMethodPatch(Ref.Entity$move_name::is);

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
        InsnPatcher findNewBlockPos = secondMoveEntityPatch.addInsnPatch((node, iterator) -> {
            if (Ref.BlockPos$INIT.is(node)) {
                // Starts with a simple 1:1 replacement
                Ref.Hooks$getImmutableBlockPosBelowEntity.replace(iterator);
                iterator.previous(); // Our INNVOKESTATIC instruction

                // Repeatedly remove the previous instruction until it's GETFIELD Entity.posX
                // Remove that too
                // If we were to call iterator.previous() again, we would get the ALOAD 0 that would normally
                // be consumed by the GETFIELD instruction we just removed
                // And it just so happens that our INVOKESTATIC instruction needs to have ALOAD 0 before it
                // So we cut out a bunch of no longer used instructions and inserted our own instruction
                // (hopefully it's fine to have local variables that are never used in bytecode)
                for (; iterator.hasPrevious(); ) {
                    AbstractInsnNode previous = iterator.previous();
                    if (Ref.Entity$posX_GET.is(previous)) {
                        iterator.remove();
                        return true;
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

                Transformer.die("Failed to find " + Ref.Entity$posX_GET + " prior to " + Ref.BlockPos$INIT);
            }
            return false;
        });

        // TODO: Change/remove this
        // If the block ~0.2 below (relative) the player's feet is air, then vanilla checks if the block
        // below that is a fence/wall/fence gate
        InsnPatcher findBlockPos$down = secondMoveEntityPatch.addInsnPatch((node, iterator) -> {
            if (Ref.BlockPos$down_NO_ARGS.is(node)) {
                Ref.Hooks$getRelativeDownBlockPos.replace(iterator);
                iterator.previous();
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                return true;
            }
            return false;
        });

        // When the player moves, minecraft tracks how far they've moved and uses it to work out the distance
        // they've walked, which is used to play step sounds and water 'sloshing' sounds and it might be used
        // to do walking animations too.
        //
        // We fix this by loading the local variables that store the change in X/Y/Z, adjusting them and then
        // storing the adjusted values back into the local variables
        InsnPatcher findBlocks$LADDERFound = secondMoveEntityPatch.addInsnPatch((node, iterator) -> {
            if (Ref.Blocks$LADDER_GET.is(node)) {
                iterator.previous(); // returns the same GETSTATIC instruction
                iterator.previous(); // should be ALOAD 29

                int countToUndo = 0;
                int posZVar = -1; //33/39
                int posYVar = -1; //31/76 // Some interesting changes in compiler optimisation since 1.10.2
                int posXVar = -1; //29/74

                int foundStores = 0;
                while (foundStores < 3) {
                    countToUndo++;
                    AbstractInsnNode previous = iterator.previous();
                    if (previous.getOpcode() == Opcodes.DSTORE) {
                        foundStores++;
                        switch (foundStores) {
                            case 1:
                                posZVar = ((VarInsnNode)previous).var;
                                break;
                            case 2:
                                posYVar = ((VarInsnNode)previous).var;
                                break;
                            case 3:
                                posXVar = ((VarInsnNode)previous).var;
                                break;
                        }
                    }
                }
                while (countToUndo > 0) {
                    iterator.next();
                    countToUndo--;
                }


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
                Ref.Hooks$inverseAdjustXYZ.addTo(iterator);
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
                return true;
            }
            return false;
        });

        InsnPatcher.sequentialOrder(findNewBlockPos, findBlockPos$down, findBlocks$LADDERFound);
    }
}
