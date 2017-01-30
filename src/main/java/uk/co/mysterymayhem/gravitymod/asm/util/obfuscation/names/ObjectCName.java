package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names;

import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.DeobfAwareString;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IClassProxy;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class ObjectCName extends DeobfAwareString implements IClassProxy {
    public ObjectCName(String name) {
        super(name);
    }

    @Override
    public String asDescriptor() {
        return 'L' + this.toString() + ';';
    }
}
