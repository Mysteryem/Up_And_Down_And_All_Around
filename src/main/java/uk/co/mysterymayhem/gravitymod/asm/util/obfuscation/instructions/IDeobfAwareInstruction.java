package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IDeobfAware;

import java.util.ListIterator;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public interface IDeobfAwareInstruction extends IDeobfAware {
    boolean is(AbstractInsnNode abstractInsnNode);

    default void addTo(InsnList insnList) {
        insnList.add(this.asAbstract());
    }

    AbstractInsnNode asAbstract();

    default void replace(ListIterator<AbstractInsnNode> it) {
        it.remove();
        this.addTo(it);
    }

    default void addTo(ListIterator<AbstractInsnNode> it) {
        it.add(this.asAbstract());
    }
}
