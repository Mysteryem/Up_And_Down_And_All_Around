package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names;

import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.DeobfAwareString;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IClassProxy;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class ArrayClassName extends DeobfAwareString implements IClassProxy {

    public ArrayClassName(IClassProxy componentClass) {
        super(componentClass.toString());
    }

    @Override
    public String asDescriptor() {
        return '[' + this.toString();
    }
}
