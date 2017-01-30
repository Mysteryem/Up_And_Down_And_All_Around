package uk.co.mysterymayhem.gravitymod.asm.util.obfuscation;

import java.util.Objects;

/**
 * Created by Mysteryem on 2017-01-30.
 */
public class DeobfAwareString implements IDeobfAware {

    private final String deobf;
    private final String value;

    public DeobfAwareString(String deobf, String obf) {
        this.value = ObfuscationHelper.IS_DEV_ENVIRONMENT ? this.getDeobfName(Objects.requireNonNull(obf)) : Objects.requireNonNull(obf);
        this.deobf = Objects.requireNonNull(deobf);
    }

    private String getDeobfName(String obf) {
        String toReturn = ObfuscationHelper.deobfNameLookup.get(Objects.requireNonNull(obf));
        if (toReturn == null) {
            System.out.println("[UpAndDown] [WARNING] Could not find mcp name for srg name \"" + obf + "\"");
            toReturn = obf;
        }
        return toReturn;
    }

    public DeobfAwareString(String name) {
        this.value = Objects.requireNonNull(name);
        this.deobf = this.value;
    }

    //        @Override
    public String getDeobf() {
        return this.deobf;
    }

    @Override
    public String toString() {
        return this.value;
    }


}
