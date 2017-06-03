package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.VarInsnNode;
import uk.co.mysterymayhem.gravitymod.asm.Ref;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;

/**
 * Created by Mysteryem on 29/05/2017.
 */
public class PatchWorld extends ClassPatcher {
    public PatchWorld() {
        super("net.minecraft.world.World", 0, ClassWriter.COMPUTE_MAXS);

        this.addMethodPatch(Ref.World$getCollisionBoxes_name::is, (node, iterator) -> {
            if (node instanceof LineNumberNode) {
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // aabb method arg
                Ref.Hooks$normaliseAABB.addTo(iterator); // normalise AABB
                iterator.add(new VarInsnNode(Opcodes.ASTORE, 2)); // store normalised aabb
                return true;
            }
            return false;
        });
    }
}