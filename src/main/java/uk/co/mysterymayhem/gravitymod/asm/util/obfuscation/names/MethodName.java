package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names;

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
}
