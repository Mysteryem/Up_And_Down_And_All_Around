package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names;

import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.DeobfAwareString;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IClassProxy;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IDeobfAware;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class UnqualifiedName extends DeobfAwareString {
    public final String name;
    public final String owner;

    public UnqualifiedName(IClassProxy owner, IDeobfAware name) {
        this(owner, name.toString());
    }

    public UnqualifiedName(IClassProxy owner, String name) {
        super(owner + "." + name);
        this.owner = owner.toString();
        this.name = name;
    }

    public boolean is(String owner, String name) {
        return this.owner.equals(owner) && this.name.equals(name);
    }
}
