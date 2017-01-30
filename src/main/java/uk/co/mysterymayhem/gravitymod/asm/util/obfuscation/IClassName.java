package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation;

import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.ArrayName;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public interface IClassName {
    default ArrayName asArray() {
        return new ArrayName(this);
    }

    default String asDescriptor() {
        return this.toString();
    }
}
