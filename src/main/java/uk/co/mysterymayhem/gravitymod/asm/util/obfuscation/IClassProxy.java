package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation;

import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.ArrayClassName;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public interface IClassProxy {
    default ArrayClassName asArray() {
        return new ArrayClassName(this);
    }

    default String asDescriptor() {
        return this.toString();
    }
}
