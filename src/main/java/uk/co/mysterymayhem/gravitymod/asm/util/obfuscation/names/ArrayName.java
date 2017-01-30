package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names;

import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IClassName;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class ArrayName extends DeobfAwareString implements IClassName {

    public ArrayName(IClassName componentClass) {
        super(componentClass.toString());
    }

    @Override
    public String asDescriptor() {
        return '[' + this.toString();
    }
}
