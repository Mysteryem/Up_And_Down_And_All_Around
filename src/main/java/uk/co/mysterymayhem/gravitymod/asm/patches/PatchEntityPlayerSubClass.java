package uk.co.mysterymayhem.gravitymod.asm.patches;

import org.objectweb.asm.tree.*;
import uk.co.mysterymayhem.gravitymod.asm.Ref;
import uk.co.mysterymayhem.gravitymod.asm.Transformer;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.ObjectName;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;

import java.util.ListIterator;

/**
 * Inserts UpAndDown's EntityPlayerWithGravity class into EntityPlayerMP's and AbstractClientPlayer's hierarchy.
 * This is done to significantly lower the amount of ASM required and make it easier to code and debug the mod.
 * <p>
 * Created by Mysteryem on 2017-01-30.
 */
public class PatchEntityPlayerSubClass extends ClassPatcher {

    // Superclass replacement details:
    // Effectively patches:
    //  Cleanly calls super method:
    //      moveEntity, jump, handleWaterMovement, setEntityBoundingBox, updateFallState, pushOutOfBlocks (NYI), knockback, setAngles
    //  Should always call super:
    //      getEntityBoundingBox (in the case it doesn't call super, the mod probably isn't going to work anyway)
    //  Sometimes calls super:
    //      getEyeHeight (super call is used elsewhere to simulate the effects of calling super)
    //  Only calls super for downwards gravity:
    //      resetPositionToBB, (method is basically completely replaced outside of downwards gravity, ASM probably not worth it)
    //      playStepSound (done in order to bypass some vanilla code)
    //  Shouldn't ever call super:
    //      isEntityInsideOpaqueBlock, (replacing with ASM looks like a good idea)
    //      createRunningParticles, (replacing with ASM looks like a good idea)
    //      isOnLadder (method pretty much entirely replaced)
    //  Never calls super!:
    //      updateSize, (replacing with ASM looks like a good idea)
    //      setSize, (probably can be replaced with ASM?)
    //      setPosition (method pretty much entirely replaced)
    public static final String classToReplace = Ref.EntityPlayer.toString();
    public static final String classReplacement = Ref.EntityPlayerWithGravity.toString();

    public PatchEntityPlayerSubClass(ObjectName className) {
        super(className.asClassName());
    }

    @Override
    protected void patchClass(ClassNode classNode) {
        classNode.superName = classReplacement;

        for (MethodNode methodNode : classNode.methods) {
            for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                AbstractInsnNode next = iterator.next();
                switch (next.getType()) {
                    case (AbstractInsnNode.TYPE_INSN):
                        TypeInsnNode typeInsnNode = (TypeInsnNode)next;
                        if (typeInsnNode.desc.equals(classToReplace)) {
                            typeInsnNode.desc = classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.FIELD_INSN):
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (fieldInsnNode.owner.equals(classToReplace)) {
                            fieldInsnNode.owner = classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.METHOD_INSN):
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (methodInsnNode.owner.equals(classToReplace)) {
                            methodInsnNode.owner = classReplacement;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        Transformer.log("Injected super class into " + classNode.name);
    }
}
