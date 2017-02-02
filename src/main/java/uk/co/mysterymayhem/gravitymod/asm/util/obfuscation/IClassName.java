package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation;

import org.objectweb.asm.Type;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.ArrayName;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public interface IClassName {
    default ArrayName asArray() {
        return new ArrayName(this);
    }

    default Type asType() {
        return Type.getType(this.asDescriptor());
    }

    default String asDescriptor() {
        return this.toString();
    }
}
