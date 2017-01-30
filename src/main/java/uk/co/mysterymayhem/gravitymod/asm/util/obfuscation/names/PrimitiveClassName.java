package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names;

import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.DeobfAwareString;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IClassProxy;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class PrimitiveClassName extends DeobfAwareString implements IClassProxy {
    public PrimitiveClassName(String name) {
        super(name);
    }
}
