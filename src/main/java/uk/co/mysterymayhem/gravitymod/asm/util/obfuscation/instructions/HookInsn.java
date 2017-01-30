package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions;

import org.objectweb.asm.Opcodes;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IDeobfAware;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.MethodDesc;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.ObjectName;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class HookInsn extends MethodInsn {
    private static final ObjectName Hooks = new ObjectName("uk/co/mysterymayhem/gravitymod/asm/Hooks");

    public HookInsn(IDeobfAware methodName, MethodDesc description) {
        super(Opcodes.INVOKESTATIC, Hooks, methodName, description);
    }

    public HookInsn(String methodName, MethodDesc description) {
        super(Opcodes.INVOKESTATIC, Hooks, methodName, description);
    }
}
