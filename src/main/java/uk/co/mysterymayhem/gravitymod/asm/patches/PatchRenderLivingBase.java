package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;

import java.util.function.Function;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class PatchRenderLivingBase implements Function<byte[], byte[]> {

    @Override
    public byte[] apply(byte[] bytes) {
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            // If a player looks 'up' from their perspective, we want their head to look 'up'.
            // If a player has non standard gravity, then their pitch (and yaw) won't match their own perspective
            //      (player rotation is always relative to normal minecraft gravity so that bows and similar work properly)
            // This replaces all accesses to the player's rotation fields with static hook calls that calculate and
            //      return the field in question, as seen from the player's perspective.
            if (Transformer.RenderLivingBase$doRender_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.RenderLivingBase$doRender_name);
                Transformer.patchMethodUsingAbsoluteRotations(methodNode, Transformer.ALL_GET_ROTATION_VARS);
                Transformer.logPatchComplete(Transformer.RenderLivingBase$doRender_name);
                methodPatches++;
                break;
            }
        }

        Transformer.dieIfFalse(methodPatches == 1, classNode);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
