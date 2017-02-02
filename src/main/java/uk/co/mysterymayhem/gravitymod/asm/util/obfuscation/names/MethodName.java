package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class MethodName extends DeobfAwareString {

    public MethodName(String deobf, String obf) {
        super(deobf, obf);
    }

    public MethodName(String name) {
        super(name);
    }

    public boolean is(MethodNode methodNode) {
        return this.is(methodNode.name);
    }

    public boolean is(AbstractInsnNode abstractInsnNode) {
        if (abstractInsnNode instanceof MethodInsnNode) {
            return this.is((MethodInsnNode)abstractInsnNode);
        }
        return false;
    }

    public boolean is(MethodInsnNode methodInsnNode) {
        return this.is(methodInsnNode.name);
    }
}
