package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions;

import org.objectweb.asm.Opcodes;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IDeobfAware;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.MethodDesc;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.ObjectCName;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class HooksInsn extends MethodInsn {
    private static final ObjectCName Hooks = new ObjectCName("uk/co/mysterymayhem/gravitymod/asm/Hooks");

    public HooksInsn(IDeobfAware methodName, MethodDesc description) {
        super(Opcodes.INVOKESTATIC, Hooks, methodName, description);
    }

    public HooksInsn(String methodName, MethodDesc description) {
        super(Opcodes.INVOKESTATIC, Hooks, methodName, description);
    }
}
