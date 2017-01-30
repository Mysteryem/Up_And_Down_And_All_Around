package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names;

import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IClassName;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class ObjectName extends DeobfAwareString implements IClassName {
    public ObjectName(String name) {
        super(name);
    }

    @Override
    public String asDescriptor() {
        return 'L' + this.toString() + ';';
    }
}
