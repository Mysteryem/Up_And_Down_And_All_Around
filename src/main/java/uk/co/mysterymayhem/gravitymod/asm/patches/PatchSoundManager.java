package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.VarInsnNode;
import uk.co.mysterymayhem.gravitymod.asm.Ref;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.MethodPatcher;

/**
 * When the camera rotates due to a change in gravity, the player's ears also have to be rotated.
 * <p>
 * setListener calls SoundSystem::setListenerOrientation as if the player has normal gravity.
 * <p>
 * A hook is inserted that replaces this call and calls SoundSystem::setListenerOrientation with arguments that take into account the rotation of the
 * player's camera
 * <p>
 * Created by Mysteryem on 2017-01-31.
 */
public class PatchSoundManager extends ClassPatcher {
    public PatchSoundManager() {
        super("net.minecraft.client.audio.SoundManager", 0, ClassWriter.COMPUTE_MAXS);
        MethodPatcher patch_setListener = this.addMethodPatch(Ref.SoundManager$setListener_name::is);
        patch_setListener.addInsnPatch((node, iterator) -> {
            if (Ref.SoundSystem$setListenerOrientation_name.is(node)) {
                Ref.Hooks$setListenerOrientationHook.replace(iterator);
                iterator.previous();
                iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load EntityPlayer method argument
                return true;
            }
            return false;
        });

        //TODO: Try replacing with a patch that makes rotation variables relative at the start and then back to absolute afterwards
        this.addMethodPatch(
                Ref.SoundManager$setListener_name::is,
                (node) -> Transformer.patchMethodUsingAbsoluteRotations(
                        node,
                        Transformer.GET_ROTATIONYAW | Transformer.GET_PREVROTATIONYAW | Transformer.GET_ROTATIONPITCH | Transformer.GET_PREVROTATIONPITCH));
    }
}
