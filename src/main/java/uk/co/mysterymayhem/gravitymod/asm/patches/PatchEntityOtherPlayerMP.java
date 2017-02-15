package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import uk.co.mysterymayhem.gravitymod.asm.Ref;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.InsnPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.MethodPatcher;

/**
 * Created by Mysteryem on 15/02/2017.
 */
public class PatchEntityOtherPlayerMP extends ClassPatcher {
    private int xDiffLocalVar = -1;

    public PatchEntityOtherPlayerMP() {
        super(Ref.EntityOtherPlayerMP, 0, ClassWriter.COMPUTE_MAXS);

        MethodPatcher methodPatcher = this.addMethodPatch(Ref.Entity$onUpdate_name::is);

        InsnPatcher.sequentialOrder(
                methodPatcher.addInsnPatch(Ref.EntityOtherPlayerMP$posX_GET::is),
                methodPatcher.addInsnPatch(Ref.EntityOtherPlayerMP$prevPosX_GET::is),

                methodPatcher.addInsnPatch((node, iterator) -> {
                    if (node.getOpcode() == Opcodes.DSTORE && node instanceof VarInsnNode) {
                        this.xDiffLocalVar = ((VarInsnNode)node).var;
                        return true;
                    }
                    return false;
                }),

                methodPatcher.addInsnPatch(Ref.EntityOtherPlayerMP$posZ_GET::is),
                methodPatcher.addInsnPatch(Ref.EntityOtherPlayerMP$prevPosZ_GET::is),

                methodPatcher.addInsnPatch((node, iterator) -> {
                    if (node.getOpcode() == Opcodes.DSTORE && node instanceof VarInsnNode) {
                        int zDiffLocalVar = ((VarInsnNode)node).var;
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));                    // this
                        iterator.add(new VarInsnNode(Opcodes.DLOAD, this.xDiffLocalVar));   // this, xDiff
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));                    // this, xDiff, this
                        Ref.EntityOtherPlayerMP$posY_GET.addTo(iterator);                   // this, xDiff, posY
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));                    // this, xDiff, posY, this
                        Ref.EntityOtherPlayerMP$prevPosY_GET.addTo(iterator);               // this, xDiff, posY, prevPosY
                        iterator.add(new InsnNode(Opcodes.DSUB));                           // this, xDiff, yDiff
                        iterator.add(new VarInsnNode(Opcodes.DLOAD, zDiffLocalVar));        // this, xDiff, yDiff, zDiff
                        Ref.Hooks$inverseAdjustXYZ.addTo(iterator);                         // doubles
                        iterator.add(new InsnNode(Opcodes.DUP));                            // doubles, doubles
                        iterator.add(new InsnNode(Opcodes.ICONST_0));                       // doubles, doubles, 0
                        iterator.add(new InsnNode(Opcodes.DALOAD));                         // doubles, doubles[0]
                        iterator.add(new VarInsnNode(Opcodes.DSTORE, this.xDiffLocalVar));  // doubles (doubles[0] stored to xDiff local var)
                        iterator.add(new InsnNode(Opcodes.ICONST_2));                       // doubles, 2
                        iterator.add(new InsnNode(Opcodes.DALOAD));                         // doubles[2]
                        iterator.add(new VarInsnNode(Opcodes.DSTORE, zDiffLocalVar));       // [empty] (doubles[2] stored to xDiff local var)
                        return true;
                    }
                    return false;
                })
        );
    }
}
