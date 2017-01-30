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
public class PatchSoundManager implements Function<byte[], byte[]> {

    @Override
    public byte[] apply(byte[] bytes) {
        int methodPatches = 0;
        int expectedMethodPatches = 1;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            // When the camera rotates due to a change in gravity, the player's ears would also have to be rotated
            //      setListener calls SoundSystem::setListenerOrientation as if the player has normal gravity. A hook is inserted
            //      that replaces this call and calls SoundSystem::setListenerOrientation with arguments that take into account
            //      the rotation of the player's camera
            if (Transformer.SoundManager$setListener_name.is(methodNode)) {
                Transformer.logPatchStarting(Transformer.SoundManager$setListener_name);
                boolean patched_setListenerOrientationInstruction = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (Transformer.SoundSystem$setListenerOrientation_name.is(methodInsnNode.name)) {
                            Transformer.Hooks$setListenerOrientationHook.replace(methodInsnNode);
                            iterator.previous();
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load EntityPlayer method argument
                            patched_setListenerOrientationInstruction = true;
                        }
                    }
                }
                Transformer.patchMethodUsingAbsoluteRotations(methodNode, Transformer.GET_ROTATIONYAW + Transformer.GET_PREVROTATIONYAW + Transformer.GET_ROTATIONPITCH + Transformer.GET_PREVROTATIONPITCH);
                Transformer.dieIfFalse(patched_setListenerOrientationInstruction, "Failed to patch setListenerOrientation instruction");
                Transformer.logPatchComplete(Transformer.SoundManager$setListener_name);
                methodPatches++;
            }
        }
        Transformer.dieIfFalse(methodPatches == expectedMethodPatches, "Could not find " + Transformer.SoundManager$setListener_name + " method");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
